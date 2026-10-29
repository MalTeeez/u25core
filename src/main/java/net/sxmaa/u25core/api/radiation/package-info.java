/**
 * U25Core radiation API.
 * <p>
 * <b>Depending on it:</b> compile against the api jar as compileOnly. Without U25Core installed there is no radiation,
 * so the API classes are absent at runtime:
 * <ul>
 * <li>Only call {@link net.sxmaa.u25core.api.radiation.RadiationAPI} when {@code Loader.isModLoaded("u25core")}, and
 * keep those calls in a separate class so they don't get loaded otherwise.</li>
 * <li>Classes <i>implementing</i> an interface from this package (tiles, items, blocks, entities) would fail to load
 * without U25Core. Strip the interface with
 * {@code @Optional.Interface(iface = "net.sxmaa.u25core.api.radiation.IRadiationSource", modid = "u25core")} and its
 * methods with {@code @Optional.Method(modid = "u25core")}.</li>
 * <li>Don't add {@code after:u25core} to your mod dependencies. U25Core loads after DragonAPI mods, which load after
 * many others, so it easily causes a sorting cycle. It isn't needed: registrations made before U25Core loads are
 * buffered and applied.</li>
 * </ul>
 * <b>Model:</b> every chunk has an ambient level in Sv/h that moves towards the sum of its sources (tiles implementing
 * {@link net.sxmaa.u25core.api.radiation.IRadiationSource}, radioactive dropped items) plus its contamination, and
 * spreads to neighbouring chunks. Contamination is lingering radiation released into a chunk (meltdowns, dumped
 * waste) that decays with a half-life. Entities accumulate dose in Sv from the level where they are, what they carry
 * and direct exposure; their armor's resistance is applied once per update to the total.
 * <p>
 * <b>Units:</b> levels, emissions and exposure in Sv/h, dose in Sv, radioactivity in Sv/h per item or per mB.
 */
package net.sxmaa.u25core.api.radiation;
