package me.flawless.mod.modules.impl.render;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.ColorSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.awt.*;


public class Ambience extends Module {

    public static Ambience INSTANCE;
    public final ColorSetting worldColor = add(new ColorSetting("WorldColor", new Color(0xFFFFFFFF, true)).injectBoolean(true));
    public final BooleanSetting customTime =
            add(new BooleanSetting("CustomTime", false).setParent());
    private final SliderSetting time =
            add(new SliderSetting("Time", 0, 0, 24000, v -> customTime.isOpen()));
    public final ColorSetting fog =
            add(new ColorSetting("FogColor", new Color(0xCC7DD5)).injectBoolean(false));
    public final ColorSetting sky =
            add(new ColorSetting("SkyColor", new Color(0x000000)).injectBoolean(false));
    public final BooleanSetting fogDistance =
            add(new BooleanSetting("FogDistance", false).setParent());
    public final SliderSetting fogStart =
            add(new SliderSetting("FogStart", 50, 0, 1000, v -> fogDistance.isOpen()));
    public final SliderSetting fogEnd =
            add(new SliderSetting("FogEnd", 100, 0, 1000, v -> fogDistance.isOpen()));
    public final BooleanSetting fullBright =
            add(new BooleanSetting("FullBright", false));
    public final BooleanSetting forceOverworld =
            add(new BooleanSetting("ForceOverworld", false));
    public Ambience() {
        super("Ambience", "Custom ambience", Category.Render);
        INSTANCE = this;
    }

    long oldTime;

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (fullBright.getValue()) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 100000, 0));
        }
        if (customTime.getValue()) {
            mc.world.setTimeOfDay((long) this.time.getValue());
        }
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        oldTime = mc.world.getTimeOfDay();
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;
        mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        mc.world.setTimeOfDay(oldTime);
    }

    @EventHandler
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            oldTime = ((WorldTimeUpdateS2CPacket) event.getPacket()).getTime();
            event.cancel();
        }
    }
}