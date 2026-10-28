package net.sxmaa.u25core.radiation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.sxmaa.u25core.U25Core;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

public final class RadiationNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(U25Core.MODID);

    private RadiationNetwork() {}

    public static void init() {
        CHANNEL.registerMessage(PlayerHandler.class, PlayerMessage.class, 0, Side.CLIENT);
        CHANNEL.registerMessage(CherenkovHandler.class, CherenkovMessage.class, 1, Side.CLIENT);
    }

    /** The player's own dose, absorbed rate and the ambient level, for HUD and sounds. */
    public static class PlayerMessage implements IMessage {

        public double dose, rate, level;

        public PlayerMessage() {}

        PlayerMessage(double dose, double rate, double level) {
            this.dose = dose;
            this.rate = rate;
            this.level = level;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            dose = buf.readDouble();
            rate = buf.readDouble();
            level = buf.readDouble();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeDouble(dose);
            buf.writeDouble(rate);
            buf.writeDouble(level);
        }
    }

    public static class PlayerHandler implements IMessageHandler<PlayerMessage, IMessage> {

        @Override
        public IMessage onMessage(PlayerMessage message, MessageContext ctx) {
            RadiationClientState.dose = message.dose;
            RadiationClientState.rate = message.rate;
            RadiationClientState.level = message.level;
            return null;
        }
    }

    /** Positions and emissions of hot sources near the player, for Cherenkov glow. */
    public static class CherenkovMessage implements IMessage {

        public List<double[]> sources = new ArrayList<>();

        public CherenkovMessage() {}

        CherenkovMessage(List<double[]> sources) {
            this.sources = sources;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            int count = buf.readShort();
            for (int i = 0; i < count; i++) {
                sources.add(new double[] { buf.readInt(), buf.readInt(), buf.readInt(), buf.readFloat() });
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeShort(sources.size());
            for (double[] s : sources) {
                buf.writeInt((int) s[0]);
                buf.writeInt((int) s[1]);
                buf.writeInt((int) s[2]);
                buf.writeFloat((float) s[3]);
            }
        }
    }

    public static class CherenkovHandler implements IMessageHandler<CherenkovMessage, IMessage> {

        @Override
        public IMessage onMessage(CherenkovMessage message, MessageContext ctx) {
            RadiationClientState.hotSources = message.sources;
            RadiationClientState.hotSourcesTime = System.currentTimeMillis();
            return null;
        }
    }

    /** Last values sent per player, to only send when the display would change. */
    static boolean shouldSend(double[] last, double dose, double rate, double level) {
        return differs(last[0], dose) || differs(last[1], rate) || differs(last[2], level);
    }

    /** True if two values differ by more than ~1% or cross zero. */
    private static boolean differs(double a, double b) {
        if (a == b) return false;
        if (a == 0 || b == 0) return true;
        return Math.abs(a - b) / Math.max(Math.abs(a), Math.abs(b)) > 0.01;
    }

    static void sendPlayer(EntityPlayerMP player, double dose, double rate, double level) {
        CHANNEL.sendTo(new PlayerMessage(dose, rate, level), player);
    }

    static void sendCherenkov(EntityPlayerMP player, List<double[]> sources) {
        CHANNEL.sendTo(new CherenkovMessage(sources), player);
    }
}
