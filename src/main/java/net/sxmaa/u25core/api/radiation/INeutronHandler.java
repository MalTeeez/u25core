package net.sxmaa.u25core.api.radiation;

import net.minecraft.world.World;

/**
 * Implement on a TileEntity or Block to react to neutrons passing through it (breeders, irradiators, moderators,
 * shielding). Checked before the shielding table.
 */
public interface INeutronHandler {

    /**
     * @param thermal whether the neutron was slowed down by a moderator on its way
     * @return true if the neutron is absorbed and stops here
     */
    boolean onNeutron(World world, int x, int y, int z, NeutronType type, boolean thermal);
}
