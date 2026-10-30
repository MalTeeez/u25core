package net.sxmaa.u25core.util;

import net.minecraft.server.MinecraftServer;

/** Energy one block may move per tick, shared by every call it gets in that tick. */
public final class TransferBudget {

    private int tick = -1;
    private double moved;

    /** What's left of {@code limit} this tick. */
    public double remaining(double limit) {
        MinecraftServer server = MinecraftServer.getServer();
        int now = server == null ? 0 : server.getTickCounter();
        if (now != tick) {
            tick = now;
            moved = 0;
        }
        return Math.max(0, limit - moved);
    }

    public void use(double amount) {
        moved += amount;
    }
}
