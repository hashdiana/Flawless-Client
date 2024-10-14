/**
 * A class to represent a ClickGui Tab that contains different Components.
 */

package me.flawless.mod.gui.clickgui.tabs;

import me.flawless.FlawLess;
import me.flawless.api.managers.GuiManager;
import me.flawless.api.utils.render.Render2DUtil;
import me.flawless.api.utils.render.TextUtil;
import me.flawless.mod.gui.clickgui.ClickGuiScreen;
import me.flawless.mod.gui.clickgui.components.Component;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.client.ClickGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.ArrayList;


public class ClickGuiTab extends Tab {
	protected String title;
	protected final boolean drawBorder = true;
	private Module.Category category = null;
	protected final ArrayList<Component> children = new ArrayList<>();

	public ClickGuiTab(String title, int x, int y) {
		this.title = title;
		this.x = FlawLess.CONFIG.getInt(title + "_x", x);
		this.y = FlawLess.CONFIG.getInt(title + "_y", y);
		this.width = 100;
		this.mc = MinecraftClient.getInstance();
	}

	public ClickGuiTab(Module.Category category, int x, int y) {
		this(category.name(), x, y);
		this.category = category;
	}
	public ArrayList<Component> getChildren() {
		return children;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(String title) {
		this.title = title;
	}

	public final int getX() {
		return x;
	}

	public final void setX(int x) {
		this.x = x;
	}

	public final int getY() {
		return y;
	}

	public final void setY(int y) {
		this.y = y;
	}

	public final int getWidth() {
		return width;
	}

	public final void setWidth(int width) {
		this.width = width;
	}

	public final int getHeight() {
		return height;
	}

	public final void setHeight(int height) {
		this.height = height;
	}

	public final boolean isGrabbed() {
		return (GuiManager.currentGrabbed == this);
	}

	public final void addChild(Component component) {
		this.children.add(component);
	}

	boolean popped = true;

	@Override
	public void update(double mouseX, double mouseY, boolean mouseClicked) {
		onMouseClick(mouseX, mouseY, mouseClicked);
		if (popped) {
			int tempHeight = 1;
			for (Component child : children) {
				tempHeight += (child.getHeight());
			}
			this.height = tempHeight;
			int i = defaultHeight;
			for (Component child : this.children) {
				child.update(i, mouseX, mouseY, mouseClicked);
				i += child.getHeight();
			}
		}
	}

	public void onMouseClick(double mouseX, double mouseY, boolean mouseClicked) {
		if (GuiManager.currentGrabbed == null) {
			if (mouseX >= (x) && mouseX <= (x + width)) {
				if (mouseY >= (y + 1) && mouseY <= (y + 14)) {
					if (mouseClicked) {
						GuiManager.currentGrabbed = this;
					}
					else if (ClickGuiScreen.rightClicked) {
						popped = !popped;
						ClickGuiScreen.rightClicked = false;
					}
				}
			}
		}
	}

	public double currentHeight = 0;

	@Override
	public void draw(DrawContext drawContext, float partialTicks, Color color) {
		int tempHeight = 1;
		for (Component child : children) {
			tempHeight += (child.getHeight());
		}
		this.height = tempHeight;

		MatrixStack matrixStack = drawContext.getMatrices();
		currentHeight = Component.animate(currentHeight, height);
		if (drawBorder) {
			if (ClickGui.INSTANCE.barEnd.booleanValue) {
				Render2DUtil.drawRectVertical(matrixStack, x, y - 1, width, 15, ClickGui.INSTANCE.bar.getValue(), ClickGui.INSTANCE.barEnd.getValue());
			} else {
				Render2DUtil.drawRect(matrixStack, x, y - 1, width, 15, ClickGui.INSTANCE.bar.getValue());
			}
			Render2DUtil.drawRect(matrixStack, x, y - 1 + 15, width, 1, new Color(38, 38, 38));
			if (popped) Render2DUtil.drawRect(matrixStack, x, y + 15, width, (int) currentHeight - 1, ClickGui.INSTANCE.background.getValue());
		}
		if (popped) {
			int i = defaultHeight;
			for (Component child : children) {
				if (child.isVisible()) {
					child.draw(i, drawContext, partialTicks, color, false);
					i += child.getHeight();
				} else {
					child.currentOffset = i - defaultHeight;
				}
			}
		}
		TextUtil.drawString(drawContext, this.title, x + width / 2d - TextUtil.getWidth(title) / 2, y + 3, new Color(255, 255, 255));
	}
}
