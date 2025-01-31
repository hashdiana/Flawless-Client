package me.flawless.mod.gui.clickgui.components.impl;

import me.flawless.FlawLess;
import me.flawless.api.managers.GuiManager;
import me.flawless.api.utils.render.Render2DUtil;
import me.flawless.api.utils.render.TextUtil;
import me.flawless.mod.gui.clickgui.components.Component;
import me.flawless.mod.gui.clickgui.ClickGuiScreen;
import me.flawless.mod.gui.clickgui.tabs.ClickGuiTab;
import me.flawless.mod.modules.impl.client.ClickGui;
import me.flawless.mod.modules.settings.impl.EnumSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
public class EnumComponent extends Component {
	private final EnumSetting setting;
	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.test(null);
		}
		return true;
	}
	public EnumComponent(ClickGuiTab parent, EnumSetting enumSetting) {
		super();
		this.parent = parent;
		setting = enumSetting;
	}

	private boolean hover = false;

	public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		if ((mouseX >= ((parentX + 2)) && mouseX <= (((parentX)) + parentWidth - 2)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + defaultHeight - 2))) {
			hover = true;
			if (GuiManager.currentGrabbed == null && isVisible()) {
				if (mouseClicked) {
					ClickGuiScreen.clicked = false;
					setting.increaseEnum();
				}
				if (ClickGuiScreen.rightClicked) {
					setting.popped = !setting.popped;
					ClickGuiScreen.rightClicked = false;
				}
			}
		} else {
			hover = false;
		}

		if (GuiManager.currentGrabbed == null && isVisible() && mouseClicked) {
			int cy = parentY + offset - 1 + (defaultHeight - 2) - 2;
			if (setting.popped) {
				for (Object o : setting.getValue().getDeclaringClass().getEnumConstants()) {
					if (mouseX >= parentX && mouseX <= parentX + parentWidth && mouseY >= TextUtil.getHeight() / 2 + cy && mouseY < TextUtil.getHeight() + TextUtil.getHeight() / 2 + cy) {
						setting.setEnumValue(String.valueOf(o));
						ClickGuiScreen.clicked = false;
						break;
					}
					cy += (int) TextUtil.getHeight();
				}
			}
		}
	}

	boolean isback;
	@Override
	public int getHeight() {
		if (!isVisible()) {
			return 0;
		}
		if (setting.popped && !isback) {
			int y = 0;
			for (Object ignored : setting.getValue().getDeclaringClass().getEnumConstants()) {
				y += (int) TextUtil.getHeight();
			}
			return defaultHeight + y;
		} else {
			return defaultHeight;
		}

	}

	@Override
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		isback = back;
		currentOffset = animate(currentOffset, offset);
		if (back && Math.abs(currentOffset - offset) <= 0.5) {
			return false;
		}
		int x = parent.getX();
		int y = (int) (parent.getY() + currentOffset - 2);
		int width = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();

		if (ClickGui.INSTANCE.mainEnd.booleanValue) {
			Render2DUtil.drawRectHorizontal(matrixStack, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight - 1, hover ? ClickGui.INSTANCE.mainHover.getValue() : FlawLess.GUI.getColor(), ClickGui.INSTANCE.mainEnd.getValue());
		} else {
			Render2DUtil.drawRect(matrixStack, (float) x + 1, (float) y + 1, (float) width - 2, (float) defaultHeight - 1, hover ? ClickGui.INSTANCE.mainHover.getValue() : FlawLess.GUI.getColor());
		}
		TextUtil.drawString(drawContext, setting.getName() + ": " + setting.getValue().name(), x + 4, y + getTextOffsetY(), new Color(-1).getRGB());
		TextUtil.drawString(drawContext, setting.popped ? "-" : "+", x + width - 11, y + getTextOffsetY(), new Color(255, 255, 255).getRGB());

		int cy = (int) (parent.getY() + currentOffset - 1 + (defaultHeight - 2)) - 2;
		if (setting.popped && !back) {
			for (Object o : setting.getValue().getDeclaringClass().getEnumConstants()) {

				String s = o.toString();

				TextUtil.drawString(drawContext, s, width / 2d - TextUtil.getWidth(s) / 2 + 2.0f + x, TextUtil.getHeight() / 2 + (cy), setting.getValue().name().equals(s) ? -1 : new Color(120, 120, 120).getRGB());
				cy += (int) TextUtil.getHeight();
			}
		}
		return true;
	}
}