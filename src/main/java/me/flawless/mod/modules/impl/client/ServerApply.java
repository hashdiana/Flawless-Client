package me.flawless.mod.modules.impl.client;

import me.flawless.FlawLess;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.utils.math.Timer;
import me.flawless.asm.accessors.IPlayerPositionLookS2CPacket;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;

public class ServerApply extends Module {
    public final BooleanSetting rotate = add(new BooleanSetting("Rotate", true));
    public final BooleanSetting slot = add(new BooleanSetting("Slot", true));
    public final BooleanSetting pack = add(new BooleanSetting("ResourcePack", true));
    public ServerApply() {
        super("ServerApply", Category.Client);
        FlawLess.EVENT_BUS.subscribe(this);
    }

    boolean send = false;

    private Timer applyTimer = new Timer();
    @Override
    public void onLogin() {
        applyTimer.reset();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!pack.getValue() && event.getPacket() instanceof ResourcePackSendS2CPacket) {
            //System.out.println("ResourcePackSendS2CPacket");
            send = true;
            event.cancel();
        }
        if (!applyTimer.passed(1000)) return;
        if (nullCheck()) return;
        if (!rotate.getValue() && event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            if (packet.getFlags().contains(PositionFlag.Y_ROT)) {
                ((IPlayerPositionLookS2CPacket) packet).setYaw(0);
            } else {
                ((IPlayerPositionLookS2CPacket) packet).setYaw(mc.player.getYaw());
            }
            if (packet.getFlags().contains(PositionFlag.X_ROT)) {
                ((IPlayerPositionLookS2CPacket) packet).setPitch(0);
            } else {
                ((IPlayerPositionLookS2CPacket) packet).setPitch(mc.player.getPitch());
            }
        }
        if (!slot.getValue() && event.getPacket() instanceof UpdateSelectedSlotS2CPacket packet) {
            event.setCancelled(true);
            if (packet.getSlot() != mc.player.getInventory().selectedSlot) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            }
        }
    }

    @Override
    public void onUpdate() {
        if (send && mc.player != null) {
            //System.out.println("send");
            mc.player.networkHandler.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
            mc.player.networkHandler.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.DOWNLOADED));
            mc.player.networkHandler.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            send = false;
        }
    }

    @Override
    public void enable() {
        this.state = true;
    }

    @Override
    public void disable() {
        this.state = true;
    }

    @Override
    public boolean isOn() {
        return true;
    }
}
