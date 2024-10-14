package me.flawless.mod.modules.impl.movement;

import me.flawless.mod.modules.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class VClip extends Module {
    public VClip() {
        super("Ejection", Category.Movement);
    }

    @Override
    public void onUpdate() {
        disable();
        double posX = mc.player.getX();
        double posY = Math.round(mc.player.getY());
        double posZ = mc.player.getZ();
        boolean onGround = mc.player.isOnGround();

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(posX,
                posY,
                posZ,
                onGround));

        double halfY = 2 / 400.0;
        posY -= halfY;

        mc.player.setPosition(posX, posY, posZ);
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(posX,
                posY,
                posZ,
                onGround));

        posY -= halfY * 300.0;
        mc.player.setPosition(posX, posY, posZ);
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(posX,
                posY,
                posZ,
                onGround));
    }
}
