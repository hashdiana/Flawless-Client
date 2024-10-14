package me.flawless.mod.modules.impl.player;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PacketControl extends Module {
    public static PacketControl INSTANCE;
    public PacketControl() {
        super("PacketControl", Category.Player);
        INSTANCE = this;
    }

    private final SliderSetting fullPackets =
            add(new SliderSetting("FullPackets", 15, 1, 50));
    private final SliderSetting positionPackets =
            add(new SliderSetting("PositionPackets", 15, 1, 50));
    private int fullPacket;
    private int positionPacket;
    public boolean full;

    @EventHandler
    public final void onPacketSend(final PacketEvent.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket.PositionAndOnGround && !full) {
            positionPacket++;
            if (positionPacket >= positionPackets.getValue()) {
                positionPacket = 0;
                full = true;
            }
        } else if (event.getPacket() instanceof PlayerMoveC2SPacket.Full && full) {
            fullPacket++;
            if (fullPacket > fullPackets.getValue()) {
                fullPacket = 0;
                full = false;
            }
        }
    }
}
