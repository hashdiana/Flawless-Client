package me.flawless.mod.modules.impl.movement;

import me.flawless.api.utils.entity.MovementUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;

public class Clip extends Module {

    public static Clip INSTANCE;

    private final SliderSetting delay =
            add(new SliderSetting("delay", 5, 1, 10));
    private int packets;

    public Clip() {
        super("Clip", "Clips into blocks nearby to prevent crystal damage", Category.Movement);
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        packets = 0;
    }

    @Override
    public String getInfo() {
        return String.valueOf(packets);
    }

    @Override
    public void onUpdate() {
        if (MovementUtil.isMoving()) {
            packets = 0;
            return;
        }
        if (mc.player.age % delay.getValue() == 0) {
            mc.player.setPosition(mc.player.getX() + MathHelper.clamp(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.241, Math.floor(mc.player.getX()) + 0.759) - mc.player.getX(), -0.03, 0.03), mc.player.getY(), mc.player.getZ() + MathHelper.clamp(roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.241, Math.floor(mc.player.getZ()) + 0.759) - mc.player.getZ(), -0.03, 0.03));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(roundToClosest(mc.player.getX(), Math.floor(mc.player.getX()) + 0.23, Math.floor(mc.player.getX()) + 0.77), mc.player.getY(), roundToClosest(mc.player.getZ(), Math.floor(mc.player.getZ()) + 0.23, Math.floor(mc.player.getZ()) + 0.77), true));
            packets++;
        }
    }

    private double roundToClosest(double num, double low, double high) {
        double d1 = num - low;
        double d2 = high - num;

        if (d2 > d1) {
            return low;

        } else {
            return high;
        }
    }
}