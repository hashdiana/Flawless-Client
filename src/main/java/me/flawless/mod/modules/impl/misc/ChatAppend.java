package me.flawless.mod.modules.impl.misc;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.SendMessageEvent;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.StringSetting;

public class ChatAppend extends Module {
	public static ChatAppend INSTANCE;
	private final StringSetting message = add(new StringSetting("append", "reb-v2"));
	public ChatAppend() {
		super("ChatAppend", Category.Misc);
		INSTANCE = this;
	}

	@EventHandler
	public void onSendMessage(SendMessageEvent event) {
		if (nullCheck() || event.isCancelled()) return;
		String message = event.message;

		if (message.startsWith("/") || message.startsWith("!") || message.endsWith(this.message.getValue())) {
			return;
		}
		String suffix = this.message.getValue();
		message = message + " " + suffix;
		event.message = message;
	}
}