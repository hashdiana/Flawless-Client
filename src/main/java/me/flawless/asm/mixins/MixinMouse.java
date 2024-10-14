package me.flawless.asm.mixins;

import me.flawless.FlawLess;
import me.flawless.mod.gui.clickgui.ClickGuiScreen;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.flawless.api.utils.Wrapper.mc;
@Mixin(Mouse.class)
public class MixinMouse {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouse(long window, int button, int action, int mods, CallbackInfo ci) {
        int key = -(button + 2);
        if (mc.currentScreen instanceof ClickGuiScreen && action == 1 && FlawLess.MODULE.setBind(key)) {
            return;
        }
        if (action == 1) {
            FlawLess.MODULE.onKeyPressed(key);
        }
        if (action == 0) {
            FlawLess.MODULE.onKeyReleased(key);
        }
    }
}
