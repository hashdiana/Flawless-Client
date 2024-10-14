package me.flawless.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import me.flawless.FlawLess;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.Render3DEvent;
import me.flawless.api.events.impl.RotateEvent;
import me.flawless.api.events.impl.UpdateWalkingPlayerEvent;
import me.flawless.api.managers.CommandManager;
import me.flawless.api.utils.combat.*;
import me.flawless.api.utils.math.damage.ExplosionUtil;
import me.flawless.api.utils.math.damage.MeteorExplosionUtil;
import me.flawless.api.utils.math.damage.OyveyExplosionUtil;
import me.flawless.api.utils.math.damage.ThunderExplosionUtil;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.entity.InventoryUtil;
import me.flawless.api.utils.math.Timer;
import me.flawless.api.utils.math.AnimateUtil;
import me.flawless.api.utils.render.ColorUtil;
import me.flawless.api.utils.render.Render3DUtil;
import me.flawless.api.utils.world.BlockUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.SwingSide;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.ColorSetting;
import me.flawless.mod.modules.settings.impl.EnumSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import static me.flawless.api.utils.world.BlockUtil.*;

public class AutoAnchor extends Module {
	public static AutoAnchor INSTANCE;
	public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
	//Assist
	private final BooleanSetting assist =
			add(new BooleanSetting("Assist", true, v -> page.getValue() == Page.Assist));
	private final BooleanSetting checkMine =
			add(new BooleanSetting("DetectMining", false, v -> page.getValue() == Page.Assist));
	private final SliderSetting assistRange =
			add(new SliderSetting("AssistRange", 5.0, 0.0, 6.0, 0.1, v -> page.getValue() == Page.Assist).setSuffix("m"));
	private final SliderSetting assistDamage =
			add(new SliderSetting("AssistDamage", 6.0, 0.0, 36.0, 0.1, v -> page.getValue() == Page.Assist).setSuffix("h"));
	private final SliderSetting delay =
			add(new SliderSetting("AssistDelay", 0.1, 0.0, 1, 0.01, v -> page.getValue() == Page.Assist).setSuffix("s"));

	//
	private final BooleanSetting light =
			add(new BooleanSetting("Light", true, v -> page.getValue() == Page.General));
	public final SliderSetting range =
			add(new SliderSetting("Range", 5.0, 0.0, 6.0, 0.1, v -> page.getValue() == Page.General).setSuffix("m"));
	public final SliderSetting targetRange =
			add(new SliderSetting("TargetRange", 8.0, 0.10, 6.0, 0.1, v -> page.getValue() == Page.General).setSuffix("m"));
	private final BooleanSetting inventorySwap =
			add(new BooleanSetting("InventorySwap", true, v -> page.getValue() == Page.General));
	private final BooleanSetting breakCrystal =
			add(new BooleanSetting("BreakCrystal", true, v -> page.getValue() == Page.General));
	private final BooleanSetting spam =
			add(new BooleanSetting("Spam", true, v -> page.getValue() == Page.General).setParent());
	private final BooleanSetting mineSpam =
			add(new BooleanSetting("OnlyMining", true, v -> page.getValue() == Page.General && spam.isOpen()));
	private final BooleanSetting spamPlace =
			add(new BooleanSetting("Fast", true, v -> page.getValue() == Page.General).setParent());
	private final BooleanSetting inSpam =
			add(new BooleanSetting("WhenSpamming", true, v -> page.getValue() == Page.General && spamPlace.isOpen()));
	private final BooleanSetting usingPause =
			add(new BooleanSetting("UsingPause", true, v -> page.getValue() == Page.General));
	private final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.Server, v -> page.getValue() == Page.General));
	private final SliderSetting placeDelay =
			add(new SliderSetting("Delay", 100, 0, 500, 1, v -> page.getValue() == Page.General).setSuffix("ms"));
	private final SliderSetting spamDelay =
			add(new SliderSetting("SpamDelay", 200, 0, 1000, 1, v -> page.getValue() == Page.General).setSuffix("ms"));
	private final SliderSetting calcDelay =
			add(new SliderSetting("CalcDelay", 200, 0, 1000, 1, v -> page.getValue() == Page.General).setSuffix("ms"));
	private final SliderSetting updateDelay =
			add(new SliderSetting("UpdateDelay", 50, 0, 1000, v -> page.getValue() == Page.General).setSuffix("ms"));
	private final BooleanSetting debug =
			add(new BooleanSetting("Debug", false, v -> page.getValue() == Page.General));

	private final BooleanSetting rotate =
			add(new BooleanSetting("Rotate", true, v -> page.getValue() == Page.Rotate).setParent());
	private final BooleanSetting yawStep =
			add(new BooleanSetting("YawStep", true, v -> rotate.isOpen() && page.getValue() == Page.Rotate));
	private final SliderSetting steps =
			add(new SliderSetting("Steps", 0.3f, 0.1f, 1.0f, 0.01, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting packet =
			add(new BooleanSetting("Packet", false, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting random =
			add(new BooleanSetting("Random", true, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting checkLook =
			add(new BooleanSetting("CheckLook", true, v -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
	private final SliderSetting fov =
			add(new SliderSetting("Fov", 5f, 0f, 30f, v -> rotate.isOpen() && yawStep.getValue() && checkLook.getValue() && page.getValue() == Page.Rotate));

	private final EnumSetting<AutoAnchor.CalcMode> calcType = add(new EnumSetting<>("DMGCalcType", AutoAnchor.CalcMode.FlawLess, v -> page.getValue() == Page.Calc));
	private final BooleanSetting noSuicide =
			add(new BooleanSetting("NoSuicide", true, v -> page.getValue() == Page.Calc));
	private final BooleanSetting terrainIgnore =
			add(new BooleanSetting("TerrainIgnore", true, v -> page.getValue() == Page.Calc));
	public final SliderSetting minDamage =
			add(new SliderSetting("Min", 4.0, 0.0, 36.0, 0.1, v -> page.getValue() == Page.Calc).setSuffix("dmg"));
	public final SliderSetting breakMin =
			add(new SliderSetting("ExplosionMin", 4.0, 0.0, 36.0, 0.1, v -> page.getValue() == Page.Calc).setSuffix("dmg"));
	public final SliderSetting headDamage =
			add(new SliderSetting("ForceHead", 7.0, 0.0, 36.0, 0.1, v -> page.getValue() == Page.Calc).setSuffix("dmg"));
	private final SliderSetting minPrefer =
			add(new SliderSetting("Prefer", 7.0, 0.0, 36.0, 0.1, v -> page.getValue() == Page.Calc).setSuffix("dmg"));
	private final SliderSetting maxSelfDamage =
			add(new SliderSetting("MaxSelf", 8.0, 0.0, 36.0, 0.1, v -> page.getValue() == Page.Calc).setSuffix("dmg"));
	public final SliderSetting predictTicks =
			add(new SliderSetting("Predict", 2, 0.0, 50, 1, v -> page.getValue() == Page.Calc).setSuffix("ticks"));

	private final EnumSetting<Aura.TargetESP> mode = add(new EnumSetting<>("TargetESP", Aura.TargetESP.Jello, v -> page.getValue() == Page.Render));
	private final ColorSetting color = add(new ColorSetting("TargetColor", new Color(255, 255, 255, 250), v -> page.getValue() == Page.Render));
	final BooleanSetting render =
			add(new BooleanSetting("Render", true, v -> page.getValue() == Page.Render));
	final BooleanSetting shrink =
			add(new BooleanSetting("Shrink", true, v -> page.getValue() == Page.Render && render.getValue()));
	final ColorSetting box =
			add(new ColorSetting("Box", new Color(255, 255, 255, 255), v -> page.getValue() == Page.Render && render.getValue()).injectBoolean(true));
	final SliderSetting lineWidth =
			add(new SliderSetting("LineWidth", 1.5d, 0.01d, 3d, 0.01, v -> page.getValue() == Page.Render && render.getValue()));
	final ColorSetting fill =
			add(new ColorSetting("Fill", new Color(255, 255, 255, 100), v -> page.getValue() == Page.Render && render.getValue()).injectBoolean(true));
	final SliderSetting sliderSpeed = add(new SliderSetting("SliderSpeed", 0.2, 0d, 1, 0.01, v -> page.getValue() == Page.Render && render.getValue()));
	final SliderSetting startFadeTime =
			add(new SliderSetting("StartFade", 0.3d, 0d, 2d, 0.01, v -> page.getValue() == Page.Render && render.getValue()).setSuffix("s"));
	final SliderSetting fadeSpeed =
			add(new SliderSetting("FadeSpeed", 0.2d, 0.01d, 1d, 0.01, v -> page.getValue() == Page.Render && render.getValue()));

	private final Timer updateTimer = new Timer();
	private final Timer delayTimer = new Timer();
	private final Timer calcTimer = new Timer();
	public Vec3d directionVec = null;
	private float lastYaw = 0;
	private float lastPitch = 0;
	public AutoAnchor() {
		super("AutoAnchor", Category.Combat);
		INSTANCE = this;
		FlawLess.EVENT_BUS.subscribe(new AnchorRender());
	}
	public PlayerEntity displayTarget;
	@Override
	public String getInfo() {
		if (displayTarget != null && currentPos != null) return displayTarget.getName().getString();
		return null;
	}
	private final ArrayList<BlockPos> chargeList = new ArrayList<>();
	public BlockPos currentPos;
	public BlockPos tempPos;
	public double lastDamage;
	final Timer noPosTimer = new Timer();
	static Vec3d placeVec3d;
	static Vec3d curVec3d;
	double fade = 0;
	@Override
	public void onRender3D(MatrixStack matrixStack, float partialTicks) {
		update();
		if (displayTarget != null && currentPos != null) {
			Aura.doRender(matrixStack, partialTicks, displayTarget, color.getValue(), mode.getValue());
		}
	}
	Random rand = new Random();
	@EventHandler
	public void onRotate(RotateEvent event) {
		if (currentPos != null && yawStep.getValue() && directionVec != null) {
			float[] newAngle = injectStep(EntityUtil.getLegitRotations(directionVec), steps.getValueFloat());
			lastYaw = newAngle[0];
			lastPitch = newAngle[1];
			if (random.getValue() && rand.nextBoolean() && rand.nextBoolean()) {
				lastPitch = Math.min(rand.nextFloat() * 2 + lastPitch, 90);
			}
			event.setYaw(lastYaw);
			event.setPitch(lastPitch);
		} else {
			lastYaw = FlawLess.ROTATE.lastYaw;
			lastPitch = FlawLess.ROTATE.lastPitch;
		}
	}

	@Override
	public void onDisable() {
		currentPos = null;
		tempPos = null;
	}

	@Override
	public void onEnable() {
		lastYaw = FlawLess.ROTATE.lastYaw;
		lastPitch = FlawLess.ROTATE.lastPitch;
	}

	@EventHandler
	public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
		update();
	}
	@Override
	public void onUpdate() {
		if (assist.getValue()) onAssist();
		update();
	}

	public void update() {
		if (nullCheck()) return;
		anchor();
		currentPos = tempPos;
	}
	public void anchor() {
		if (!updateTimer.passedMs((long) updateDelay.getValue())) return;
		int anchor = inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR) : InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
		int glowstone = inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.GLOWSTONE) : InventoryUtil.findBlock(Blocks.GLOWSTONE);
		int unBlock = inventorySwap.getValue() ? anchor : InventoryUtil.findUnBlock();
		int old = mc.player.getInventory().selectedSlot;
		if (anchor == -1) {
			tempPos = null;
			return;
		}
		if (glowstone == -1) {
			tempPos = null;
			return;
		}
		if (unBlock == -1) {
			tempPos = null;
			return;
		}
		if (mc.player.isSneaking()) {
			tempPos = null;
			return;
		}
		if (usingPause.getValue() && mc.player.isUsingItem()) {
			tempPos = null;
			return;
		}
		updateTimer.reset();
		PlayerAndPredict selfPredict = new PlayerAndPredict(mc.player);
		if (calcTimer.passed((long) (calcDelay.getValueFloat()))) {
			calcTimer.reset();
			tempPos = null;
			double placeDamage = minDamage.getValue();
			double breakDamage = breakMin.getValue();
			boolean anchorFound = false;
			java.util.List<PlayerEntity> enemies = CombatUtil.getEnemies(targetRange.getValue());
			ArrayList<PlayerAndPredict> list = new ArrayList<>();
			for (PlayerEntity player : enemies) {
				list.add(new PlayerAndPredict(player));
			}
			for (PlayerAndPredict pap : list) {
				BlockPos pos = EntityUtil.getEntityPos(pap.player, true).up(2);
				if (canPlace(pos, range.getValue(), breakCrystal.getValue()) || getBlock(pos) == Blocks.RESPAWN_ANCHOR && BlockUtil.getClickSideStrict(pos) != null) {
					double selfDamage;
					if ((selfDamage = getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > maxSelfDamage.getValue() || noSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
						continue;
					}
					double damage;
					if ((damage = getAnchorDamage(pos, pap.player, pap.predict)) > headDamage.getValueFloat()) {
						lastDamage = damage;
						displayTarget = pap.player;
						tempPos = pos;
						break;
					}
				}
			}
			if (tempPos == null) {
				for (BlockPos pos : getSphere(range.getValueFloat())) {
					for (PlayerAndPredict pap : list) {
						if (light.getValue()) {
							CombatUtil.modifyPos = pos;
							CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
							boolean skip = !AutoCrystal.canSee(pos.toCenterPos(), pap.predict.getPos());
							CombatUtil.modifyPos = null;
							if (skip) continue;
						}

						if (getBlock(pos) != Blocks.RESPAWN_ANCHOR) {
							if (anchorFound) continue;
							if (!canPlace(pos, range.getValue(), breakCrystal.getValue())) continue;

							CombatUtil.modifyPos = pos;
							CombatUtil.modifyBlockState = Blocks.OBSIDIAN.getDefaultState();
							boolean skip = BlockUtil.getClickSideStrict(pos) == null;
							CombatUtil.modifyPos = null;
							if (skip) continue;

							double damage = getAnchorDamage(pos, pap.player, pap.predict);
							if (damage >= placeDamage) {
								if (AutoCrystal.crystalPos == null || AutoCrystal.INSTANCE.isOff() || AutoCrystal.INSTANCE.lastDamage < damage) {
									double selfDamage;
									if ((selfDamage = getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > maxSelfDamage.getValue() || noSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
										continue;
									}
									lastDamage = damage;
									displayTarget = pap.player;
									placeDamage = damage;
									tempPos = pos;
								}
							}
						} else {
							double damage = getAnchorDamage(pos, pap.player, pap.predict);
							if (getClickSideStrict(pos) == null) continue;
							if (damage >= breakDamage) {
								if (damage >= minPrefer.getValue()) anchorFound = true;
								if (!anchorFound && damage < placeDamage) {
									continue;
								}
								if (AutoCrystal.crystalPos == null || AutoCrystal.INSTANCE.isOff() || AutoCrystal.INSTANCE.lastDamage < damage) {
									double selfDamage;
									if ((selfDamage = getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > maxSelfDamage.getValue() || noSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
										continue;
									}
									lastDamage = damage;
									displayTarget = pap.player;
									breakDamage = damage;
									tempPos = pos;
								}
							}
						}
					}
				}
			}
		}

		if (tempPos != null) {
			if (breakCrystal.getValue()) CombatUtil.attackCrystal(new BlockPos(tempPos), rotate.getValue(), false);
			boolean shouldSpam = this.spam.getValue() && (!mineSpam.getValue() || FlawLess.BREAK.isMining(tempPos));
			if (shouldSpam) {
				if (!delayTimer.passed((long) (spamDelay.getValueFloat()))) {
					return;
				}
				delayTimer.reset();
				if (canPlace(tempPos, range.getValue(), breakCrystal.getValue())) {
					placeBlock(tempPos, rotate.getValue(), anchor);
				}
				if (!chargeList.contains(tempPos)) {
					delayTimer.reset();
					clickBlock(tempPos, getClickSide(tempPos), rotate.getValue(), glowstone);
					chargeList.add(tempPos);
				}
				chargeList.remove(tempPos);
				clickBlock(tempPos, getClickSide(tempPos), rotate.getValue(), unBlock);
				if (spamPlace.getValue() && inSpam.getValue()) {
					CombatUtil.modifyPos = tempPos;
					CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
					placeBlock(tempPos, rotate.getValue(), anchor);
					CombatUtil.modifyPos = null;
				}
			} else {
				if (canPlace(tempPos, range.getValue(), breakCrystal.getValue())) {
					if (!delayTimer.passed((long) (placeDelay.getValueFloat()))) {
						return;
					}
					delayTimer.reset();
					placeBlock(tempPos, rotate.getValue(), anchor);
				} else if (getBlock(tempPos) == Blocks.RESPAWN_ANCHOR) {
					if (!chargeList.contains(tempPos)) {
						if (!delayTimer.passed((long) (placeDelay.getValueFloat()))) {
							return;
						}
						delayTimer.reset();
						clickBlock(tempPos, getClickSide(tempPos), rotate.getValue(), glowstone);
						chargeList.add(tempPos);
					} else {
						if (!delayTimer.passed((long) (placeDelay.getValueFloat()))) {
							return;
						}
						delayTimer.reset();
						chargeList.remove(tempPos);
						clickBlock(tempPos, getClickSide(tempPos), rotate.getValue(), unBlock);
						if (spamPlace.getValue()) {
							CombatUtil.modifyPos = tempPos;
							CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
							placeBlock(tempPos, rotate.getValue(), anchor);
							CombatUtil.modifyPos = null;
						}
					}
				}
			}
			if (!inventorySwap.getValue()) doSwap(old);
		}
	}

	public double getAnchorDamage(BlockPos anchorPos, PlayerEntity target, PlayerEntity predict) {
		if (terrainIgnore.getValue()) {
			CombatUtil.terrainIgnore = true;
		}
		double damage = 0;
		switch (calcType.getValue()) {
			case Meteor -> damage = MeteorExplosionUtil.anchorDamage(target, anchorPos, predict);
			case Thunder -> damage = ThunderExplosionUtil.anchorDamage(anchorPos, target, predict);
			case Oyvey -> damage = OyveyExplosionUtil.anchorDamage(anchorPos, target, predict);
			case FlawLess -> damage = ExplosionUtil.anchorDamage(anchorPos, target, predict);
		}
		CombatUtil.terrainIgnore = false;
		return damage;
	}
	public void placeBlock(BlockPos pos, boolean rotate, int slot) {
		if (airPlace()) {
			for (Direction i : Direction.values()) {
				if (mc.world.isAir(pos.offset(i))) {
					clickBlock(pos, i, rotate, slot);
					return;
				}
			}
		}
		Direction side = getPlaceSide(pos);
		if (side == null) return;
		//BlockUtil.placedPos.add(pos);
		clickBlock(pos.offset(side), side.getOpposite(), rotate, slot);
	}
	public void clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
		Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
		if (rotate) {
			if (!faceVector(directionVec)) return;
		}
		doSwap(slot);
		EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
		BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
		mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, getWorldActionId(mc.world)));
		if (inventorySwap.getValue()) {
			doSwap(slot);
		}
	}
	private void doSwap(int slot) {
		if (debug.getValue()) CommandManager.sendChatMessage(slot + "");
		if (inventorySwap.getValue()) {
			InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
		} else {
			InventoryUtil.switchToSlot(slot);
		}
	}
	public boolean faceVector(Vec3d directionVec) {
		if (!yawStep.getValue()) {
			EntityUtil.faceVector(directionVec);
			return true;
		} else {
			this.directionVec = directionVec;
			float[] angle = EntityUtil.getLegitRotations(directionVec);
			if (Math.abs(MathHelper.wrapDegrees(angle[0] - lastYaw)) < fov.getValueFloat() && Math.abs(MathHelper.wrapDegrees(angle[1] - lastPitch)) < fov.getValueFloat()) {
				if (packet.getValue()) EntityUtil.sendYawAndPitch(angle[0], angle[1]);
				return true;
			}
		}
		return !checkLook.getValue();
	}

	private float[] injectStep(float[] angle, float steps) {
		if (steps < 0.01f) steps = 0.01f;

		if (steps > 1) steps = 1;

		if (steps < 1 && angle != null) {
			float packetYaw = lastYaw;
			float diff = MathHelper.wrapDegrees(angle[0] - packetYaw);

			if (Math.abs(diff) > 90 * steps) {
				angle[0] = (packetYaw + (diff * ((90 * steps) / Math.abs(diff))));
			}

			float packetPitch = lastPitch;
			diff = angle[1] - packetPitch;
			if (Math.abs(diff) > 90 * steps) {
				angle[1] = (packetPitch + (diff * ((90 * steps) / Math.abs(diff))));
			}
		}

		return new float[]{
				angle[0],
				angle[1]
		};
	}

	public enum CalcMode {
		FlawLess,
		Oyvey,
		Meteor,
		Thunder,
	}

	public static class PlayerAndPredict {
		public final PlayerEntity player;
		public final PlayerEntity predict;
		public PlayerAndPredict(PlayerEntity player) {
			this.player = player;
			if (INSTANCE.predictTicks.getValueFloat() > 0) {
				predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {@Override public boolean isSpectator() {return false;} @Override public boolean isCreative() {return false;}};
				predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, INSTANCE.predictTicks.getValueInt(), true)));
				predict.setHealth(player.getHealth());
				predict.prevX = player.prevX;
				predict.prevZ = player.prevZ;
				predict.prevY = player.prevY;
				predict.setPose(player.getPose());
				predict.setOnGround(player.isOnGround());
				predict.getInventory().clone(player.getInventory());
				for (StatusEffectInstance se : player.getStatusEffects()) {
					predict.addStatusEffect(se);
				}
			} else {
				predict = player;
			}
		}
	}

	public enum Page {
		General,
		Calc,
		Rotate,
		Assist,
		Render,
	}

	public class AnchorRender {
		@EventHandler
		public void onRender3D(Render3DEvent event) {
			if (currentPos != null) {
				noPosTimer.reset();
				placeVec3d = currentPos.toCenterPos();
			}
			if (placeVec3d == null) {
				return;
			}
			if (fadeSpeed.getValue() >= 1) {
				fade = noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000)) ? 0 : 0.5;
			} else {
				fade = AnimateUtil.animate(fade, noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000)) ? 0 : 0.5, fadeSpeed.getValue() / 10);
			}
			if (fade == 0) {
				curVec3d = null;
				return;
			}
			if (curVec3d == null || sliderSpeed.getValue() >= 1) {
				curVec3d = placeVec3d;
			} else {
				curVec3d = new Vec3d(AnimateUtil.animate(curVec3d.x, placeVec3d.x, sliderSpeed.getValue() / 10),
						AnimateUtil.animate(curVec3d.y, placeVec3d.y, sliderSpeed.getValue() / 10),
						AnimateUtil.animate(curVec3d.z, placeVec3d.z, sliderSpeed.getValue() / 10));
			}

			if (render.getValue()) {
				Box cbox = new Box(curVec3d, curVec3d);
				if (shrink.getValue()) {
					cbox = cbox.expand(fade);
				} else {
					cbox = cbox.expand(0.5);
				}
				MatrixStack matrixStack = event.getMatrixStack();
				if (fill.booleanValue) {
					Render3DUtil.drawFill(matrixStack, cbox, ColorUtil.injectAlpha(fill.getValue(), (int) (fill.getValue().getAlpha() * fade * 2D)));
				}
				if (box.booleanValue) {
					Render3DUtil.drawBox(matrixStack, cbox, ColorUtil.injectAlpha(box.getValue(), (int) (box.getValue().getAlpha() * fade * 2D)), lineWidth.getValueFloat());
				}
			}
		}
	}

	private final Timer assistTimer = new Timer();
	BlockPos assistPos;

	public void onAssist() {
		assistPos = null;
		int anchor = inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR) : InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
		int glowstone = inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.GLOWSTONE) : InventoryUtil.findBlock(Blocks.GLOWSTONE);
		int old = mc.player.getInventory().selectedSlot;
		if (anchor == -1) {
			return;
		}
		if (glowstone == -1) {
			return;
		}
		if (mc.player.isSneaking()) {
			return;
		}
		if (usingPause.getValue() && mc.player.isUsingItem()) {
			return;
		}
		if (!assistTimer.passed((long) (delay.getValueFloat() * 1000))) {
			return;
		}
		assistTimer.reset();
		double bestDamage;
        ArrayList<AutoAnchor.PlayerAndPredict> list = new ArrayList<>();
		for (PlayerEntity player : CombatUtil.getEnemies(assistRange.getValue())) {
			list.add(new AutoAnchor.PlayerAndPredict(player));
		}

		bestDamage = assistDamage.getValue();
		for (AutoAnchor.PlayerAndPredict pap : list) {
			BlockPos pos = EntityUtil.getEntityPos(pap.player, true).up(2);
			if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR) {
				return;
			}
			if (BlockUtil.clientCanPlace(pos, false)) {
				double damage = getAnchorDamage(pos, pap.player, pap.predict);
				if (damage >= bestDamage) {
					bestDamage = damage;
					assistPos = pos;
				}
			}
			for (Direction i : Direction.values()) {
				if (i == Direction.UP || i == Direction.DOWN) continue;
				if (BlockUtil.clientCanPlace(pos.offset(i), false)) {
					double damage = getAnchorDamage(pos.offset(i), pap.player, pap.predict);
					if (damage >= bestDamage) {
						bestDamage = damage;
						assistPos = pos.offset(i);
					}
				}
			}
		}
		if (assistPos != null && BlockUtil.getPlaceSide(assistPos, range.getValue()) == null) {
			BlockPos placePos;
			if ((placePos = getHelper(assistPos)) != null) {
				doSwap(anchor);
				BlockUtil.placeBlock(placePos, rotate.getValue());
				doSwap(old);
			}
		}
	}

	public BlockPos getHelper(BlockPos pos) {
		for (Direction i : Direction.values()) {
			if (checkMine.getValue() && BlockUtil.isMining(pos.offset(i))) continue;
			if (!BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite(), true)) continue;
			if (BlockUtil.canPlace(pos.offset(i))) return pos.offset(i);
		}
		return null;
	}
}