package me.flawless;

import me.flawless.api.events.eventbus.EventBus;
import me.flawless.api.managers.*;
import me.flawless.mod.modules.impl.combat.AutoCrystal;
import net.fabricmc.api.ModInitializer;

import java.lang.invoke.MethodHandles;

public final class FlawLess implements ModInitializer {

	@Override
	public void onInitialize()
	{
		//Blur.register();
		load();
	}

	public static final String LOG_NAME = "FlawLess";
	public static final String VERSION = "1.0";
	public static String PREFIX = ";";
	public static final EventBus EVENT_BUS = new EventBus();
	// Systems
	public static ModuleManager MODULE;
	public static CommandManager COMMAND;
	public static AltManager ALT;
	public static GuiManager GUI;
	public static ConfigManager CONFIG;
	public static RotateManager ROTATE;
	public static MineManager BREAK;
	public static PopManager POP;
	public static FriendManager FRIEND;
	public static TimerManager TIMER;
	public static ShaderManager SHADER;
	public static FPSManager FPS;
	public static ServerManager SERVER;
	public static boolean loaded = false;

	public static void update() {
		MODULE.onUpdate();
		GUI.update();
		POP.update();
	}

	public static void load() {
		System.out.println("[" + FlawLess.LOG_NAME + "] Setup");
		EVENT_BUS.registerLambdaFactory("me", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
		CONFIG = new ConfigManager();
		PREFIX = FlawLess.CONFIG.getString("prefix", ";");
		MODULE = new ModuleManager();
		COMMAND = new CommandManager();
		GUI = new GuiManager();
		ALT = new AltManager();
		FRIEND = new FriendManager();
		ROTATE = new RotateManager();
		BREAK = new MineManager();
		POP = new PopManager();
		TIMER = new TimerManager();
		SHADER = new ShaderManager();
		FPS = new FPSManager();
		SERVER = new ServerManager();
		CONFIG.loadSettings();
		System.out.println("[" + FlawLess.LOG_NAME + "] It's Okay");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (loaded) {
				save();
			}
		}));
		loaded = true;
	}

	public static void unload() {
		loaded = false;
		if (AutoCrystal.thread != null && AutoCrystal.thread.isAlive()) AutoCrystal.thread.stop();
		System.out.println("[" + FlawLess.LOG_NAME + "] Unloading..");
		EVENT_BUS.listenerMap.clear();
		ConfigManager.resetModule();
		CONFIG = null;
		MODULE = null;
		COMMAND = null;
		GUI = null;
		ALT = null;
		FRIEND = null;
		ROTATE = null;
		POP = null;
		TIMER = null;
		System.out.println("[" + FlawLess.LOG_NAME + "] Unloaded");
	}
	public static void save() {
		System.out.println("[" + FlawLess.LOG_NAME + "] Saving");
		CONFIG.saveSettings();
		FRIEND.saveFriends();
		ALT.saveAlts();
		System.out.println("[" + FlawLess.LOG_NAME + "] Saved");
	}
}
