package me.flawless.mod.gui.clickgui.components.impl;

import me.flawless.api.managers.GuiManager;
import me.flawless.api.utils.math.FadeUtils;
import me.flawless.api.utils.render.Render2DUtil;
import me.flawless.api.utils.render.TextUtil;
import me.flawless.mod.gui.clickgui.ClickGuiScreen;
import me.flawless.mod.gui.clickgui.components.Component;
import me.flawless.mod.gui.clickgui.tabs.ClickGuiTab;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.client.ClickGui;
import me.flawless.mod.modules.settings.Setting;
import me.flawless.mod.modules.settings.impl.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleComponent extends Component {

	private final String text;
	private final Module module;
	private final ClickGuiTab parent;
	private boolean popped = false;

	private int expandedHeight = defaultHeight;

	private final List<Component> settingsList = new ArrayList<>();
	public List<Component> getSettingsList() {
		return settingsList;
	}
	public ModuleComponent(String text, ClickGuiTab parent, Module module) {
		super();
		this.text = text;
		this.parent = parent;
		this.module = module;
		for (Setting setting : this.module.getSettings()) {
			Component c;
			if (setting.hide) {
				c = null;
			} else if (setting instanceof SliderSetting) {
				c = new SliderComponent(this.parent, (SliderSetting) setting);
			} else if (setting instanceof BooleanSetting) {
				c = new BooleanComponent(this.parent, (BooleanSetting) setting);
			} else if (setting instanceof BindSetting) {
				c = new BindComponent(this.parent, (BindSetting) setting);
			} else if (setting instanceof EnumSetting) {
				c = new EnumComponent(this.parent, (EnumSetting) setting);
			} else if (setting instanceof ColorSetting) {
				c = new ColorComponents(this.parent, (ColorSetting) setting);
			} else if (setting instanceof StringSetting) {
				c = new StringComponent(this.parent, (StringSetting) setting);
			} else {
				c = null;
			}
			if (c != null)
				settingsList.add(c);
		}

		RecalculateExpandedHeight();
	}

	boolean hovered = false;

	public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();

		// If the Module options are popped, display all of the options.
		if (this.popped) {
			// Updates all of the options. 
			int i = offset + defaultHeight + 1;
			for (Component children : this.settingsList) {
				children.update(i, mouseX, mouseY, mouseClicked);
				i += children.getHeight();
			}
		}


		// Check if the current Module Component is currently hovered over.
		hovered = ((mouseX >= parentX && mouseX <= (parentX + parentWidth)) && (mouseY >= parentY + offset && mouseY <= (parentY + offset + defaultHeight - 1)));
		if (hovered && GuiManager.currentGrabbed == null) {
			if (mouseClicked) {
				ClickGuiScreen.clicked = false;
				if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
					module.drawnSetting.setValue(!module.drawnSetting.getValue());
				} else {
					module.toggle();
				}
			}

			if (ClickGuiScreen.rightClicked) {
				ClickGuiScreen.rightClicked = false;
				this.popped = !this.popped;
			}
		}
		RecalculateExpandedHeight();
		if (this.popped) {
			this.setHeight(expandedHeight);
		} else {
			this.setHeight(defaultHeight);
		}
	}

	public boolean isPopped = false;
	public double currentWidth = 0;
	@Override
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		currentOffset = animate(currentOffset, offset);
		if (ClickGui.fade.getQuad(FadeUtils.Quad.Out) >= 1 && ClickGui.INSTANCE.scissor.getValue())
			setScissorRegion(parentX * 2, (int) ((parentY + currentOffset + defaultHeight) * 2), parentWidth * 2, 9999);
		if (this.popped) {
			isPopped = true;
			int i = offset + defaultHeight + 1;
			for (Component children : this.settingsList) {
				if (children.isVisible()) {
					children.draw(i, drawContext, partialTicks, color, false);
					i += children.getHeight();
				} else {
					/*if (children instanceof SliderComponent sliderComponent) {
						sliderComponent.renderSliderPosition = 0;
					} else if (children instanceof BooleanComponent booleanComponent) {
						booleanComponent.currentWidth = 0;
					} else if (children instanceof ColorComponents colorComponents) {
						colorComponents.currentWidth = 0;
					}*/
					children.currentOffset = i - defaultHeight;
				}
			}
		} else if (isPopped) {
			boolean finish2 = true;
			boolean finish = false;
			for (Component children : this.settingsList) {
				if (children.isVisible()) {
					if (!children.draw((int) currentOffset, drawContext, partialTicks, color, true)) {
						finish = true;
					} else {
						finish2 = false;
					}
				}
			}
			if (finish && finish2) {
				isPopped = false;
			}
		} else {
			for (Component children : this.settingsList) {
				children.currentOffset = currentOffset;
			}
		}
		if (ClickGui.fade.getQuad(FadeUtils.Quad.Out) >= 1 && ClickGui.INSTANCE.scissor.getValue())
			GL11.glDisable(GL11.GL_SCISSOR_TEST);

		currentWidth = animate(currentWidth, module.isOn() ? (parentWidth - 2D) : 0D, ClickGui.INSTANCE.booleanSpeed.getValue());
		if (ClickGui.INSTANCE.activeBox.getValue()) {
			if (ClickGui.INSTANCE.mainEnd.booleanValue) {
				Render2DUtil.drawRectHorizontal(matrixStack, parentX + 1, (int) (parentY + currentOffset), (float) currentWidth, defaultHeight - 1, hovered ? ClickGui.INSTANCE.mainHover.getValue() : ClickGui.INSTANCE.color.getValue(), ClickGui.INSTANCE.mainEnd.getValue());
			} else {
				Render2DUtil.drawRect(matrixStack, parentX + 1, (int) (parentY + currentOffset), (float) currentWidth, defaultHeight - 1, hovered ? ClickGui.INSTANCE.mainHover.getValue() : ClickGui.INSTANCE.color.getValue());
			}
		}
		if (module.isOff() || !ClickGui.INSTANCE.activeBox.getValue())
			Render2DUtil.drawRect(matrixStack, parentX + 1, (int) (parentY + currentOffset), parentWidth - 2, defaultHeight - 1, hovered ? ClickGui.INSTANCE.moduleHover.getValue() : ClickGui.INSTANCE.module.getValue());
		if (hovered && InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
			TextUtil.drawString(drawContext, "Drawn " + (module.drawnSetting.getValue() ? "§aOn" : "§cOff"), (float) (parentX + 4), (float) (parentY + getTextOffsetY() + currentOffset) - 1, -1);
		} else {
			TextUtil.drawString(drawContext, this.text, (float) (parentX + 4), (float) (parentY + getTextOffsetY() + currentOffset) - 1,
					module.isOn() ? ClickGui.INSTANCE.enableText.getValue().getRGB() : ClickGui.INSTANCE.disableText.getValue().getRGB());
		}

		if (ClickGui.INSTANCE.bind.booleanValue) {
			if (module.getBind().getKey() != -1) {
				String bindText = "[" + module.getBind().getBind() + "]";
				TextUtil.drawStringWithScale(drawContext, bindText, parentX + 5 + TextUtil.getWidth(this.text), (float) (parentY + getTextOffsetY() + currentOffset - TextUtil.getHeight() / 4), ClickGui.INSTANCE.bind.getValue(), 0.5f);
			}
		}
		if (ClickGui.INSTANCE.gear.booleanValue) {
			if (isPopped) {
				TextUtil.drawString(drawContext, "…", parentX + parentWidth - 12,
						parentY + getTextOffsetY() + currentOffset - 3, ClickGui.INSTANCE.gear.getValue().getRGB());
			} else {
				TextUtil.drawString(drawContext, "+", parentX + parentWidth - 11,
						parentY + getTextOffsetY() + currentOffset, ClickGui.INSTANCE.gear.getValue().getRGB());
			}
		}
		return true;
	}

	public void setScissorRegion(int x, int y, int width, int height) {
		if (y > mc.getWindow().getHeight()) return;
		double scaledY = (mc.getWindow().getHeight() - (y + height));
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x, (int) scaledY, width, height);
	}
	public void RecalculateExpandedHeight() {
		int height = defaultHeight;
		for (Component children : this.settingsList) {
			if (children != null && children.isVisible()) {
				height += children.getHeight();
			}
		}
		expandedHeight = height;
	}
}
