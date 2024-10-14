package me.flawless.mod.modules.impl.player;

import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;

public class ForceSync extends Module {
    public static ForceSync INSTANCE;
    public ForceSync() {
        super("ForceSync", Category.Player);
        INSTANCE = this;
    }
    public final BooleanSetting onGround = this.add(new BooleanSetting("OnGround", true));
    public final BooleanSetting rotate = this.add(new BooleanSetting("Rotate", true));
    public final BooleanSetting position = this.add(new BooleanSetting("Position", true));
}
