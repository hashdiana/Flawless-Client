package me.flawless.mod.modules.impl.client;

import me.flawless.api.utils.math.FadeUtils;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.*;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;

import java.awt.*;
import java.util.HashMap;

public class ChatSetting extends Module {
    public static ChatSetting INSTANCE;
    public enum Page {
        Notification,
        ChatHud
    }
    public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.Notification));
    public final StringSetting hackName = add(new StringSetting("Notification", "<flaw>", v -> page.getValue() == Page.Notification));
    public final ColorSetting color = add(new ColorSetting("Color", new Color(140, 146, 255), v -> page.getValue() == Page.Notification));
    public final ColorSetting pulse = add(new ColorSetting("Pulse", new Color(0, 0, 0), v -> page.getValue() == Page.Notification).injectBoolean(true));
    public final SliderSetting pulseSpeed = add(new SliderSetting("Speed", 1, 0, 5, 0.1, v -> page.getValue() == Page.Notification && pulse.booleanValue));
    public final SliderSetting pulseCounter = add(new SliderSetting("Counter", 10, 1, 50, v -> page.getValue() == Page.Notification && pulse.booleanValue));
    public final EnumSetting<Style> messageStyle = add(new EnumSetting<>("Style", Style.Mio, v -> page.getValue() == Page.Notification));
    public final BooleanSetting toggle = add(new BooleanSetting("ModuleToggle", true, v -> page.getValue() == Page.Notification).setParent());
    public final BooleanSetting onlyOne = add(new BooleanSetting("OnlyOne", false, v -> page.getValue() == Page.Notification && toggle.isOpen()));

    public final BooleanSetting keepHistory = add(new BooleanSetting("KeepHistory", true, v -> page.getValue() == Page.ChatHud));
    public final BooleanSetting infiniteChat = add(new BooleanSetting("InfiniteChat", true, v -> page.getValue() == Page.ChatHud));
    public final SliderSetting animateTime = add(new SliderSetting("AnimTime", 300, 0, 1000, v -> page.getValue() == Page.ChatHud));
    public final SliderSetting animateOffset = add(new SliderSetting("AnimOffset", -40, -200, 100, v -> page.getValue() == Page.ChatHud));
    public final EnumSetting<FadeUtils.Quad> animQuad = add(new EnumSetting<>("Quad", FadeUtils.Quad.In2, v -> page.getValue() == Page.ChatHud));
    public final BooleanSetting fade = add(new BooleanSetting("Fade", true, v -> page.getValue() == Page.ChatHud));
    public final BooleanSetting yAnim = add(new BooleanSetting("YAnim", false, v -> page.getValue() == Page.ChatHud));
    public final SliderSetting fadeTime = add(new SliderSetting("FadeTime", 300, 0, 1000, v -> page.getValue() == Page.ChatHud));
    public final BooleanSetting inputBoxAnim = add(new BooleanSetting("InputBoxAnim", true, v -> page.getValue() == Page.ChatHud));
    public final BooleanSetting hideIndicator = add(new BooleanSetting("HideIndicator", true, v -> page.getValue() == Page.ChatHud));
    public ChatSetting() {
        super("ChatSetting", Category.Client);
        INSTANCE = this;
    }

    public enum Style {
        Mio,
        Debug,
        Flaw,
        Basic,
        Future,
        Earth,
        MoonGod,
        IDK,
        None
    }
    public static final HashMap<OrderedText, StringVisitable> chatMessage = new HashMap<>();
    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }
}
