package me.flawless.mod.commands.impl;

import me.flawless.FlawLess;
import me.flawless.api.managers.CommandManager;
import me.flawless.api.managers.ConfigManager;
import me.flawless.mod.commands.Command;

import java.io.File;
import java.util.List;

public class SaveCommand extends Command {

	public SaveCommand() {
		super("save", "save", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§e[!] §fSaving..");
		if (parameters.length == 1) {
			ConfigManager.options = new File(mc.runDirectory, parameters[0] + ".cfg");
			FlawLess.save();
			ConfigManager.options = new File(mc.runDirectory, "flawless_options.txt");
		}
		FlawLess.save();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
