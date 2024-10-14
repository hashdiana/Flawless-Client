package me.flawless.mod.modules.impl.player;

import com.mojang.authlib.GameProfile;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.*;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Blink
        extends Module {
    public static Blink INSTANCE = new Blink();
    private final CopyOnWriteArrayList<Packet<?>> packetsList = new CopyOnWriteArrayList<>();
    public static OtherClientPlayerEntity fakePlayer;
    public Blink() {
        super("Blink", "Fake lag", Category.Player);
        INSTANCE = this;
    }

    private final BooleanSetting allPacket =
            add(new BooleanSetting("AllPacket", true));

    @Override
    public void onEnable() {
        packetsList.clear();
        if (nullCheck()) {
            disable();
            return;
        }
        fakePlayer = new OtherClientPlayerEntity(mc.world, new GameProfile(UUID.fromString("11451466-6666-6666-6666-666666666601"), mc.player.getName().getString()));
        fakePlayer.copyPositionAndRotation(mc.player);
        fakePlayer.bodyYaw = mc.player.bodyYaw;
        fakePlayer.headYaw = mc.player.headYaw;
        fakePlayer.getInventory().clone(mc.player.getInventory());
        mc.world.addEntity(fakePlayer);
    }

    @Override
    public void onUpdate() {
        if (mc.player.isDead()) {
            packetsList.clear();
            disable();
        }
    }

    @Override
    public void onLogin() {
        if (this.isOn()) {
            packetsList.clear();
            disable();
        }
    }

    @EventHandler
    public void onSendPacket(PacketEvent.Send event) {
        Packet<?> t = event.getPacket();
        if (t instanceof ChatMessageC2SPacket) {
            return;
        }
        if (t instanceof RequestCommandCompletionsC2SPacket) {
            return;
        }
        if (t instanceof CommandExecutionC2SPacket) {
            return;
        }
        if (t instanceof TeleportConfirmC2SPacket) {
            return;
        }
        if (t instanceof KeepAliveC2SPacket) {
            return;
        }
        if (t instanceof AdvancementTabC2SPacket) {
            return;
        }
        if (t instanceof ClientStatusC2SPacket) {
            return;
        }
        if (t instanceof ClickSlotC2SPacket) {
            return;
        }
        if (t instanceof PlayerMoveC2SPacket || allPacket.getValue()) {
            packetsList.add(event.getPacket());
            event.cancel();
        }
    }

    @Override
    public void onDisable() {
        if (nullCheck()) {
            packetsList.clear();
            disable();
            return;
        }
        if (fakePlayer != null) {
            fakePlayer.kill();
            fakePlayer.setRemoved(Entity.RemovalReason.KILLED);
            fakePlayer.onRemoved();
            fakePlayer = null;
        }
        for (Packet<?> packet : packetsList) {
            mc.player.networkHandler.sendPacket(packet);
        }
    }

    @Override
    public String getInfo() {
        return String.valueOf(packetsList.size());
    }
}