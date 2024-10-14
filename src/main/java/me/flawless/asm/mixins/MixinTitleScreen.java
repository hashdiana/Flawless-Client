package me.flawless.asm.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import me.flawless.FlawLess;
import me.flawless.api.utils.render.LogoDrawer;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsNotificationsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
    @Final
    @Shadow
    public static Text COPYRIGHT;
    @Final
    @Shadow
    private static Identifier PANORAMA_OVERLAY;
    @Shadow
    private RealmsNotificationsScreen realmsNotificationGui;
    @Final
    @Shadow
    private RotatingCubeMapRenderer backgroundRenderer;
    @Final
    @Shadow
    private boolean doBackgroundFade;
    @Shadow
    private long backgroundFadeStart;
    @Shadow
    private TitleScreen.DeprecationNotice deprecationNotice;

    protected MixinTitleScreen(Text title) {
        super(title);
    }

    /*
    @Overwrite
    private void initWidgetsNormal(int y, int spacingY) {
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.singleplayer"), (button) -> {
            this.client.setScreen(new SelectWorldScreen(this));
        }).dimensions(this.width / 2 - 100, y, 200, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.multiplayer"), (button) -> {
            Screen screen = this.client.options.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this);
            this.client.setScreen(screen);
        }).dimensions(this.width / 2 - 100, y + spacingY, 200, 20).build()).active = true;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.online"), (button) -> {
            this.switchToRealms();
        }).dimensions(this.width / 2 - 100, y + spacingY * 2, 200, 20).build()).active = true;
    }*/


    /**
     * @author me
     * @reason titleScreen
     */
    @Overwrite
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.backgroundFadeStart == 0L && this.doBackgroundFade) {
            this.backgroundFadeStart = Util.getMeasuringTimeMs();
        }

        float f = this.doBackgroundFade ? (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;

        FlawLess.SHADER.applyFlow(() -> this.backgroundRenderer.render(delta, MathHelper.clamp(f, 0.0F, 1.0F)));
        RenderSystem.enableBlend();
        context.setShaderColor(1.0F, 1.0F, 1.0F, this.doBackgroundFade ? (float) MathHelper.ceil(MathHelper.clamp(f, 0.0F, 1.0F)) : 1.0F);
        context.drawTexture(PANORAMA_OVERLAY, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        LogoDrawer.draw(context, this.width, this.height, g);
        int i = MathHelper.ceil(g * 255.0F) << 24;
        if ((i & -67108864) != 0) {
            if (this.deprecationNotice != null) {
                this.deprecationNotice.render(context, i);
            }

            String string = "Minecraft " + SharedConstants.getGameVersion().getName();
            if (this.client.isDemo()) {
                string = string + " Demo";
            }
            string = string + "/§b" + FlawLess.LOG_NAME;

            context.drawTextWithShadow(this.textRenderer, string, 2, this.height - 10, 16777215 | i);

            for (Element element : this.children()) {
                if (element instanceof ClickableWidget) {
                    ((ClickableWidget) element).setAlpha(g);
                }
            }

            super.render(context, mouseX, mouseY, delta);
            if (this.isRealmsNotificationsGuiDisplayed() && g >= 1.0F) {
                RenderSystem.enableDepthTest();
                this.realmsNotificationGui.render(context, mouseX, mouseY, delta);
            }

        }
    }

    @Shadow
    private void switchToRealms() {
        this.client.setScreen(new RealmsMainScreen(this));
    }
    @Shadow
    private boolean isRealmsNotificationsGuiDisplayed() {
        return this.realmsNotificationGui != null;
    }

    @Shadow
    private void initWidgetsDemo(int y, int spacingY) {
    }
}
