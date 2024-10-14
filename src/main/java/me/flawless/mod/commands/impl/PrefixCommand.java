package me.flawless.mod.commands.impl;

import me.flawless.FlawLess;
import me.flawless.mod.commands.Command;
import me.flawless.api.managers.CommandManager;

import java.util.List;

public class PrefixCommand extends Command {

	public PrefixCommand() {
		super("prefix", "Set prefix", "[prefix]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		if (parameters[0].startsWith("/")) {
			CommandManager.sendChatMessage("§6[!] §fPlease specify a valid §bprefix.");
			return;
		}
		FlawLess.PREFIX = parameters[0];
		CommandManager.sendChatMessage("§a[√] §bPrefix §fset to §e" + parameters[0]);
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
