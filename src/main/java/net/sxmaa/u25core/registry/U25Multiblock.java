package net.sxmaa.u25core.registry;

import java.lang.reflect.InvocationTargetException;

import net.sxmaa.u25core.U25Core;
import net.sxmaa.u25core.common.multiblocks.IExternalMultiblock;
import net.sxmaa.u25core.common.multiblocks.enderio.TelepadMultiblock;
import net.sxmaa.u25core.common.multiblocks.reactorcraft.*;

import cpw.mods.fml.common.Loader;

public enum U25Multiblock {

    // TEST(TestMultiblock.class, "ReactorCraft"),
    TELEPAD(TelepadMultiblock.class, "EnderIO"),
    PLASMA_INJECTOR(PlasmaInjectorMultiblock.class, "ReactorCraft"),
    TOROID_MAGNETS(ToroidMagnetsMultiblock.class, "ReactorCraft"),
    PREHEATER(PreheaterMultiblock.class, "ReactorCraft"),
    SOLENOID(SolenoidMultiblock.class, "ReactorCraft"),
    HIGH_PRESSURE_TURBINE(HighPressureTurbineMultiblock.class, "ReactorCraft"),
    TURBINE_FLYWHEEL(TurbineFlywheelMultiblock.class, "ReactorCraft"),
    TURBINE_GENERATOR(TurbineGeneratorMultiblock.class, "ReactorCraft");

    private final IExternalMultiblock instance;

    public final String requiredMod;
    public final Class<? extends IExternalMultiblock> multiClass;

    public static final U25Multiblock[] multiblocks = values();

    U25Multiblock(Class<? extends IExternalMultiblock> clazz, String requiredMod) {
        if (requiredMod == null || Loader.isModLoaded(requiredMod)) {
            try {
                this.instance = clazz.getDeclaredConstructor()
                    .newInstance();
                this.instance.registerSelf();
                this.requiredMod = requiredMod;
                this.multiClass = clazz;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            this.requiredMod = requiredMod;
            this.multiClass = clazz;
            this.instance = null;
        }
    }

    public IExternalMultiblock getInstance() {
        return instance;
    }

    public static void init() {
        U25Core.LOG.debug("Registering Multiblocks");
    }
}
