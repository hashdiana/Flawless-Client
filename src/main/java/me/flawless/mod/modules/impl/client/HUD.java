package me.flawless.mod.modules.impl.client;

import me.flawless.FlawLess;
import me.flawless.api.utils.render.TextUtil;
import me.flawless.mod.gui.font.FontRenderers;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.ColorSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import me.flawless.mod.modules.settings.impl.StringSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;

import java.awt.*;

public class HUD extends Module {
    public static HUD INSTANCE;

    public final BooleanSetting armor = add(new BooleanSetting("Armor", true));
    public final BooleanSetting customFont = add(new BooleanSetting("CustomFont", true));
    public final ColorSetting color = add(new ColorSetting("Color", new Color(140, 146, 255)));
    public final ColorSetting pulse = add(new ColorSetting("Pulse", new Color(0, 0, 0)).injectBoolean(true));
    public final BooleanSetting waterMark = add(new BooleanSetting("WaterMark", true));
    public final StringSetting waterMarkString = add(new StringSetting("Title", FlawLess.LOG_NAME + " " + FlawLess.VERSION));
    public final SliderSetting offset = add(new SliderSetting("Offset", 8, 0, 100, -1));
    public final BooleanSetting sync = add(new BooleanSetting("InfoColorSync", true));
    public final BooleanSetting ctps = add(new BooleanSetting("CTPS", true));
    public final BooleanSetting fps = add(new BooleanSetting("FPS", true));
    public final BooleanSetting ping = add(new BooleanSetting("Ping", true));
    public final BooleanSetting tps = add(new BooleanSetting("TPS", true));
    private final SliderSetting pulseSpeed = add(new SliderSetting("Speed", 1, 0, 5, 0.1));
    private final SliderSetting pulseCounter = add(new SliderSetting("Counter", 10, 1, 50));
    public HUD() {
        super("HUD", Category.Client);
        INSTANCE = this;

    }

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (armor.getValue()) {
            FlawLess.GUI.armorHud.draw(drawContext, tickDelta, null);
        }
        if (waterMark.getValue()) {
            if (pulse.booleanValue) {
                TextUtil.drawStringPulse(drawContext, waterMarkString.getValue(), offset.getValueInt(), offset.getValueInt(), color.getValue(), pulse.getValue(), pulseSpeed.getValue(), pulseCounter.getValueInt(), customFont.getValue());
            } else {
                TextUtil.drawString(drawContext, waterMarkString.getValue(), offset.getValueInt(), offset.getValueInt(), color.getValue().getRGB(), customFont.getValue());
            }
        }
        int fontHeight = getHeight();
        int y = mc.getWindow().getScaledHeight() - fontHeight;
        if (mc.currentScreen instanceof ChatScreen) {
            y -= 15;
        }
        if (ctps.getValue()) {
            int x = getWidth("CTPS " + FlawLess.SERVER.getCurrentTPS());
            drawText(drawContext, "CTPS §f" + FlawLess.SERVER.getCurrentTPS(), mc.getWindow().getScaledWidth() - x, y);
            y -= fontHeight;
        }
        if (tps.getValue()) {
            int x = getWidth("TPS " + FlawLess.SERVER.getTPS());
            drawText(drawContext, "TPS §f" + FlawLess.SERVER.getTPS(), mc.getWindow().getScaledWidth() - x, y);
            y -= fontHeight;
        }
        if (fps.getValue()) {
            int x = getWidth("FPS " + "AAA");
            drawText(drawContext, "FPS §f" + FlawLess.FPS.getFps(), mc.getWindow().getScaledWidth() - x, y);
            y -= fontHeight;
        }
        if (ping.getValue()) {
            PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            String ping;
            if (playerListEntry == null) {
                ping = "Unknown";
            } else {
                ping = String.valueOf(playerListEntry.getLatency());
            }
            int x = getWidth("Ping " + ping);
            drawText(drawContext, "Ping §f" + ping, mc.getWindow().getScaledWidth() - x, y);
            y -= fontHeight;
        }
    }

    private int getWidth(String s) {
        if (customFont.getValue()) {
            return (int) FontRenderers.Arial.getWidth(s);
        }
        return mc.textRenderer.getWidth(s);
    }

    private int getHeight() {
        if (customFont.getValue()) {
            return (int) FontRenderers.Arial.getFontHeight();
        }
        return mc.textRenderer.fontHeight;
    }

    private void drawText(DrawContext drawContext, String s, int x, int y) {
        if (sync.getValue()) {
            ModuleList.INSTANCE.counter--;
            if (ModuleList.INSTANCE.lowerCase.getValue()) {
                s = s.toLowerCase();
            }
            TextUtil.drawString(drawContext, s, x, y, ModuleList.INSTANCE.getColor(ModuleList.INSTANCE.counter), customFont.getValue());
            return;
        }
        if (pulse.booleanValue) {
            TextUtil.drawStringPulse(drawContext, s, x, y, color.getValue(), pulse.getValue(), pulseSpeed.getValue(), pulseCounter.getValueInt(), customFont.getValue());
        } else {
            TextUtil.drawString(drawContext, s, x, y, color.getValue().getRGB(), customFont.getValue());
        }
    }
}
