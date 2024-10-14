package me.flawless.asm.mixins;

import me.flawless.FlawLess;
import me.flawless.api.utils.math.AnimateUtil;
import me.flawless.mod.modules.impl.client.ChatSetting;
import me.flawless.mod.modules.impl.client.GameSetting;
import me.flawless.mod.modules.impl.render.NoRender;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {



	@Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
	private void onRenderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.portal.getValue()) ci.cancel();
	}

	@Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
	private void onRenderStatusEffectOverlay(DrawContext context, CallbackInfo ci) {
		if (NoRender.INSTANCE.isOn() && NoRender.INSTANCE.potionsIcon.getValue()) ci.cancel();
	}

	@Inject(at = {@At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V", remap = false, ordinal = 3)}, method = {"render(Lnet/minecraft/client/gui/DrawContext;F)V"})
	private void onRender(DrawContext context, float tickDelta, CallbackInfo ci) {
		FlawLess.MODULE.render2D(context);
	}

	@Inject(method = "clear", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;clear(Z)V"), cancellable = true)
	private void onClear(CallbackInfo info) {
		if (ChatSetting.INSTANCE.isOn() && ChatSetting.INSTANCE.keepHistory.getValue()) {
			info.cancel();
		}
	}

	@Unique
	private double hotbarX = 0;
	@ModifyArg(method="renderHotbar",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V",ordinal = 1),index = 1)
	private int selectedSlotX(int x){
		if (GameSetting.INSTANCE.hotbar()) {
			hotbarX = AnimateUtil.animate(hotbarX, x, GameSetting.INSTANCE.hotbarSpeed.getValue(), GameSetting.INSTANCE.animMode.getValue());
			return (int) hotbarX;
		}
		return(x);
	}
}