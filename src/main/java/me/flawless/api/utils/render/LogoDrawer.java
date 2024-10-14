package me.flawless.api.utils.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class LogoDrawer {
	public static final Identifier LOGO_TEXTURE = new Identifier("textures/logo.png");

	public static void draw(DrawContext context, int screenWidth, int screenHeight, float alpha) {
		context.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
		int i = screenWidth / 2 - 75;
		int o = screenHeight / 4 - 75;
		context.drawTexture(LOGO_TEXTURE, i, o, 0.0F, 0.0F, 150, 150, 150, 150);
		context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
