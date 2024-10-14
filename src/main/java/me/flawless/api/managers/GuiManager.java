package me.flawless.api.managers;

import me.flawless.FlawLess;
import me.flawless.api.utils.Wrapper;
import me.flawless.api.utils.math.FadeUtils;
import me.flawless.mod.gui.clickgui.ClickGuiScreen;
import me.flawless.mod.gui.clickgui.components.impl.ModuleComponent;
import me.flawless.mod.gui.clickgui.tabs.ClickGuiTab;
import me.flawless.mod.gui.clickgui.tabs.Tab;
import me.flawless.mod.gui.elements.ArmorHUD;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.Module.Category;
import me.flawless.mod.modules.impl.client.ClickGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class GuiManager implements Wrapper {

	public final ArrayList<ClickGuiTab> tabs = new ArrayList<>();
	public static final ClickGuiScreen clickGui = new ClickGuiScreen();
	public final ArmorHUD armorHud;
	public static Tab currentGrabbed = null;
	private int lastMouseX = 0;
	private int lastMouseY = 0;
	private int mouseX;
	private int mouseY;

	public GuiManager() {

		armorHud = new ArmorHUD();

		int xOffset = 30;
		for (Category category : Module.Category.values()) {
			ClickGuiTab tab = new ClickGuiTab(category, xOffset, 50);
			for (Module module : FlawLess.MODULE.modules) {
				if (module.getCategory() == category) {
					ModuleComponent button = new ModuleComponent(module.getName(), tab, module);
					tab.addChild(button);
				}
			}
			tabs.add(tab);
			xOffset += tab.getWidth() + 2;
		}
	}
	
	public Color getColor() {
		return ClickGui.INSTANCE.color.getValue();
	}
	
	public void update() {
		if (isClickGuiOpen()) {
			for (ClickGuiTab tab : tabs) {
				tab.update(mouseX, mouseY, ClickGuiScreen.clicked);
			}
			armorHud.update(mouseX, mouseY, ClickGuiScreen.clicked);
		}
	}

	public void draw(int x, int y, DrawContext drawContext, float tickDelta) {
		MatrixStack matrixStack = drawContext.getMatrices();
		boolean mouseClicked = ClickGuiScreen.clicked;
		this.mouseX = x;
		this.mouseY = y;
		if (this.isClickGuiOpen()) {
			int dx = (int) (double) mouseX;
			int dy = (int) (double) mouseY;
			if (!mouseClicked)
				currentGrabbed = null;
			if (currentGrabbed != null)
				currentGrabbed.moveWindow((lastMouseX - dx), (lastMouseY - dy));
			this.lastMouseX = dx;
			this.lastMouseY = dy;
		}
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		matrixStack.push();

		armorHud.draw(drawContext, tickDelta, getColor());
		if (isClickGuiOpen()) {
			double quad = ClickGui.fade.getQuad(FadeUtils.Quad.In2);
			boolean s = false;
			if (quad < 1) {
				switch (ClickGui.INSTANCE.mode.getValue()) {
					case Pull -> {
						quad = 1 - quad;
						matrixStack.translate(0, -100 * quad, 0);
					}
					case Scale -> matrixStack.scale((float) quad, (float) quad, 1);
					case Scissor -> {
						setScissorRegion(0, 0, mc.getWindow().getWidth(), (int) (mc.getWindow().getHeight() * quad));
						s = true;
					}
				}
			}
			for (ClickGuiTab tab : tabs) {
				tab.draw(drawContext, tickDelta, getColor());
			}
			if (s) {
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
			}
		}
		matrixStack.pop();

		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	public void setScissorRegion(int x, int y, int width, int height) {
		double scaledY = (mc.getWindow().getHeight() - (y + height));
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x, (int) scaledY, width, height);
	}
	public boolean isClickGuiOpen() {
		return mc.currentScreen instanceof ClickGuiScreen;
	}
}
