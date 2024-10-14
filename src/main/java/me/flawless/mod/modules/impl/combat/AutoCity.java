package me.flawless.mod.modules.impl.combat;

import me.flawless.api.utils.combat.CombatUtil;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.world.BlockPosX;
import me.flawless.api.utils.world.BlockUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.client.CombatSetting;
import me.flawless.mod.modules.impl.player.PacketMine;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;

import static me.flawless.api.utils.world.BlockUtil.getBlock;

public class AutoCity extends Module {
	public static AutoCity INSTANCE;
	private final BooleanSetting burrow = add(new BooleanSetting("Burrow", true));
	private final BooleanSetting surround = add(new BooleanSetting("Surround", true));
	private final BooleanSetting lowVersion = add(new BooleanSetting("1.12", false));
	public final SliderSetting targetRange =
			add(new SliderSetting("TargetRange", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));
	public final SliderSetting range =
			add(new SliderSetting("Range", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));
	public AutoCity() {
		super("AutoCity", Category.Combat);
		INSTANCE = this;
	}

	@Override
	public void onUpdate() {
		PlayerEntity player = CombatUtil.getClosestEnemy(targetRange.getValue());
		if (player == null) return;
		doBreak(player);
	}

	private void doBreak(PlayerEntity player) {
		BlockPos pos = EntityUtil.getEntityPos(player, true);
		if (burrow.getValue()) {
			double[] yOffset = new double[]{-0.8, 0.5, 1.1};
			double[] xzOffset = new double[]{0.3, -0.3, 0};
			for (PlayerEntity entity : CombatUtil.getEnemies(targetRange.getValue())) {
				for (double y : yOffset) {
					for (double x : xzOffset) {
						for (double z : xzOffset) {
							BlockPos offsetPos = new BlockPosX(entity.getX() + x, entity.getY() + y, entity.getZ() + z);
							if (canBreak(offsetPos) && offsetPos.equals(PacketMine.breakPos)) {
								return;
							}
						}
					}
				}
			}

			yOffset = new double[]{0.5, 1.1};
			for (double y : yOffset) {
				for (double offset : xzOffset) {
					BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
					if (canBreak(offsetPos)) {
						PacketMine.INSTANCE.mine(offsetPos);
						return;
					}
				}
			}
			for (double y : yOffset) {
				for (double offset : xzOffset) {
					for (double offset2 : xzOffset) {
						BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
						if (canBreak(offsetPos)) {
							PacketMine.INSTANCE.mine(offsetPos);
							return;
						}
					}
				}
			}
		}
		if (surround.getValue()) {
			if (!lowVersion.getValue()) {
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
						continue;
					}
					if ((BlockUtil.isAir(pos.offset(i)) || pos.offset(i).equals(PacketMine.breakPos)) && canPlaceCrystal(pos.offset(i), false)) {
						return;
					}
				}
				ArrayList<BlockPos> list = new ArrayList<>();
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
						continue;
					}
					if (!PacketMine.godBlocks.contains(getBlock(pos.offset(i))) && !(getBlock(pos.offset(i)) instanceof BedBlock) && !(getBlock(pos.offset(i)) instanceof CobwebBlock) && canPlaceCrystal(pos.offset(i), true)) {
						list.add(pos.offset(i));
					}
				}
				if (!list.isEmpty()) {
					//System.out.println("found");
					PacketMine.INSTANCE.mine(list.stream().min(Comparator.comparingDouble((E) -> E.getSquaredDistance(mc.player.getEyePos()))).get());
				} else {
					for (Direction i : Direction.values()) {
						if (i == Direction.UP || i == Direction.DOWN) continue;
						if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
							continue;
						}
						if (!PacketMine.godBlocks.contains(getBlock(pos.offset(i))) && !(getBlock(pos.offset(i)) instanceof BedBlock) && !(getBlock(pos.offset(i)) instanceof CobwebBlock) && canPlaceCrystal(pos.offset(i), false)) {
							list.add(pos.offset(i));
						}
					}
					if (!list.isEmpty()) {
						//System.out.println("found");
						PacketMine.INSTANCE.mine(list.stream().min(Comparator.comparingDouble((E) -> E.getSquaredDistance(mc.player.getEyePos()))).get());
					}
				}

			} else {

				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (mc.player.getEyePos().distanceTo(pos.offset(i).toCenterPos()) > range.getValue()) {
						continue;
					}
					if ((BlockUtil.isAir(pos.offset(i)) && BlockUtil.isAir(pos.offset(i).up())) && canPlaceCrystal(pos.offset(i), false)) {
						return;
					}
				}

				ArrayList<BlockPos> list = new ArrayList<>();
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
						continue;
					}
					if (canCrystal(pos.offset(i))) {
						list.add(pos.offset(i));
					}
				}

				int max = 0;
				BlockPos minePos = null;
				for (BlockPos cPos : list) {
					if (getAir(cPos) >= max) {
						max = getAir(cPos);
						minePos = cPos;
					}
				}
				if (minePos != null) {
					doMine(minePos);
				}
			}
		}
		if (PacketMine.breakPos == null) {
			if (burrow.getValue()) {
				double[] yOffset;
				double[] xzOffset = new double[]{0.3, -0.3, 0};

				yOffset = new double[]{0.5, 1.1};
				for (double y : yOffset) {
					for (double offset : xzOffset) {
						BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
						if (isObsidian(offsetPos)) {
							PacketMine.INSTANCE.mine(offsetPos);
							return;
						}
					}
				}
				for (double y : yOffset) {
					for (double offset : xzOffset) {
						for (double offset2 : xzOffset) {
							BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
							if (isObsidian(offsetPos)) {
								PacketMine.INSTANCE.mine(offsetPos);
								return;
							}
						}
					}
				}
			}
		}
	}

	private void doMine(BlockPos pos) {
		if (canBreak(pos)) {
			PacketMine.INSTANCE.mine(pos);
		} else if (canBreak(pos.up())) {
			PacketMine.INSTANCE.mine(pos.up());
		}
	}
	private static boolean canCrystal(BlockPos pos) {
		if (PacketMine.godBlocks.contains(getBlock(pos)) || getBlock(pos) instanceof BedBlock || getBlock(pos) instanceof CobwebBlock || !canPlaceCrystal(pos, true) || BlockUtil.getClickSideStrict(pos) == null) {
			return false;
		}
		if (PacketMine.godBlocks.contains(getBlock(pos.up())) || getBlock(pos.up()) instanceof BedBlock || getBlock(pos.up()) instanceof CobwebBlock || BlockUtil.getClickSideStrict(pos.up()) == null) {
			return false;
		}
		return true;
	}
	private int getAir(BlockPos pos) {
		int value = 0;
		if (!canBreak(pos)) {
			value++;
		}
		if (!canBreak(pos.up())) {
			value++;
		}

		return value;
	}
	public static boolean canPlaceCrystal(BlockPos pos, boolean block) {
		BlockPos obsPos = pos.down();
		BlockPos boost = obsPos.up();
		return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN || !block)
				&& !BlockUtil.hasEntityBlockCrystal(boost, true, true)
				&& !BlockUtil.hasEntityBlockCrystal(boost.up(), true, true)
				&& (!CombatSetting.INSTANCE.lowVersion.getValue() || BlockUtil.isAir(boost.up()));
	}

	private boolean isObsidian(BlockPos pos) {
		return mc.player.getEyePos().distanceTo(pos.toCenterPos()) <= range.getValue() && (getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.ENDER_CHEST || getBlock(pos) == Blocks.NETHERITE_BLOCK) && BlockUtil.getClickSideStrict(pos) != null;
	}
	private boolean canBreak(BlockPos pos) {
		return isObsidian(pos) && (!pos.equals(PacketMine.secondPos) || !(mc.player.getMainHandStack().getItem() instanceof PickaxeItem));
	}
}