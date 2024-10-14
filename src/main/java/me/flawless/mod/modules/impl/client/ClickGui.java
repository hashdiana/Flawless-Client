package me.flawless.mod.modules.impl.client;

import me.flawless.FlawLess;
import me.flawless.api.managers.GuiManager;
import me.flawless.api.utils.math.FadeUtils;
import me.flawless.api.utils.math.AnimateUtil;
import me.flawless.mod.gui.clickgui.components.Component;
import me.flawless.mod.gui.clickgui.components.impl.BooleanComponent;
import me.flawless.mod.gui.clickgui.components.impl.ColorComponents;
import me.flawless.mod.gui.clickgui.components.impl.ModuleComponent;
import me.flawless.mod.gui.clickgui.components.impl.SliderComponent;
import me.flawless.mod.gui.clickgui.ClickGuiScreen;
import me.flawless.mod.gui.clickgui.tabs.ClickGuiTab;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.ColorSetting;
import me.flawless.mod.modules.settings.impl.EnumSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;

import java.awt.*;

public class ClickGui extends Module {
	public static ClickGui INSTANCE;
	private final EnumSetting<Pages> page = add(new EnumSetting<>("Page", Pages.General));
	public final EnumSetting<Type> uiType = add(new EnumSetting<>("UIType", Type.New, v -> page.getValue() == Pages.Element));
	public final BooleanSetting activeBox = add(new BooleanSetting("ActiveBox", true, v -> page.getValue() == Pages.Element));
	public final ColorSetting bind = add(new ColorSetting("Bind", new Color(255, 255, 255), v -> page.getValue() == Pages.Element).injectBoolean(true));
	public final ColorSetting gear = add(new ColorSetting("Gear", new Color(150, 150, 150), v -> page.getValue() == Pages.Element).injectBoolean(true));


	public final BooleanSetting font = add(new BooleanSetting("Font", false, v -> page.getValue() == Pages.General));
	public final EnumSetting<AnimateUtil.AnimMode> animMode = add(new EnumSetting<>("AnimMode", AnimateUtil.AnimMode.Mio, v -> page.getValue() == Pages.General));
	public final SliderSetting height = add(new SliderSetting("Height", 16, 10, 20, 1, v -> page.getValue() == Pages.General));
	public final EnumSetting<Mode> mode = add(new EnumSetting<>("EnableAnim", Mode.Reset, v -> page.getValue() == Pages.General));
	public final BooleanSetting scissor = add(new BooleanSetting("Scissor", true, v -> page.getValue() == Pages.General));
	public final SliderSetting animationSpeed = add(new SliderSetting("AnimationSpeed", 0.2, 0.01, 1, 0.01, v -> page.getValue() == Pages.General));
	public final SliderSetting sliderSpeed = add(new SliderSetting("SliderSpeed", 0.2, 0.01, 1, 0.01, v -> page.getValue() == Pages.General));
	public final SliderSetting booleanSpeed = add(new SliderSetting("BooleanSpeed", 0.2, 0.01, 1, 0.01, v -> page.getValue() == Pages.General));
	public final ColorSetting color = add(new ColorSetting("Main", -1716191232, v -> page.getValue() == Pages.Color));
	public final ColorSetting mainEnd = add(new ColorSetting("MainEnd", -2113929216, v -> page.getValue() == Pages.Color).injectBoolean(true));
	public final ColorSetting mainHover = add(new ColorSetting("Hover", 2097086464, v -> page.getValue() == Pages.Color));
	public final ColorSetting bar = add(new ColorSetting("Bar", -1716191232, v -> page.getValue() == Pages.Color));
	public final ColorSetting barEnd = add(new ColorSetting("BarEnd", -2113929216, v -> page.getValue() == Pages.Color).injectBoolean(true));
	public final ColorSetting disableText = add(new ColorSetting("DisableText", -1, v -> page.getValue() == Pages.Color));
	public final ColorSetting enableText = add(new ColorSetting("EnableText", -1, v -> page.getValue() == Pages.Color));
	public final ColorSetting enableTextS = add(new ColorSetting("EnableText2", new Color(255, 0, 0), v -> page.getValue() == Pages.Color));
	public final ColorSetting module = add(new ColorSetting("Module", 708788031, v -> page.getValue() == Pages.Color));
	public final ColorSetting moduleHover = add(new ColorSetting("ModuleHover", 385451140, v -> page.getValue() == Pages.Color));
	public final ColorSetting setting = add(new ColorSetting("Setting", 1579032, v -> page.getValue() == Pages.Color));
	public final ColorSetting settingHover = add(new ColorSetting("SettingHover", 385451140, v -> page.getValue() == Pages.Color));
	public final ColorSetting background = add(new ColorSetting("Background", -2113929216, v -> page.getValue() == Pages.Color));
	public ClickGui() {
		super("ClickGui", Category.Client);
		INSTANCE = this;
	}

	public static final FadeUtils fade = new FadeUtils(300);

	@Override
	public void onUpdate() {
		if (!(mc.currentScreen instanceof ClickGuiScreen)) {
			disable();
		}
	}

	int lastHeight;
	@Override
	public void onEnable() {
		if (lastHeight != height.getValueInt()) {
			for (ClickGuiTab tab : FlawLess.GUI.tabs) {
				for (Component component : tab.getChildren()) {
					if (component instanceof ModuleComponent moduleComponent) {
						for (Component settingComponent : moduleComponent.getSettingsList()) {
							settingComponent.setHeight(height.getValueInt());
							settingComponent.defaultHeight = height.getValueInt();
						}
					}
					component.setHeight(height.getValueInt());
					component.defaultHeight = height.getValueInt();
				}
			}
			lastHeight = height.getValueInt();
		}
		if (mode.getValue() == Mode.Reset) {
			for (ClickGuiTab tab : FlawLess.GUI.tabs) {
				for (Component component : tab.getChildren()) {
					component.currentOffset = 0;
					if (component instanceof ModuleComponent moduleComponent) {
						moduleComponent.isPopped = false;
						for (Component settingComponent : moduleComponent.getSettingsList()) {
							settingComponent.currentOffset = 0;
							if (settingComponent instanceof SliderComponent sliderComponent) {
								sliderComponent.renderSliderPosition = 0;
							} else if (settingComponent instanceof BooleanComponent booleanComponent) {
								booleanComponent.currentWidth = 0;
							} else if (settingComponent instanceof ColorComponents colorComponents) {
								colorComponents.currentWidth = 0;
							}
						}
					}
				}
				tab.currentHeight = 0;
			}
		}
		fade.reset();
		if (nullCheck()) {
			disable();
			return;
		}
		mc.setScreen(GuiManager.clickGui);
	}

	@Override
	public void onDisable() {
		if (mc.currentScreen instanceof ClickGuiScreen) {
			mc.setScreen(null);
		}
	}

	public enum Mode {
		Scale, Pull, Scissor, Reset, None
	}

	private enum Pages {
		General,
		Color,
		Element
	}

	public enum Type {
		Old,
		New
	}
}