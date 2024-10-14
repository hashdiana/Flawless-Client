package me.flawless.mod.modules.impl.movement;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.entity.MovementUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class BurrowStuck extends Module {
    public BurrowStuck() {
        super("BurrowStuck", Category.Movement);
    }

    private final BooleanSetting obsidian = add(new BooleanSetting("Obsidian", true));

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if (nullCheck()) return;
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            if (!mc.player.isOnGround() || MovementUtil.isMoving()) return;
            if (!insideBlock()) return;
            event.cancel();
        }
    }

    public boolean insideBlock() {
        BlockPos playerBlockPos = EntityUtil.getPlayerPos(true);
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    BlockPos offsetPos = playerBlockPos.add(xOffset, yOffset, zOffset);
                    if (mc.world.getBlockState(offsetPos).getBlock() == Blocks.BEDROCK || obsidian.getValue() && mc.world.getBlockState(offsetPos).getBlock() == Blocks.OBSIDIAN) {
                        if (mc.player.getBoundingBox().intersects(new Box(offsetPos))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
