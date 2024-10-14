package me.flawless.mod.commands.impl;

import me.flawless.FlawLess;
import me.flawless.api.managers.CommandManager;
import me.flawless.api.managers.ConfigManager;
import me.flawless.mod.commands.Command;

import java.io.File;
import java.util.List;

public class LoadCommand extends Command {

	public LoadCommand() {
		super("load", "debug", "[config]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		CommandManager.sendChatMessage("§e[!] §fLoading..");
		ConfigManager.options = new File(mc.runDirectory, parameters[0] + ".cfg");
		FlawLess.unload();
		FlawLess.load();
		ConfigManager.options = new File(mc.runDirectory, "flawless_options.txt");
		FlawLess.save();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
