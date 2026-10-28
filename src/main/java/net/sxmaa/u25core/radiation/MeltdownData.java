package net.sxmaa.u25core.radiation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/** Pending meltdowns of one world: explosions over time, then contamination. Saved with the world. */
public final class MeltdownData extends WorldSavedData {

    private static final String NAME = "u25core_meltdowns";
    private static final Random RAND = new Random();

    private static final class Meltdown {

        double minX, minY, minZ, maxX, maxY, maxZ, contamination;
        float power;
        int ticksLeft;
    }

    private final List<Meltdown> meltdowns = new ArrayList<>();

    public MeltdownData(String name) {
        super(name);
    }

    public static MeltdownData get(World world) {
        MeltdownData data = (MeltdownData) world.perWorldStorage.loadData(MeltdownData.class, NAME);
        if (data == null) {
            data = new MeltdownData(NAME);
            world.perWorldStorage.setData(NAME, data);
        }
        return data;
    }

    void add(AxisAlignedBB bounds, float power, double contamination, int ticks) {
        Meltdown m = new Meltdown();
        m.minX = bounds.minX;
        m.minY = bounds.minY;
        m.minZ = bounds.minZ;
        m.maxX = bounds.maxX;
        m.maxY = bounds.maxY;
        m.maxZ = bounds.maxZ;
        m.power = power;
        m.contamination = contamination;
        m.ticksLeft = Math.max(1, ticks);
        meltdowns.add(m);
        markDirty();
    }

    void tick(World world) {
        if (meltdowns.isEmpty()) return;
        for (Iterator<Meltdown> it = meltdowns.iterator(); it.hasNext();) {
            Meltdown m = it.next();
            double x = m.minX + RAND.nextDouble() * (m.maxX - m.minX);
            double y = m.minY + RAND.nextDouble() * (m.maxY - m.minY);
            double z = m.minZ + RAND.nextDouble() * (m.maxZ - m.minZ);
            if (!world.blockExists((int) x, (int) y, (int) z)) continue;
            if (RAND.nextInt(5) == 0) world.createExplosion(null, x, y, z, m.power, true);
            if (--m.ticksLeft <= 0) {
                RadiationManager.INSTANCE.contaminate(
                    world,
                    (m.minX + m.maxX) / 2,
                    (m.minY + m.maxY) / 2,
                    (m.minZ + m.maxZ) / 2,
                    m.contamination);
                it.remove();
            }
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        meltdowns.clear();
        NBTTagList list = tag.getTagList("meltdowns", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound t = list.getCompoundTagAt(i);
            Meltdown m = new Meltdown();
            m.minX = t.getDouble("minX");
            m.minY = t.getDouble("minY");
            m.minZ = t.getDouble("minZ");
            m.maxX = t.getDouble("maxX");
            m.maxY = t.getDouble("maxY");
            m.maxZ = t.getDouble("maxZ");
            m.power = t.getFloat("power");
            m.contamination = t.getDouble("contamination");
            m.ticksLeft = t.getInteger("ticksLeft");
            meltdowns.add(m);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (Meltdown m : meltdowns) {
            NBTTagCompound t = new NBTTagCompound();
            t.setDouble("minX", m.minX);
            t.setDouble("minY", m.minY);
            t.setDouble("minZ", m.minZ);
            t.setDouble("maxX", m.maxX);
            t.setDouble("maxY", m.maxY);
            t.setDouble("maxZ", m.maxZ);
            t.setFloat("power", m.power);
            t.setDouble("contamination", m.contamination);
            t.setInteger("ticksLeft", m.ticksLeft);
            list.appendTag(t);
        }
        tag.setTag("meltdowns", list);
    }
}
