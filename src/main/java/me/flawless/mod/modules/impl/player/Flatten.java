package me.flawless.mod.modules.impl.player;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.UpdateWalkingPlayerEvent;
import me.flawless.api.managers.RotateManager;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.entity.InventoryUtil;
import me.flawless.api.utils.math.Timer;
import me.flawless.api.utils.world.BlockPosX;
import me.flawless.api.utils.world.BlockUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.client.CombatSetting;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

public class Flatten extends Module {
	public static Flatten INSTANCE;
	private final BooleanSetting rotate =
			add(new BooleanSetting("Rotate", true));
	private final BooleanSetting checkMine =
			add(new BooleanSetting("DetectMining", true));
	private final BooleanSetting inventory =
			add(new BooleanSetting("InventorySwap", true));
	private final BooleanSetting usingPause =
			add(new BooleanSetting("UsingPause", true));
	private final SliderSetting blocksPer =
			add(new SliderSetting("BlocksPer", 2, 1, 8));
	private final SliderSetting delay =
			add(new SliderSetting("Delay", 100, 0, 1000));
	public Flatten() {
		super("Flatten", Category.Player);
		INSTANCE = this;
	}

	private final Timer timer = new Timer();
	int progress = 0;
	@EventHandler
	public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
		if (event.isPost()) return;
		progress = 0;
		if (usingPause.getValue() && mc.player.isUsingItem()) {
			return;
		}
		if (!mc.player.isOnGround()) {
			return;
		}
		if (!timer.passedMs(delay.getValueInt())) return;
		int oldSlot = mc.player.getInventory().selectedSlot;
		int block;
		if ((block = getBlock()) == -1) {
			return;
		}
		if (!EntityUtil.isInsideBlock()) return;

		BlockPos pos1 = new BlockPosX(mc.player.getX() + 0.6, mc.player.getY() + 0.5, mc.player.getZ() + 0.6).down();
		BlockPos pos2 = new BlockPosX(mc.player.getX() - 0.6, mc.player.getY() + 0.5, mc.player.getZ() + 0.6).down();
		BlockPos pos3 = new BlockPosX(mc.player.getX() + 0.6, mc.player.getY() + 0.5, mc.player.getZ() - 0.6).down();
		BlockPos pos4 = new BlockPosX(mc.player.getX() - 0.6, mc.player.getY() + 0.5, mc.player.getZ() - 0.6).down();

		if (!canPlace(pos1) && !canPlace(pos2) && !canPlace(pos3) && !canPlace(pos4)) {
			return;
		}
		doSwap(block);
        tryPlaceObsidian(pos1, rotate.getValue());
        tryPlaceObsidian(pos2, rotate.getValue());
        tryPlaceObsidian(pos3, rotate.getValue());
        tryPlaceObsidian(pos4, rotate.getValue());

		if (inventory.getValue()) {
			doSwap(block);
			EntityUtil.syncInventory();
		} else {
			doSwap(oldSlot);
		}
	}

	private boolean tryPlaceObsidian(BlockPos pos, boolean rotate) {
		if (canPlace(pos)) {
			if (checkMine.getValue() && BlockUtil.isMining(pos)) {
				return false;
			}
			Direction side;
			if ((side = BlockUtil.getPlaceSide(pos)) == null) return false;
			if (!(progress < blocksPer.getValue())) return false;
			progress++;
			if (rotate) {
				RotateManager.lastEvent.cancelRotate();
			}
			BlockUtil.placedPos.add(pos);
			BlockUtil.clickBlock(pos.offset(side), side.getOpposite(), rotate);
			timer.reset();
			return true;
		}
		return false;
	}

	private void doSwap(int slot) {
		if (inventory.getValue()) {
			InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
		} else {
			InventoryUtil.switchToSlot(slot);
		}
	}

	private boolean canPlace(BlockPos pos) {
		if (BlockUtil.getPlaceSide(pos) == null) {
			return false;
		}
		if (!BlockUtil.canReplace(pos)) {
			return false;
		}
		return !hasEntity(pos);
	}

	private boolean hasEntity(BlockPos pos) {
		for (Entity entity : mc.world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
			if (entity == mc.player) continue;
			if (!entity.isAlive() || entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof ExperienceBottleEntity || entity instanceof ArrowEntity || entity instanceof EndCrystalEntity || entity instanceof ArmorStandEntity && CombatSetting.INSTANCE.obsMode.getValue())
				continue;
			return true;
		}
		return false;
	}

	private int getBlock() {
		if (inventory.getValue()) {
				return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
		} else {
				return InventoryUtil.findBlock(Blocks.OBSIDIAN);
		}
	}
}