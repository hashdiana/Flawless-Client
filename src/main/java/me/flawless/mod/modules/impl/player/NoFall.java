package me.flawless.mod.modules.impl.player;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.asm.accessors.IPlayerMoveC2SPacket;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.misc.BowBomb;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
	private final SliderSetting distance =
			add(new SliderSetting("Distance", 3.0f, 0.0f, 8.0f, 0.1));
	public NoFall() {
		super("NoFall", Category.Player);
		this.setDescription("Prevents fall damage.");
	}

	@Override
	public String getInfo() {
		return "SpoofGround";
	}


	@EventHandler
	public void onPacketSend(PacketEvent.Send event) {
		if (nullCheck()) {
			return;
		}
		for (ItemStack is : mc.player.getArmorItems()) {
			if (is.getItem() == Items.ELYTRA) {
				return;
			}
		}
		if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
			if (mc.player.fallDistance >= (float) this.distance.getValue() && !BowBomb.send) {
				((IPlayerMoveC2SPacket) packet).setOnGround(true);
			}
		}
	}
}
