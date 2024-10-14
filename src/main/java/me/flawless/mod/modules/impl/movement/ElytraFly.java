package me.flawless.mod.modules.impl.movement;

import me.flawless.FlawLess;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.TravelEvent;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.math.Timer;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.EnumSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.MovementType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static me.flawless.api.utils.entity.MovementUtil.*;

public class ElytraFly extends Module {
    public static ElytraFly INSTANCE;
    public final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Control));
    private final BooleanSetting instantFly = add(new BooleanSetting("AutoStart", true));
    private final BooleanSetting boostTimer = add(new BooleanSetting("Timer", true));
    private final SliderSetting timeout = add(new SliderSetting("Timeout", 0.5F, 0.1F, 1F));
    public final SliderSetting upPitch = add(new SliderSetting("UpPitch", 0.0f, 0.0f, 90.0f, v -> mode.getValue() == Mode.Control));
    public final SliderSetting upFactor = add(new SliderSetting("UpFactor", 1.0f, 0.0f, 10.0f, v -> mode.getValue() == Mode.Control));
    public final SliderSetting downFactor = add(new SliderSetting("DownFactor", 1.0f, 0.0f, 10.0f, v -> mode.getValue() == Mode.Control));
    public final SliderSetting speed = add(new SliderSetting("Speed", 1.0f, 0.1f, 10.0f, v -> mode.getValue() == Mode.Control));
    public final BooleanSetting speedLimit = add(new BooleanSetting("SpeedLimit", true, v -> mode.getValue() == Mode.Control));
    public final SliderSetting maxSpeed = add(new SliderSetting("MaxSpeed", 2.5f, 0.1f, 10.0f, v -> speedLimit.getValue() && mode.getValue() == Mode.Control));
    public final BooleanSetting noDrag = add(new BooleanSetting("NoDrag", false, v -> mode.getValue() == Mode.Control));
    private final SliderSetting sneakDownSpeed = add(new SliderSetting("DownSpeed", 1.0F, 0.1F, 10.0F, v -> mode.getValue() == Mode.Control));
    private final SliderSetting boost = add(new SliderSetting("Boost", 1F, 0.1F, 4F, v -> mode.getValue() == Mode.Boost));
    private final Timer instantFlyTimer = new Timer();
    private final Timer strictTimer = new Timer();
    private boolean hasElytra = false;
    private boolean hasTouchedGround = false;
    public ElytraFly() {
        super("ElytraFly", Category.Movement);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            if (!mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
        hasElytra = false;
    }

    @Override
    public void onDisable() {
        FlawLess.TIMER.reset();
        hasElytra = false;
        if (mc.player != null) {
            if (!mc.player.isCreative()) mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().flying = false;
        }
    }

    public static void startFly() {
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }
    private void boost() {
        if (hasElytra) {
            if (!mc.player.isFallFlying()) {
               /* if (instantFly.getValue()) startFly();*/
                return;
            }
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            if (mc.options.forwardKey.isPressed()) {
                mc.player.addVelocity(-MathHelper.sin(yaw) * boost.getValueFloat() / 10, 0, MathHelper.cos(yaw) * boost.getValueFloat() / 10);
            }
           /* float yaw = mc.player.getYaw();
            Vec3d forward = Vec3d.fromPolar(0, yaw);
            Vec3d right = Vec3d.fromPolar(0, yaw + 90);
            double velX = 0;
            double velZ = 0;
            double s = boost.getValue();
            double speedValue = 0.01;
            if (mc.options.forwardKey.isPressed()) {
                velX += forward.x * s * speedValue;
                velZ += forward.z * s * speedValue;
            }
            if (mc.options.backKey.isPressed()) {
                velX -= forward.x * s * speedValue;
                velZ -= forward.z * s * speedValue;
            }

            if (mc.options.rightKey.isPressed()) {
                velX += right.x * s * speedValue;
                velZ += right.z * s * speedValue;
            }
            if (mc.options.leftKey.isPressed()) {
                velX -= right.x * s * speedValue;
                velZ -= right.z * s * speedValue;
            }
            double y = mc.player.getVelocity().y;
            ((IVec3d) mc.player.getVelocity()).setX(velX);
            ((IVec3d) mc.player.getVelocity()).setY(y);
            ((IVec3d) mc.player.getVelocity()).setZ(velZ);*/
        }
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        for (ItemStack is : mc.player.getArmorItems()) {
            if (is.getItem() instanceof ElytraItem) {
                hasElytra = true;
                break;
            } else {
                hasElytra = false;
            }
        }
        if (mc.player.isOnGround()) {
            hasTouchedGround = true;
        }
        if (strictTimer.passedMs(1500) && !strictTimer.passedMs(2000) || EntityUtil.isElytraFlying() && FlawLess.TIMER.get() == 0.3) {
            FlawLess.TIMER.reset();
        }
        if (!mc.player.isFallFlying()) {
            if (hasTouchedGround && boostTimer.getValue() && !mc.player.isOnGround()) {
                FlawLess.TIMER.set(0.3f);
            }
            if (!mc.player.isOnGround() && instantFly.getValue() && mc.player.getVelocity().getY() < 0D) {
                if (!instantFlyTimer.passedMs((long) (1000 * timeout.getValue()))) return;
                instantFlyTimer.reset();
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                hasTouchedGround = false;
                strictTimer.reset();
            }
        }
        if (mode.getValue() == Mode.Boost) {
            boost();
        }
    }

    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public final Vec3d getRotationVec(float tickDelta) {
        return this.getRotationVector(-upPitch.getValueFloat(), mc.player.getYaw(tickDelta));
    }

    @EventHandler
    public void onMove(TravelEvent event) {
        if (nullCheck() || !hasElytra || !mc.player.isFallFlying()) return;
        if (mode.getValue() != Mode.Control) return;
        Vec3d lookVec = getRotationVec(mc.getTickDelta());
        double lookDist = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
        double motionDist = Math.sqrt(getX() * getX() + getZ() * getZ());
        if (mc.options.sneakKey.isPressed()) {
            setY(-sneakDownSpeed.getValue());
        } else if (!mc.player.input.jumping) {
            setY(-0.00000000003D * downFactor.getValue());
        }
        if (mc.player.input.jumping) {
            if (motionDist > upFactor.getValue() / upFactor.getMaximum()) {
                double rawUpSpeed = motionDist * 0.01325D;
                setY(getY() + rawUpSpeed * 3.2D);
                setX(getX() - lookVec.x * rawUpSpeed / lookDist);
                setZ(getZ() - lookVec.z * rawUpSpeed / lookDist);
            } else {
                double[] dir = directionSpeed(speed.getValue());
                setX(dir[0]);
                setZ(dir[1]);
            }
        }
        if (lookDist > 0.0D) {
            setX(getX() + (lookVec.x / lookDist * motionDist - getX()) * 0.1D);
            setZ(getZ() + (lookVec.z / lookDist * motionDist - getZ()) * 0.1D);
        }
        if (!mc.player.input.jumping) {
            double[] dir = directionSpeed(speed.getValue());
            setX(dir[0]);
            setZ(dir[1]);
        }
        if (!noDrag.getValue()) {
            setY(getY() * 0.9900000095367432D);
            setX(getX() * 0.9800000190734863D);
            setZ(getZ() * 0.9900000095367432D);
        }
        double finalDist = Math.sqrt(getX() * getX() + getZ() * getZ());
        if (speedLimit.getValue() && finalDist > maxSpeed.getValue()) {
            setX(getX() * maxSpeed.getValue() / finalDist);
            setZ(getZ() * maxSpeed.getValue() / finalDist);
        }
        event.cancel();
        mc.player.move(MovementType.SELF, mc.player.getVelocity());
    }

    private double getX() {
        return getMotionX();
    }

    private void setX(double f) {
        setMotionX(f);
    }

    private double getY() {
        return getMotionY();
    }

    private void setY(double f) {
        setMotionY(f);
    }

    private double getZ() {
        return getMotionZ();
    }

    private void setZ(double f) {
        setMotionZ(f);
    }

    public enum Mode {
        Control, Boost
    }
}
