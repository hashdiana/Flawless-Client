package me.flawless.mod.modules.impl.movement;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.MoveEvent;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.combat.AutoAnchor;
import me.flawless.mod.modules.impl.combat.HoleKick;
import me.flawless.mod.modules.settings.impl.SliderSetting;

public class BlockStrafe extends Module {
    public static BlockStrafe INSTANCE;
    private final SliderSetting speed =
            add(new SliderSetting("Speed", 10, 0, 20, 1).setSuffix("%"));
    private final SliderSetting aSpeed =
            add(new SliderSetting("AnchorSpeed", 3, 0, 20, 1).setSuffix("%"));
    private final SliderSetting aForward =
            add(new SliderSetting("AnchorForward", 1, 0, 20, 0.25).setSuffix("%"));
    public BlockStrafe() {
        super("BlockStrafe", Category.Movement);
        INSTANCE = this;
    }

    @EventHandler
    public void onMove(MoveEvent event) {
        if (!EntityUtil.isInsideBlock()) return;
        if (HoleKick.isInWeb(mc.player)) return;
        double speed = AutoAnchor.INSTANCE.currentPos == null ? this.speed.getValue() : aSpeed.getValue();
        double moveSpeed = 0.2873 / 100 * speed;
        double n = mc.player.input.movementForward;
        double n2 = mc.player.input.movementSideways;
        double n3 = mc.player.getYaw();
        if (n == 0.0 && n2 == 0.0) {
            if (AutoAnchor.INSTANCE.currentPos == null) {
                event.setX(0.0);
                event.setZ(0.0);
            } else {
                moveSpeed = 0.2873 / 100 * aForward.getValue();
                event.setX(1 * moveSpeed * -Math.sin(Math.toRadians(n3)));
                event.setZ(1 * moveSpeed * Math.cos(Math.toRadians(n3)));
            }
            return;
        } else if (n != 0.0 && n2 != 0.0) {
            n *= Math.sin(0.7853981633974483);
            n2 *= Math.cos(0.7853981633974483);
        }
        event.setX((n * moveSpeed * -Math.sin(Math.toRadians(n3)) + n2 * moveSpeed * Math.cos(Math.toRadians(n3))));
        event.setZ((n * moveSpeed * Math.cos(Math.toRadians(n3)) - n2 * moveSpeed * -Math.sin(Math.toRadians(n3))));
    }
}
