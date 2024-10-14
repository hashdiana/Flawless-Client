package me.flawless.mod.commands.impl;

import me.flawless.FlawLess;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.managers.CommandManager;
import me.flawless.mod.commands.Command;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.List;

public class PingCommand extends Command {

	public PingCommand() {
		super("ping", "ping", "");
	}

	private long sendTime;

	@Override
	public void runCommand(String[] parameters) {
		sendTime = System.currentTimeMillis();
		mc.player.networkHandler.sendCommand("chat ");
		FlawLess.EVENT_BUS.subscribe(this);
	}


	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}

	@EventHandler
	public void onPacketReceive(PacketEvent.Receive e) {
		if (e.getPacket() instanceof GameMessageS2CPacket packet) {
			if (packet.content().getString().contains("chat.use") || packet.content().getString().contains("命令") || packet.content().getString().contains("Bad command")|| packet.content().getString().contains("No such command") || packet.content().getString().contains("<--[HERE]") || packet.content().getString().contains("Unknown")) {
				CommandManager.sendChatMessage("ping: " + (System.currentTimeMillis() - sendTime) + "ms");
				FlawLess.EVENT_BUS.unsubscribe(this);
			}
		}
	}
}
