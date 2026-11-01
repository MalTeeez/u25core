package net.sxmaa.u25core.clouds;

import Reika.RotaryCraft.API.Event.WeatherControlEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** The clouds above RotaryCraft's weather controller open up when it changes the weather. */
public final class RotaryCraftWeather {

    @SubscribeEvent
    public void onWeatherControl(WeatherControlEvent event) {
        CloudCompat.skyColumn(event.getWorld(), event.getTileX() + 0.5, event.getTileY() + 1, event.getTileZ() + 0.5);
    }
}
