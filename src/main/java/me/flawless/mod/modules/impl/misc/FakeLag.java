package me.flawless.mod.modules.impl.misc;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.events.impl.UpdateWalkingPlayerEvent;
import me.flawless.api.utils.math.Timer;
import me.flawless.api.utils.render.Render3DUtil;
import me.flawless.asm.accessors.IEntity;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FakeLag extends Module {
    public FakeLag() {
        super("FakeLag", Category.Misc);
    }

    private static final HashMap<PlayerEntity, Vec3d> map = new HashMap<>();
    private final SliderSetting spoof = add(new SliderSetting("Spoof", 500, 0, 5000, 1));
    private final BooleanSetting ping =
            add(new BooleanSetting("Ping", true));
    private final BooleanSetting entity =
            add(new BooleanSetting("Entity", true));
    private final CopyOnWriteArrayList<CustomPacket> packet = new CopyOnWriteArrayList<>();

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (nullCheck()) return;
        if (ping.getValue()) {
            if (event.getPacket() instanceof CommonPingS2CPacket || event.getPacket() instanceof KeepAliveS2CPacket) {
                packet.add(new CustomPacket(event.getPacket()));
                event.cancel();
                return;
            }
        }
        if (event.getPacket() instanceof EntityS2CPacket entityS2CPacket) {
            if (entityS2CPacket.getEntity(mc.world) instanceof PlayerEntity player) {
                if (player == mc.player) {
                    return;
                }
                if (map.containsKey(player) && entity.getValue()) {
                    if (map.get(player).distanceTo(mc.player.getPos()) < new Vec3d(entityS2CPacket.getDeltaX(), entityS2CPacket.getDeltaY(), entityS2CPacket.getDeltaZ()).distanceTo(mc.player.getPos())) {
                        packet.add(new CustomPacket(entityS2CPacket));
                        event.cancel();
                    }
                }
                map.put(player, player.getPos());
            }
        }
    }

    @Override
    public void onUpdate() {
        update();
    }

    @EventHandler
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        update();
    }

    @Override
    public void onRender3D(MatrixStack matrixStack, float partialTicks) {
        update();
        if (entity.getValue())
            for (Vec3d vec3d : map.values()) {
                Color color = new Color(255, 255, 255, 100);
                Render3DUtil.draw3DBox(matrixStack, ((IEntity) mc.player).getDimensions().getBoxAt(vec3d).expand(0, 0.1, 0), color, false, true);
            }
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            packet.clear();
            return;
        }
        for (CustomPacket p : packet) {
            p.apply();
        }
    }

    private void update() {
        if (nullCheck()) {
            packet.clear();
            return;
        }
        packet.removeIf(CustomPacket::send);
    }


    private class CustomPacket {
        Packet pp;
        final Timer timer;
        final int delay;

        public CustomPacket(Packet p) {
            pp = p;
            timer = new Timer();
            delay = spoof.getValueInt();
        }

        public boolean send() {
            if (timer.passedMs(delay)) {
                apply();
                return true;
            }
            return false;
        }

        public void apply() {
            if (pp != null) {
                pp.apply(mc.player.networkHandler);
            }
        }
    }
}
