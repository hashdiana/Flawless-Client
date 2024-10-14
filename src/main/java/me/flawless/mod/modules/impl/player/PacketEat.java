package me.flawless.mod.modules.impl.player;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.utils.world.BlockUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class PacketEat extends Module {
	public static PacketEat INSTANCE;
	private final BooleanSetting deSync =
			add(new BooleanSetting("DeSync", false));
	public PacketEat() {
		super("PacketEat", Category.Player);
		INSTANCE = this;
	}

	@Override
	public void onUpdate() {
		if (nullCheck()) return;
		if (deSync.getValue() && mc.player.isUsingItem() && mc.player.getActiveItem().getItem().isFood()){
			mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, BlockUtil.getWorldActionId(mc.world)));
		}
	}

	@EventHandler
	public void onPacket(PacketEvent.Send event) {
		if (event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && mc.player.getActiveItem().getItem().isFood()) {
			event.cancel();
		}
	}
}