package net.sxmaa.u25core.api.radiation;

/**
 * Implement on a TileEntity that emits radiation into its chunk. It is polled when its chunk is updated, so just
 * return the current value; call {@link RadiationAPI#notifySource} when it starts emitting.
 */
public interface IRadiationSource {

    /** Current emission in Sv/h, before shielding around the tile. */
    double getRadiationEmission();
}
