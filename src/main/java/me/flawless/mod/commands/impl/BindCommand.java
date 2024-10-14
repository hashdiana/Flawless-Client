package me.flawless.mod.commands.impl;

import me.flawless.FlawLess;
import me.flawless.mod.commands.Command;
import me.flawless.api.managers.CommandManager;
import me.flawless.mod.modules.Module;
import me.flawless.api.managers.ModuleManager;

import java.util.ArrayList;
import java.util.List;

public class BindCommand extends Command {

	public BindCommand() {
		super("bind", "Bind key", "[module] [key]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		String moduleName = parameters[0];
		Module module = FlawLess.MODULE.getModuleByName(moduleName);
		if (module == null) {
			CommandManager.sendChatMessage("§4[!] §fUnknown §bmodule!");
			return;
		}
		if (parameters.length == 1) {
			CommandManager.sendChatMessage("§6[!] §fPlease specify a §bkey.");
			return;
		}
		String rkey = parameters[1];
		if (rkey == null) {
			CommandManager.sendChatMessage("§4Unknown Error");
			return;
		}
		if (module.setBind(rkey.toUpperCase())) {
			CommandManager.sendChatMessage("§a[√] §fBind for §a" + module.getName() + "§f set to §7" + rkey.toUpperCase());
		}
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		if (count == 1) {
			String input = seperated.get(seperated.size() - 1).toLowerCase();
			ModuleManager cm = FlawLess.MODULE;
			List<String> correct = new ArrayList<>();
			for (Module x : cm.modules) {
				if (input.equalsIgnoreCase(FlawLess.PREFIX + "bind") || x.getName().toLowerCase().startsWith(input)) {
					correct.add(x.getName());
				}
			}
			int numCmds = correct.size();
			String[] commands = new String[numCmds];

			int i = 0;
			for (String x : correct) {
				commands[i++] = x;
			}

			return commands;
		}
		return null;
	}
}
