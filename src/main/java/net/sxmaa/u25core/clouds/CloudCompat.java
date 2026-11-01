package net.sxmaa.u25core.clouds;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.sxmaa.u25core.U25Core;
import net.sxmaa.u25core.config.ModConfig;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

/**
 * Lets mods push NotSoSimpleClouds' clouds around. Explosions that skip vanilla's explosion packet and weather machines
 * are reported by mixins and event handlers on the server and sent here; trails are found on the client (CloudTrails).
 */
public final class CloudCompat {

    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("u25core_clouds");
    static final byte EXPLOSION = 0, SKY_COLUMN = 1, NUKE = 2;
    /** Effects received off-thread as {kind, x, y, z, size}, applied on the next client tick. */
    static final Queue<float[]> PENDING = new ConcurrentLinkedQueue<>();

    private CloudCompat() {}

    public static void init() {
        CHANNEL.registerMessage(Handler.class, Message.class, 0, Side.CLIENT);
        if (ModConfig.cloudCompat && Loader.isModLoaded("RotaryCraft")) {
            MinecraftForge.EVENT_BUS.register(new RotaryCraftWeather());
        }
    }

    /**
     * Server side: a shockwave for everyone in the dimension. Strength is on vanilla's scale (TNT = 4, which reaches
     * about 2 * strength blocks).
     */
    public static void explosion(World world, double x, double y, double z, float strength) {
        if (!ModConfig.cloudCompat || world == null || world.isRemote || !(strength > 0)) return;
        send(world, EXPLOSION, x, y, z, strength);
    }

    /**
     * Server side: a nuclear blast, which clears the sky far and for long. Scale multiplies cloudNukeRadius (a
     * DefenseTech nuclear explosive is 1).
     */
    public static void nuke(World world, double x, double y, double z, float scale) {
        if (!ModConfig.cloudCompat || world == null || world.isRemote || !(scale > 0)) return;
        U25Core.LOG.info(
            "Nuclear blast at {} {} {} in dim {}, clearing the clouds at scale {}",
            (int) x,
            (int) y,
            (int) z,
            world.provider.dimensionId,
            scale);
        send(world, NUKE, x, y, z, scale);
    }

    /** Server side: a weather machine fired, the clouds above it open up. */
    public static void skyColumn(World world, double x, double y, double z) {
        if (!ModConfig.cloudCompat || world == null || world.isRemote) return;
        send(world, SKY_COLUMN, x, y, z, ModConfig.cloudWeatherMachineRadius);
    }

    private static void send(World world, byte kind, double x, double y, double z, float size) {
        CHANNEL.sendToDimension(new Message(kind, (float) x, (float) y, (float) z, size), world.provider.dimensionId);
    }

    public static class Message implements IMessage {

        byte kind;
        float x, y, z, size;

        public Message() {}

        Message(byte kind, float x, float y, float z, float size) {
            this.kind = kind;
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            kind = buf.readByte();
            x = buf.readFloat();
            y = buf.readFloat();
            z = buf.readFloat();
            size = buf.readFloat();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeByte(kind);
            buf.writeFloat(x);
            buf.writeFloat(y);
            buf.writeFloat(z);
            buf.writeFloat(size);
        }
    }

    public static class Handler implements IMessageHandler<Message, IMessage> {

        @Override
        public IMessage onMessage(Message m, MessageContext ctx) {
            // Only drained by CloudTrails, which needs NotSoSimpleClouds
            if (ModConfig.cloudCompat && Loader.isModLoaded("projectbabelfish")) {
                PENDING.add(new float[] { m.kind, m.x, m.y, m.z, m.size });
            }
            return null;
        }
    }
}
