package me.flawless.mod.commands.impl;

import me.flawless.FlawLess;
import me.flawless.api.managers.CommandManager;
import me.flawless.api.managers.ConfigManager;
import me.flawless.mod.commands.Command;

import java.util.List;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super("reload", "debug", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§e[!] §fReloading..");
		FlawLess.CONFIG = new ConfigManager();
		FlawLess.PREFIX = FlawLess.CONFIG.getString("prefix", FlawLess.PREFIX);
		FlawLess.CONFIG.loadSettings();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
