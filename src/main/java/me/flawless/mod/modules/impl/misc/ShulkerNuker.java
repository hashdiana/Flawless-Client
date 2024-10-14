package me.flawless.mod.modules.impl.misc;

import me.flawless.api.utils.world.BlockUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.player.PacketMine;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.MathHelper;

public class ShulkerNuker extends Module {
    private final SliderSetting safeRange =
            add(new SliderSetting("SafeRange", 2, 0, 8, .1));
    private final SliderSetting range =
            add(new SliderSetting("Range", 5, 0, 8,.1));
    public ShulkerNuker() {
        super("ShulkerNuker", "Shulker nuker", Category.Misc);
    }

    @Override
    public void onUpdate() {
        if (PacketMine.breakPos != null && mc.world.getBlockState(PacketMine.breakPos).getBlock() instanceof ShulkerBoxBlock) {
            return;
        }
        if (getBlock() != null) {
           PacketMine.INSTANCE.mine(getBlock().getPos());
        }
    }

    private ShulkerBoxBlockEntity getBlock() {
        for (BlockEntity entity : BlockUtil.getTileEntities()) {
            if (entity instanceof ShulkerBoxBlockEntity shulker) {
                if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(shulker.getPos().toCenterPos())) <= safeRange.getValue()) {
                    continue;
                }
                if (MathHelper.sqrt((float) mc.player.squaredDistanceTo(shulker.getPos().toCenterPos())) <= range.getValue()) {
                    return shulker;
                }
            }
        }
        return null;
    }
}