package me.flawless.mod.gui.clickgui.components.impl;

import me.flawless.api.managers.GuiManager;
import me.flawless.api.utils.render.Render2DUtil;
import me.flawless.api.utils.render.TextUtil;
import me.flawless.mod.gui.clickgui.ClickGuiScreen;
import me.flawless.mod.gui.clickgui.components.Component;
import me.flawless.mod.gui.clickgui.tabs.ClickGuiTab;
import me.flawless.mod.modules.impl.client.ClickGui;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class SliderComponent extends Component {

	private final ClickGuiTab parent;
	private double currentSliderPosition;
	final SliderSetting setting;

	public SliderComponent(ClickGuiTab parent, SliderSetting setting) {
		super();
		this.parent = parent;
		this.setting = setting;
	}

	@Override
	public boolean isVisible() {
		if (setting.visibility != null) {
			return setting.visibility.test(null);
		}
		return true;
	}

	private boolean clicked = false;
	private boolean hover = false;
	private boolean firstUpdate = true;
	@Override
	public void update(int offset, double mouseX, double mouseY, boolean mouseClicked) {
		if (firstUpdate) {
			this.currentSliderPosition = (float) ((setting.getValue() - setting.getMinimum()) / setting.getRange());
			firstUpdate = false;
		}
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();

		if ((mouseX >= ((parentX)) && mouseX <= (((parentX)) + parentWidth - 2)) && (mouseY >= (((parentY + offset))) && mouseY <= ((parentY + offset) + defaultHeight - 2))) {
			hover = true;
			if (GuiManager.currentGrabbed == null && isVisible()) {
				if (mouseClicked || ClickGuiScreen.hoverClicked && clicked) {
					clicked = true;
					ClickGuiScreen.hoverClicked = true;
					ClickGuiScreen.clicked = false;
					this.currentSliderPosition = (float) Math.min((mouseX - (parentX)) / (parentWidth - 4), 1f);
					this.currentSliderPosition = Math.max(0f, this.currentSliderPosition);
					this.setting.setValue((this.currentSliderPosition * this.setting.getRange()) + this.setting.getMinimum());
				}
			}
		} else {
			clicked = false;
			hover = false;
		}
	}

	public double renderSliderPosition = 0;

	@Override
	public boolean draw(int offset, DrawContext drawContext, float partialTicks, Color color, boolean back) {
		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		MatrixStack matrixStack = drawContext.getMatrices();
		currentOffset = animate(currentOffset, offset);
		if (back && Math.abs(currentOffset - offset) <= 0.5) {
			renderSliderPosition = 0;
			return false;
		}
		renderSliderPosition = animate(renderSliderPosition, Math.floor((parentWidth - 2) * currentSliderPosition), ClickGui.INSTANCE.sliderSpeed.getValue());
		//RenderUtil.drawBox(matrixStack, parentX + 3, (int) (parentY + currentOffset - 1), parentWidth - 6, 26, 0.5f, 0.5f, 0.5f, 0.3f);
		float height = ClickGui.INSTANCE.uiType.getValue() == ClickGui.Type.New ? 1 : defaultHeight - 1;
		float y = ClickGui.INSTANCE.uiType.getValue() == ClickGui.Type.New ? (float) (parentY + currentOffset + defaultHeight - 3) : (float) (parentY + currentOffset - 1);
		if (ClickGui.INSTANCE.mainEnd.booleanValue) {
			Render2DUtil.drawRectHorizontal(matrixStack, parentX + 1, y, (int) this.renderSliderPosition, height, hover ? ClickGui.INSTANCE.mainHover.getValue() : color, ClickGui.INSTANCE.mainEnd.getValue());
		} else {
			Render2DUtil.drawRect(matrixStack, parentX + 1, y, (int) this.renderSliderPosition, height, hover ? ClickGui.INSTANCE.mainHover.getValue() : color);
		}
		if (this.setting == null) return true;
		String value;
		if (setting.getValueInt() == setting.getValue()) {
			value = String.valueOf(setting.getValueInt());
		} else {
			value = String.valueOf(this.setting.getValueFloat());
		}
		value = value + setting.getSuffix();
		TextUtil.drawString(drawContext, setting.getName(), (float) (parentX + 4),
				(float) (parentY + getTextOffsetY() + currentOffset - 2), 0xFFFFFF);
		TextUtil.drawString(drawContext, value, parentX + parentWidth - TextUtil.getWidth(value) - 5,
				(float) (parentY + getTextOffsetY() + currentOffset - 2), 0xFFFFFF);
		return true;
	}
}

