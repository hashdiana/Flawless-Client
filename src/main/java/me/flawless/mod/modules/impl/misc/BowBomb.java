package me.flawless.mod.modules.impl.misc;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.events.impl.RotateEvent;
import me.flawless.api.managers.CommandManager;
import me.flawless.api.utils.combat.CombatUtil;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.math.Timer;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.EnumSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class BowBomb extends Module {

    private final Timer delayTimer = new Timer();
    private final BooleanSetting aim = add(new BooleanSetting("Aim", true));
    private final SliderSetting predictTicks = add(new SliderSetting("PredictTicks", 3,0, 10, 0.1));
    private final BooleanSetting rotation = add(new BooleanSetting("Rotation", false));
    private final SliderSetting spoofs = add(new SliderSetting("Spoofs", 50, 0, 200, 1));
    private final EnumSetting<exploitEn> exploit = add(new EnumSetting<>("Exploit", exploitEn.Strong));
    private final BooleanSetting minimize = add(new BooleanSetting("Minimize", false));
    private final SliderSetting delay = add(new SliderSetting("Delay", 5f, 0f, 10f).setSuffix("s"));
    private final SliderSetting activeTime = this.add(new SliderSetting("ActiveTime", 0.4f, 0f, 3f, 0.01).setSuffix("s"));
    private final BooleanSetting message = add(new BooleanSetting("Message", true));
    private final Random random = new Random();
    public BowBomb() {
        super("BowBomb", "exploit", Category.Misc);
    }
    private final Timer activeTimer = new Timer();

    @EventHandler
    public void onMotion(RotateEvent event) {
        if (!mc.player.isUsingItem() || mc.player.getActiveItem().getItem() != Items.BOW || !aim.getValue()) {
            return;
        }
        PlayerEntity target = getTarget();
        if (target == null) return;
        Vec3d headPos = target.getEyePos().add(CombatUtil.getMotionVec(target, predictTicks.getValueFloat(), true));
        float[] rotations = EntityUtil.getLegitRotations(headPos);
        event.setRotation(rotations[0], rotations[1]);
    }

    private PlayerEntity getTarget() {
        PlayerEntity target = null;
        double distance = 100000;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (Math.abs(player.getY() - mc.player.getY()) > 4) continue;
            if (!CombatUtil.isValid(player, distance)) continue;
            target = player;
            distance = mc.player.distanceTo(player);
        }
        return target;
    }

    boolean active = false;
    @Override
    public void onUpdate() {
        if (!mc.player.isUsingItem() || mc.player.getActiveItem().getItem() != Items.BOW) {
            activeTimer.reset();
            active = false;
        } else {
            active = true;
        }
    }

    @Override
    public void onDisable() {
        send = false;
    }

    public static boolean send = false;
    @EventHandler
    protected void onPacketSend(PacketEvent.Send event) {
        if (nullCheck() || !delayTimer.passedMs((long) (delay.getValue() * 1000)) || !activeTimer.passedMs((long) (activeTime.getValue() * 1000)) || !active) return;
        if (event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) {
            send = true;
            if (message.getValue()) CommandManager.sendChatMessage("§rBomb");
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));

            if (exploit.getValue() == exploitEn.Fast) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), minimize.getValue() ? mc.player.getY() : mc.player.getY() - 1e-10, mc.player.getZ(), true);
                    spoof(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), false);
                }
            }
            if (exploit.getValue() == exploitEn.Strong) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), false);
                    spoof(mc.player.getX(), minimize.getValue() ? mc.player.getY() : mc.player.getY() - 1e-10, mc.player.getZ(), true);
                }
            }
            if (exploit.getValue() == exploitEn.Phobos) {
                for (int i = 0; i < getRuns(); i++) {
                    spoof(mc.player.getX(), mc.player.getY() + 0.00000000000013, mc.player.getZ(), true);
                    spoof(mc.player.getX(), mc.player.getY() + 0.00000000000027, mc.player.getZ(), false);
                }
            }
            if (exploit.getValue() == exploitEn.Strict) {
                double[] strict_direction = new double[]{100f * -Math.sin(Math.toRadians(mc.player.getYaw())), 100f * Math.cos(Math.toRadians(mc.player.getYaw()))};
                for (int i = 0; i < getRuns(); i++) {
                    if (random.nextBoolean()) {
                        spoof(mc.player.getX() - strict_direction[0], mc.player.getY(), mc.player.getZ() - strict_direction[1], false);
                    } else {
                        spoof(mc.player.getX() + strict_direction[0], mc.player.getY(), mc.player.getZ() + strict_direction[1], true);
                    }
                }
            }
            send = false;
            delayTimer.reset();
        }
    }

    private void spoof(double x, double y, double z, boolean ground) {
        if (rotation.getValue()) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, mc.player.getYaw(), mc.player.getPitch(), ground));
        } else {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, ground));
        }
    }

    private int getRuns() {
        return spoofs.getValueInt();
    }

    private enum exploitEn {
        Strong, Fast, Strict, Phobos
    }
}
