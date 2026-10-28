package net.sxmaa.u25core.api.radiation;

/** Where a neutron came from, so handlers can tell e.g. fission neutrons from fusion ones. */
public enum NeutronType {
    DECAY,
    FISSION,
    FUSION
}
