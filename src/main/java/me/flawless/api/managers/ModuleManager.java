package me.flawless.api.managers;

import me.flawless.FlawLess;
import me.flawless.api.events.impl.Render3DEvent;
import me.flawless.api.utils.Wrapper;
import me.flawless.mod.Mod;
import me.flawless.mod.gui.clickgui.ClickGuiScreen;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.client.*;
import me.flawless.mod.modules.impl.combat.*;
import me.flawless.mod.modules.impl.misc.*;
import me.flawless.mod.modules.impl.movement.*;
import me.flawless.mod.modules.impl.player.*;
import me.flawless.mod.modules.impl.player.freelook.FreeLook;
import me.flawless.mod.modules.impl.render.*;
import me.flawless.mod.modules.settings.Setting;
import me.flawless.mod.modules.settings.impl.BindSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModuleManager implements Wrapper {
	public final ArrayList<Module> modules = new ArrayList<>();
	//public final HashMap<Module.Category, Integer> categoryModules = new HashMap<>();
	public static Mod lastLoadMod;

	public ModuleManager() {
		addModule(new Fonts());
		//Stable
		addModule(new Ambience());
		addModule(new AntiHunger());
		addModule(new AntiVoid());
		addModule(new AntiWeak());
		addModule(new AspectRatio());
		addModule(new Aura());
		addModule(new AutoAnchor());
		addModule(new AutoArmor());
		addModule(new AutoCity());
		addModule(new AutoCrystal());
		addModule(new AutoEXP());
		addModule(new AutoPush());
		addModule(new AutoTotem());
		addModule(new AutoTrap());
		addModule(new AutoWeb());
		addModule(new BedAura());
		addModule(new Blink());
		addModule(new BugClip());
		addModule(new BlockStrafe());
		addModule(new Blocker());
		addModule(new BowBomb());
		addModule(new BreakESP());
		addModule(new Burrow());
		addModule(new BurrowStuck());
		addModule(new CameraClip());
		addModule(new ChatAppend());
		addModule(new ChatSetting());
		addModule(new ChestStealer());
		addModule(new CityESP());
		addModule(new ClickGui());
		addModule(new Clip());
		addModule(new CombatSetting());
		addModule(new Criticals());
		addModule(new Crosshair());
		addModule(new CrystalChams());
		addModule(new ESP());
		addModule(new ElytraFly());
		addModule(new EntityControl());
		addModule(new FakeLag());
		addModule(new FakePlayer());
		addModule(new FastFall());
		addModule(new FastWeb());
		addModule(new Flatten());
		addModule(new Fly());
		addModule(new ForceSync());
		addModule(new FreeCamera());
		addModule(new FreeLook());
		addModule(new TimerModule());
		addModule(new GameSetting());
		addModule(new HUD());
		addModule(new HoleFiller());
		addModule(new HoleKick());
		addModule(new HoleSnap());
		addModule(new Indicator());
		addModule(new LogoutSpots());
		addModule(new AutoPearl());
		addModule(new AutoQueue());
		addModule(new PacketElytra());
		addModule(new ModuleList());
		addModule(new NameTags());
		addModule(new NoFall());
		addModule(new NoRender());
		addModule(new NoSlow());
		addModule(new NoSoundLag());
		addModule(new PacketControl());
		addModule(new PacketEat());
		addModule(new PacketFly());
		addModule(new PacketMine());
		addModule(new PearlClip());
		addModule(new PistonCrystal());
		addModule(new PlaceRender());
		addModule(new PlayerTweak());
		addModule(new PopChams());
		addModule(new PopCounter());
		addModule(new Replenish());
		addModule(new ServerApply());
		addModule(new ServerLagger());
		addModule(new Scaffold());
		addModule(new Shader());
		addModule(new ShulkerNuker());
		addModule(new ShulkerViewer());
		addModule(new Speed());
		addModule(new Sprint());
		addModule(new Step());
		addModule(new Surround());
		addModule(new TotemParticle());
		addModule(new Velocity());
		addModule(new ViewModel());
		addModule(new VClip());
		addModule(new XCarry());
		addModule(new Zoom());
		modules.sort(Comparator.comparing(Mod::getName));
	}

	public boolean setBind(int eventKey) {
		if (eventKey == -1 || eventKey == 0) {
			return false;
		}
		AtomicBoolean set = new AtomicBoolean(false);
		modules.forEach(module -> {
			for (Setting setting : module.getSettings()) {
				if (setting instanceof BindSetting bind) {
					if (bind.isListening()) {
						bind.setKey(eventKey);
						bind.setListening(false);
						if (bind.getBind().equals("DELETE")) {
							bind.setKey(-1);
						}
						set.set(true);
					}
				}
			}
		});
		return set.get();
	}

	public void onKeyReleased(int eventKey) {
		if (eventKey == -1 || eventKey == 0) {
			return;
		}
		modules.forEach(module -> {
			if (module.getBind().getKey() == eventKey && module.getBind().isHoldEnable() && module.getBind().hold) {
				module.toggle();
				module.getBind().hold = false;
			}
			module.getSettings().stream()
					.filter(setting -> setting instanceof BindSetting)
					.map(setting -> (BindSetting) setting)
					.filter(bindSetting -> bindSetting.getKey() == eventKey)
					.forEach(bindSetting -> bindSetting.setPressed(false));
		});
	}

	public void onKeyPressed(int eventKey) {
		if (eventKey == -1 || eventKey == 0 || mc.currentScreen instanceof ClickGuiScreen) {
			return;
		}
		modules.forEach(module -> {
			if (module.getBind().getKey() == eventKey && mc.currentScreen == null) {
				module.toggle();
				module.getBind().hold = true;
			}

			module.getSettings().stream()
					.filter(setting -> setting instanceof BindSetting)
					.map(setting -> (BindSetting) setting)
					.filter(bindSetting -> bindSetting.getKey() == eventKey)
					.forEach(bindSetting -> bindSetting.setPressed(true));
		});
	}

	public void onUpdate() {
		modules.stream().filter(Module::isOn).forEach(module -> {
			try {
				module.onUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				CommandManager.sendChatMessage("§4[!] " + e.getMessage());
			}
		});
	}

	public void onLogin() {
		modules.stream().filter(Module::isOn).forEach(Module::onLogin);
	}

	public void onLogout() {
		modules.stream().filter(Module::isOn).forEach(Module::onLogout);
	}

	public void render2D(DrawContext drawContext) {
		modules.stream().filter(Module::isOn).forEach(module -> module.onRender2D(drawContext, MinecraftClient.getInstance().getTickDelta()));
	}
	public void render3D(MatrixStack matrixStack) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		matrixStack.push();
		modules.stream().filter(Module::isOn).forEach(module -> module.onRender3D(matrixStack, mc.getTickDelta()));
		FlawLess.EVENT_BUS.post(new Render3DEvent(matrixStack, mc.getTickDelta()));
		matrixStack.pop();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}

	public void addModule(Module module) {
		module.add(module.getBind());
		modules.add(module);
		//categoryModules.put(module.getCategory(), categoryModules.getOrDefault(module.getCategory(), 0) + 1);
	}

	public void disableAll() {
		for (Module module : modules) {
			module.disable();
		}
	}

	public Module getModuleByName(String string) {
		for (Module module : modules) {
			if (module.getName().equalsIgnoreCase(string)) {
				return module;
			}
		}
		return null;
	}
}
