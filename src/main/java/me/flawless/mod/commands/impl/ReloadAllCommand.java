package me.flawless.mod.commands.impl;

import me.flawless.FlawLess;
import me.flawless.api.managers.CommandManager;
import me.flawless.mod.commands.Command;

import java.util.List;

public class ReloadAllCommand extends Command {

	public ReloadAllCommand() {
		super("reloadall", "debug", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§e[!] §fReloading..");
		FlawLess.unload();
		FlawLess.load();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
