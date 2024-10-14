package me.flawless.mod.modules.impl.client;

import me.flawless.mod.gui.font.FontRenderers;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;

public class Fonts extends Module {
    public static Fonts INSTANCE;
    public final BooleanSetting antiAliased = add(new BooleanSetting("AntiAliased", true));
    public final SliderSetting size = add(new SliderSetting("Size", 8, 1, 15, 1));
    public final SliderSetting yOffset = add(new SliderSetting("Offset", 0, -5, 15, 0.1));
    public Fonts() {
        super("Fonts", Category.Client);
        INSTANCE = this;
    }

    @Override
    public void enable() {
        try {
            FontRenderers.Arial = FontRenderers.createArial(size.getValueFloat());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean antiAliased() {
        return INSTANCE == null || INSTANCE.antiAliased.getValue();
    }
}
