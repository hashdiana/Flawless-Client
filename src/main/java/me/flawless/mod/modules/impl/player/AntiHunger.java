package me.flawless.mod.modules.impl.player;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.eventbus.EventPriority;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.asm.accessors.IPlayerMoveC2SPacket;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.misc.AutoPearl;
import me.flawless.mod.modules.impl.misc.BowBomb;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger extends Module {
    public static AntiHunger INSTANCE;
    public final BooleanSetting sprint = this.add(new BooleanSetting("Sprint", true));
    public final BooleanSetting ground = this.add(new BooleanSetting("Ground", true));
    public AntiHunger() {
        super("AntiHunger", "lol", Category.Player);
        INSTANCE = this;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPacketSend(PacketEvent.Send event) {
        if (BowBomb.send) return;
        if (AutoPearl.throwing) return;
        if (event.getPacket() instanceof ClientCommandC2SPacket packet && sprint.getValue()) {
            if (packet.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                event.cancel();
            }
        }
        if (event.getPacket() instanceof PlayerMoveC2SPacket && ground.getValue() && mc.player.fallDistance <= 0 && !mc.interactionManager.isBreakingBlock()) {
            ((IPlayerMoveC2SPacket) event.getPacket()).setOnGround(false);
        }
    }
}
