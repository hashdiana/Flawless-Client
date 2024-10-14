package me.flawless.api.managers;

import me.flawless.FlawLess;
import me.flawless.api.events.Event;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.eventbus.EventPriority;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.events.impl.RotateEvent;
import me.flawless.api.events.impl.UpdateWalkingPlayerEvent;
import me.flawless.api.utils.Wrapper;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.math.Timer;
import me.flawless.asm.accessors.IPlayerMoveC2SPacket;
import me.flawless.mod.modules.impl.client.CombatSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotateManager implements Wrapper {
    public RotateManager() {
        FlawLess.EVENT_BUS.subscribe(this);
    }
    public float rotateYaw = 0;
    public float rotatePitch = 0;
    public static final Timer ROTATE_TIMER = new Timer();
    public static Vec3d directionVec = null;
    @EventHandler(priority =  EventPriority.HIGH + 1)
    public void onRotation(RotateEvent event) {
        if (mc.player == null) return;
        if (directionVec != null && !ROTATE_TIMER.passed((long) (CombatSetting.INSTANCE.rotateTime.getValue() * 1000))) {
            float[] angle = EntityUtil.getLegitRotations(directionVec);
            event.setYaw(angle[0]);
            event.setPitch(angle[1]);
            //event.setPitch(MathUtil.clamp(angle[1] + MathUtil.random(-3, 3), -90, 90));
        }
    }
    public static UpdateWalkingPlayerEvent lastEvent;
    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    private int ticksExisted;
    public float lastYaw = 0;
    public float lastPitch = 0;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || event.isCancelled()) return;
        if (!EntityUtil.rotating && CombatSetting.INSTANCE.rotateSync.getValue()) {
            if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
                if (!packet.changesLook()) return;
                float yaw = packet.getYaw(114514);
                float pitch = packet.getPitch(114514);
                if (yaw == mc.player.getYaw() && pitch == mc.player.getPitch()) {
                    ((IPlayerMoveC2SPacket) event.getPacket()).setYaw(rotateYaw);
                    ((IPlayerMoveC2SPacket) event.getPacket()).setPitch(rotatePitch);
                }
            }
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (!packet.changesLook()) return;
            lastYaw = packet.getYaw(lastYaw);
            lastPitch = packet.getPitch(lastPitch);
            setRotation(lastYaw, lastPitch, false);
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            lastYaw = packet.getYaw();
            lastPitch = packet.getPitch();
            setRotation(lastYaw, lastPitch, true);
        }
    }
    @EventHandler
    public void onUpdateWalkingPost(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == Event.Stage.Post)
            setRotation(lastYaw, lastPitch, false);
    }
    public void setRotation(float yaw, float pitch, boolean force) {
        if (mc.player == null) return;
        if (mc.player.age == ticksExisted && !force) {
            return;
        }

        ticksExisted = mc.player.age;
        prevPitch = renderPitch;

        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = getRenderYawOffset(yaw, prevRenderYawOffset);

        prevRotationYawHead = rotationYawHead;
        rotationYawHead = yaw;

        renderPitch = pitch;
    }

    public static float getRenderPitch() {
        return renderPitch;
    }

    public static float getRotationYawHead() {
        return rotationYawHead;
    }

    public static float getRenderYawOffset() {
        return renderYawOffset;
    }

    public static float getPrevPitch() {
        return prevPitch;
    }

    public static float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public static float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float result = offsetIn;
        float offset;

        double xDif = mc.player.getX() - mc.player.prevX;
        double zDif = mc.player.getZ() - mc.player.prevZ;

        if (xDif * xDif + zDif * zDif > 0.0025000002f) {
            offset = (float) MathHelper.atan2(zDif, xDif) * 57.295776f - 90.0f;
            float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
            if (95.0F < wrap && wrap < 265.0F) {
                result = offset - 180.0F;
            } else {
                result = offset;
            }
        }

        if (mc.player.handSwingProgress > 0.0F) {
            result = yaw;
        }

        result = offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3f;
        offset = MathHelper.wrapDegrees(yaw - result);

        if (offset < -75.0f) {
            offset = -75.0f;
        } else if (offset >= 75.0f) {
            offset = 75.0f;
        }

        result = yaw - offset;
        if (offset * offset > 2500.0f) {
            result += offset * 0.2f;
        }

        return result;
    }
}
