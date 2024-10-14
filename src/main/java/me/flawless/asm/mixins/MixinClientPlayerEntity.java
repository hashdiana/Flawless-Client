package me.flawless.asm.mixins;

import com.mojang.authlib.GameProfile;
import me.flawless.FlawLess;
import me.flawless.api.events.Event;
import me.flawless.api.events.impl.MoveEvent;
import me.flawless.api.events.impl.RotateEvent;
import me.flawless.api.events.impl.UpdateWalkingPlayerEvent;
import me.flawless.api.managers.RotateManager;
import me.flawless.mod.modules.impl.client.CombatSetting;
import me.flawless.mod.modules.impl.client.GameSetting;
import me.flawless.mod.modules.impl.player.ForceSync;
import me.flawless.mod.modules.impl.player.PacketControl;
import me.flawless.mod.modules.impl.movement.NoSlow;
import me.flawless.mod.modules.impl.movement.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {

	public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "pushOutOfBlocks",
			at = @At("HEAD"),
			cancellable = true)
	private void onPushOutOfBlocksHook(double x, double d, CallbackInfo info) {
		if (Velocity.INSTANCE.isOn() && Velocity.INSTANCE.blockPush.getValue()) {
			info.cancel();
		}
	}

	@Redirect(method = "tickMovement",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"),
			require = 0)
	private boolean tickMovementHook(ClientPlayerEntity player) {
		if (NoSlow.INSTANCE.noSlow())
			return false;
		return player.isUsingItem();
	}

	@Redirect(method = "updateNausea", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
	private Screen updateNauseaGetCurrentScreenProxy(MinecraftClient client) {
		if (GameSetting.INSTANCE.portalGui()) return null;
		return client.currentScreen;
	}

	@Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
	public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
		MoveEvent event = new MoveEvent(movement.x, movement.y, movement.z);
		FlawLess.EVENT_BUS.post(event);
		ci.cancel();
		if (!event.isCancelled()) {
			super.move(movementType, new Vec3d(event.getX(), event.getY(), event.getZ()));
		}
	}

	@Shadow private void sendSprintingPacket() {}
	@Shadow @Final private List<ClientPlayerTickable> tickables;
	@Shadow	public Input input;
	@Shadow private boolean autoJumpEnabled;
	@Final @Shadow public ClientPlayNetworkHandler networkHandler;
	@Shadow private double lastX;
	@Shadow private double lastBaseY;
	@Shadow private double lastZ;
	@Shadow private float lastYaw;
	@Shadow private float lastPitch;
	@Shadow private boolean lastOnGround;
	@Shadow private boolean lastSneaking;
	@Final @Shadow protected MinecraftClient client;
	@Shadow private int ticksSinceLastPositionPacketSent;
	@Shadow	private void sendMovementPackets() {}
	@Shadow protected boolean isCamera() {return false;}
	@Shadow public abstract float getPitch(float tickDelta);

	@Inject(method = "sendMovementPackets", at = {@At("HEAD")}, cancellable = true)
	private void sendMovementPacketsHook(CallbackInfo ci) {
		ci.cancel();
		UpdateWalkingPlayerEvent updateEvent = new UpdateWalkingPlayerEvent(Event.Stage.Pre);
		RotateManager.lastEvent = updateEvent;
		FlawLess.EVENT_BUS.post(updateEvent);
		this.sendSprintingPacket();
		boolean bl = this.isSneaking();
		if (bl != this.lastSneaking) {
			ClientCommandC2SPacket.Mode mode = bl ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
			this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode));
			this.lastSneaking = bl;
		}

		if (this.isCamera()) {
			double d = this.getX() - this.lastX;
			double e = this.getY() - this.lastBaseY;
			double f = this.getZ() - this.lastZ;

			float yaw = this.getYaw();
			float pitch = this.getPitch();
			RotateEvent rotateEvent = new RotateEvent(yaw, pitch);
			FlawLess.EVENT_BUS.post(rotateEvent);
			if (rotateEvent.isModified() && CombatSetting.INSTANCE.randomPitch.getValue() && new Random().nextBoolean() && new Random().nextBoolean()) {
				rotateEvent.setPitch(Math.min(new Random().nextFloat() * 2 + rotateEvent.getPitch(), 90));
			}
			yaw = rotateEvent.getYaw();
			pitch = rotateEvent.getPitch();
			FlawLess.ROTATE.rotateYaw = yaw;
			FlawLess.ROTATE.rotatePitch = pitch;

			double g = yaw - FlawLess.ROTATE.lastYaw;//this.lastYaw;
			double h = pitch - FlawLess.ROTATE.lastPitch;//this.lastPitch;
			++this.ticksSinceLastPositionPacketSent;
			boolean bl2 = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20 || (ForceSync.INSTANCE.isOn() && ForceSync.INSTANCE.position.getValue());;
			boolean bl3 = (g != 0.0 || h != 0.0 || (ForceSync.INSTANCE.isOn() && ForceSync.INSTANCE.rotate.getValue())) && !updateEvent.isCancelRotate() ;
			if (PacketControl.INSTANCE.isOn()) {
				bl3 = PacketControl.INSTANCE.full;
			}
			if (this.hasVehicle()) {
				Vec3d vec3d = this.getVelocity();
				this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(vec3d.x, -999.0, vec3d.z, yaw, pitch, this.isOnGround()));
				bl2 = false;
			} else if (bl2 && bl3) {
				this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(this.getX(), this.getY(), this.getZ(), yaw, pitch, this.isOnGround()));
			} else if (bl2) {
				this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.getX(), this.getY(), this.getZ(), this.isOnGround()));
			} else if (bl3) {
				this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, this.isOnGround()));
			} else if (this.lastOnGround != this.isOnGround() || ForceSync.INSTANCE.isOn() && ForceSync.INSTANCE.onGround.getValue()) {
				this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(this.isOnGround()));
			}

			if (bl2) {
				this.lastX = this.getX();
				this.lastBaseY = this.getY();
				this.lastZ = this.getZ();
				this.ticksSinceLastPositionPacketSent = 0;
			}

			if (bl3) {
				this.lastYaw = yaw;
				this.lastPitch = pitch;
			}

			this.lastOnGround = this.isOnGround();
			this.autoJumpEnabled = this.client.options.getAutoJump().getValue();
		}
		FlawLess.EVENT_BUS.post(new UpdateWalkingPlayerEvent(Event.Stage.Post));
	}

	@Inject(method = "tick", at = {@At("HEAD")}, cancellable = true)
	private void tickHook(CallbackInfo ci) {
		ci.cancel();
		if (this.getWorld().isPosLoaded(this.getBlockX(), this.getBlockZ())) {
			super.tick();
			if (this.hasVehicle()) {
				UpdateWalkingPlayerEvent updateEvent = new UpdateWalkingPlayerEvent(Event.Stage.Pre);
				RotateManager.lastEvent = updateEvent;
				FlawLess.EVENT_BUS.post(updateEvent);
				if (!updateEvent.isCancelRotate()) {
					float yaw = this.getYaw();
					float pitch = this.getPitch();
					RotateEvent rotateEvent = new RotateEvent(yaw, pitch);
					FlawLess.EVENT_BUS.post(rotateEvent);
					if (rotateEvent.isModified() && CombatSetting.INSTANCE.randomPitch.getValue() && new Random().nextBoolean() && new Random().nextBoolean()) {
						rotateEvent.setPitch(Math.min(new Random().nextFloat() * 2 + rotateEvent.getPitch(), 90));
					}
					yaw = rotateEvent.getYaw();
					pitch = rotateEvent.getPitch();
					FlawLess.ROTATE.rotateYaw = yaw;
					FlawLess.ROTATE.rotatePitch = pitch;

					this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, this.isOnGround()));
				}
				FlawLess.EVENT_BUS.post(new UpdateWalkingPlayerEvent(Event.Stage.Post));
				this.networkHandler.sendPacket(new PlayerInputC2SPacket(this.sidewaysSpeed, this.forwardSpeed, this.input.jumping, this.input.sneaking));
				Entity entity = this.getRootVehicle();
				if (entity != this && entity.isLogicalSideForUpdatingMovement()) {
					this.networkHandler.sendPacket(new VehicleMoveC2SPacket(entity));
					this.sendSprintingPacket();
				}
			} else {
				this.sendMovementPackets();
			}

            for (ClientPlayerTickable clientPlayerTickable : this.tickables) {
                clientPlayerTickable.tick();
            }
		}
	}
}
