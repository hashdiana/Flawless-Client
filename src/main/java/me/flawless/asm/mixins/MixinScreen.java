package me.flawless.asm.mixins;

import me.flawless.api.utils.render.shader.Blur;
import me.flawless.mod.modules.impl.client.GameSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen {
    @Shadow public int width;
    @Shadow public int height;
    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    public void renderInGameBackgroundHook(DrawContext context, CallbackInfo ci) {
        ci.cancel();
        if (GameSetting.INSTANCE.blur.getValue()) {
            Blur.blur(0, 7);
        }
        if (GameSetting.INSTANCE.guiBackground.getValue()) {
            context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        }
        if (GameSetting.INSTANCE.customBackground.booleanValue) {
            context.fillGradient(0, 0, this.width, this.height, GameSetting.INSTANCE.customBackground.getValue().getRGB(), GameSetting.INSTANCE.endColor.getValue().getRGB());
        }
        //Blur.unblur();
    }
}
