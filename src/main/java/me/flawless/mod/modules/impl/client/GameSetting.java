package me.flawless.mod.modules.impl.client;

import me.flawless.FlawLess;
import me.flawless.api.utils.math.AnimateUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.*;

import java.awt.*;

public class GameSetting extends Module {
    public static GameSetting INSTANCE;
    public final StringSetting windowTitle = add(new StringSetting("WindowTitle", FlawLess.LOG_NAME));
    public final BooleanSetting titleOverride = add(new BooleanSetting("TitleOverride", true));
    public final BooleanSetting caughtException = add(new BooleanSetting("CaughtException", true).setParent());
    public final BooleanSetting log = add(new BooleanSetting("Log", true, v -> caughtException.isOpen()));
    private final BooleanSetting fontShadow = add(new BooleanSetting("FontShadow", true));
    public final BooleanSetting guiBackground = add(new BooleanSetting("GuiBackground", true).setParent());
    public final BooleanSetting blur = add(new BooleanSetting("Blur", true));
    public final BooleanSetting titleFix = add(new BooleanSetting("TitleFix", true));
    private final BooleanSetting portalGui = add(new BooleanSetting("PortalGui", true));
    public final EnumSetting<AnimateUtil.AnimMode> animMode = add(new EnumSetting<>("AnimMode", AnimateUtil.AnimMode.Mio));
    private final BooleanSetting hotbar = add(new BooleanSetting("HotbarAnim", true));
    public final SliderSetting hotbarSpeed = add(new SliderSetting("HotbarSpeed", 0.2, 0.01, 1, 0.01));
    public final ColorSetting customBackground = add(new ColorSetting("CustomBackground", new Color(0, 0, 0,0)).injectBoolean(true));
    public final ColorSetting endColor = add(new ColorSetting("End", new Color(255, 0, 0, 150), v -> customBackground.booleanValue));
    public GameSetting() {
        super("GameSetting", Category.Client);
        INSTANCE = this;
    }
    public boolean portalGui() {
        return isOn() && portalGui.getValue();
    }
    public boolean hotbar() {
        return isOn() && hotbar.getValue();
    }
    public static boolean shadow() {
        return INSTANCE == null || INSTANCE.fontShadow.getValue();
    }
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
