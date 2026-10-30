package net.sxmaa.u25core.dump;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.sxmaa.u25core.U25Core;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import codechicken.nei.ItemList;
import codechicken.nei.guihook.GuiContainerManager;

/**
 * Renders the icon of every NEI item that appears in recipes.jsonl into one atlas: icons.png, and icons.json mapping
 * mod:name:meta(+NBT) to its [x, y] there. Draws a screenful per frame and reads it back, like NEI's icon dumper.
 * Icon size in pixels: -Du25core.iconSize (default 32).
 */
public class IconDump extends GuiScreen {

    private static final int ATLAS_WIDTH = 4096;

    private final File dir;
    private final Consumer<JsonObject> finished;
    private final int size = Integer.getInteger("u25core.iconSize", 32);
    private final long start = System.currentTimeMillis();
    private volatile List<ItemStack> stacks;
    private BufferedImage atlas;
    private final JsonObject positions = new JsonObject();
    private int next;
    private boolean done;

    IconDump(File dir, Consumer<JsonObject> finished) {
        this.dir = dir;
        this.finished = finished;
        Thread thread = new Thread(() -> stacks = wanted(), "u25core icon list");
        thread.setDaemon(true);
        thread.start();
    }

    /** NEI items whose id shows up in a recipe; the ids are read off recipes.jsonl. */
    private List<ItemStack> wanted() {
        Set<String> ids = new HashSet<>();
        try (BufferedReader in = Files
            .newBufferedReader(new File(dir, "recipes.jsonl").toPath(), StandardCharsets.UTF_8)) {
            JsonParser parser = new JsonParser();
            for (String line; (line = in.readLine()) != null;) {
                JsonObject recipe = parser.parse(line)
                    .getAsJsonObject();
                for (String side : new String[] { "inputs", "outputs" }) {
                    for (JsonElement stack : recipe.getAsJsonArray(side)) {
                        for (JsonElement id : stack.getAsJsonObject()
                            .getAsJsonArray("ids")) ids.add(id.getAsString());
                    }
                }
            }
        } catch (IOException e) {
            U25Core.LOG.error("Icons need recipes.jsonl, run the recipes part first", e);
        }
        List<ItemStack> wanted = new ArrayList<>();
        Set<String> taken = new HashSet<>();
        for (ItemStack stack : ItemList.items) {
            String id = RecipeDump.id(stack);
            if (ids.contains(id) && taken.add(id)) wanted.add(stack);
        }
        return wanted;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        List<ItemStack> stacks = this.stacks;
        if (stacks == null || done) return;
        if (atlas == null) {
            int perRow = ATLAS_WIDTH / size;
            int rows = Math.max(1, (stacks.size() + perRow - 1) / perRow);
            atlas = new BufferedImage(ATLAS_WIDTH, rows * size, BufferedImage.TYPE_INT_ARGB);
        }
        Minecraft mc = Minecraft.getMinecraft();
        int width = mc.displayWidth, height = mc.displayHeight;
        int cols = width / size, rows = height / size;
        int batch = Math.min(cols * rows, stacks.size() - next);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, 1000, 3000);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glTranslatef(0, 0, -2000);
        GL11.glClearColor(0, 0, 0, 0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        for (int i = 0; i < batch; i++) {
            GL11.glPushMatrix();
            GL11.glTranslatef((i % cols) * size, (i / cols) * size, 0);
            GL11.glScalef(size / 16f, size / 16f, 1);
            GuiContainerManager.drawItem(0, 0, stacks.get(next + i));
            GL11.glPopMatrix();
        }
        RenderHelper.disableStandardItemLighting();
        GL11.glFlush();

        IntBuffer pixels = BufferUtils.createIntBuffer(width * height);
        GL11.glReadPixels(0, 0, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixels);
        int[] row = new int[size];
        int perRow = ATLAS_WIDTH / size;
        for (int i = 0; i < batch; i++) {
            int sx = (i % cols) * size, sy = (i / cols) * size;
            int index = next + i, ax = (index % perRow) * size, ay = (index / perRow) * size;
            for (int y = 0; y < size; y++) {
                // GL rows run bottom up
                pixels.position((height - 1 - (sy + y)) * width + sx);
                pixels.get(row);
                atlas.setRGB(ax, ay + y, size, 1, row, 0, size);
            }
            JsonArray xy = new JsonArray();
            xy.add(new JsonPrimitive(ax));
            xy.add(new JsonPrimitive(ay));
            positions.add(RecipeDump.id(stacks.get(index)), xy);
        }
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        next += batch;
        if (next >= stacks.size()) finish();
    }

    private void finish() {
        done = true;
        JsonObject info = new JsonObject();
        try {
            ImageIO.write(atlas, "png", new File(dir, "icons.png"));
            JsonObject json = new JsonObject();
            json.addProperty("size", size);
            json.add("icons", positions);
            DataDump.writeJson(new File(dir, "icons.json"), json);
            info.addProperty("count", next);
        } catch (Throwable t) {
            U25Core.LOG.error("Writing the icon atlas failed", t);
            info.addProperty("error", t.toString());
        }
        info.addProperty("millis", System.currentTimeMillis() - start);
        Minecraft.getMinecraft()
            .displayGuiScreen(null);
        finished.accept(info);
    }
}
