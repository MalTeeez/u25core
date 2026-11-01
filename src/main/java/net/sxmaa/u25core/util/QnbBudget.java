package net.sxmaa.u25core.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.sxmaa.u25core.U25Core;
import net.sxmaa.u25core.config.ModConfig;

/**
 * Budget for AE2 RF/EU P2P energy crossing a Quantum Network Bridge: one per ME grid, qnbRFPerTick per live QNB link
 * in it. A tunnel counts as crossing when one of its outputs is in another dimension, which only a QNB allows. Used by
 * the RF and EU P2P QNB mixins. AE2 isn't a compile dependency, so its calls go through reflection.
 */
public final class QnbBudget {

    private static final Map<Object, TransferBudget> BY_GRID = new WeakHashMap<>();
    private static final Map<Object, Tunnel> BY_TUNNEL = new WeakHashMap<>();
    private static final Method GET_TILE = method("appeng.parts.AEBasePart", "getTile"),
        GET_GRID_NODE = method("appeng.parts.AEBasePart", "getGridNode"),
        GET_GRID = method("appeng.api.networking.IGridNode", "getGrid"),
        GET_OUTPUTS = method("appeng.parts.p2p.PartP2PTunnel", "getOutputs"),
        GET_MACHINES = method("appeng.api.networking.IGrid", "getMachines", Class.class),
        GET_MACHINE = method("appeng.api.networking.IGridNode", "getMachine"),
        GET_CLUSTER = method("appeng.tile.qnb.TileQuantumBridge", "getCluster"),
        GET_CLUSTER_LINK = method("appeng.me.cluster.implementations.QuantumCluster", "getConnection"),
        GET_LINK_CONNECTION = method("appeng.me.cache.helpers.ConnectionWrapper", "getConnection");

    private QnbBudget() {}

    private static final class Tunnel {

        int nextCheck;
        TransferBudget budget; // null while no output of this tunnel is across a QNB
        int links;
    }

    /** RF the tunnel may still send across a QNB this tick; infinite if nothing it sends crosses one. */
    public static double remaining(Object tunnel) {
        if (ModConfig.qnbRFPerTick <= 0) return Double.POSITIVE_INFINITY;
        Tunnel t = refresh(tunnel);
        return t.budget == null ? Double.POSITIVE_INFINITY
            : t.budget.remaining((double) ModConfig.qnbRFPerTick * t.links);
    }

    public static void use(Object tunnel, double rf) {
        Tunnel t = BY_TUNNEL.get(tunnel);
        if (t != null && t.budget != null) t.budget.use(rf);
    }

    // ponytail: rechecks outputs, grid and links once a second per tunnel instead of on grid events,
    // so a change takes up to 20 ticks to apply
    private static Tunnel refresh(Object tunnel) {
        Tunnel t = BY_TUNNEL.computeIfAbsent(tunnel, k -> new Tunnel());
        MinecraftServer server = MinecraftServer.getServer();
        int now = server == null ? 0 : server.getTickCounter();
        if (now < t.nextCheck) return t;
        t.nextCheck = now + 20;
        t.budget = null;
        try {
            World world = ((TileEntity) GET_TILE.invoke(tunnel)).getWorldObj();
            if (world == null || world.isRemote) return t;
            boolean crosses = false;
            for (Object out : (Iterable<?>) GET_OUTPUTS.invoke(tunnel)) {
                if (((TileEntity) GET_TILE.invoke(out)).getWorldObj() != world) {
                    crosses = true;
                    break;
                }
            }
            if (!crosses) return t;
            Object node = GET_GRID_NODE.invoke(tunnel);
            Object grid = node == null ? null : GET_GRID.invoke(node);
            if (grid == null) return t;
            // At least one: something other than a QNB spanning dimensions still gets a single link's worth
            t.links = Math.max(1, countLinks(grid));
            t.budget = BY_GRID.computeIfAbsent(grid, k -> new TransferBudget());
        } catch (Exception e) {
            // No grid (GridAccessException), a part mid-placement, or a lookup that failed at load:
            // uncapped until the next check
            t.budget = null;
        }
        return t;
    }

    /** Live QNB links in the grid. Both ends of a link share one ConnectionWrapper, so each counts once. */
    private static int countLinks(Object grid) throws ReflectiveOperationException {
        Set<Object> links = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Object node : (Iterable<?>) GET_MACHINES.invoke(grid, GET_CLUSTER.getDeclaringClass())) {
            Object cluster = GET_CLUSTER.invoke(GET_MACHINE.invoke(node));
            if (cluster == null) continue;
            Object link = GET_CLUSTER_LINK.invoke(cluster);
            if (link != null && GET_LINK_CONNECTION.invoke(link) != null) links.add(link);
        }
        return links.size();
    }

    private static Method method(String owner, String name, Class<?>... args) {
        try {
            return Class.forName(owner, false, Launch.classLoader)
                .getMethod(name, args);
        } catch (ReflectiveOperationException e) {
            U25Core.LOG.error("AE2 has no {}.{}, the QNB P2P energy cap is off", owner, name, e);
            return null;
        }
    }
}
