package me.flawless.asm.mixins.chathud;

import com.llamalad7.mixinextras.sugar.Local;
import me.flawless.api.utils.math.FadeUtils;
import me.flawless.mod.modules.impl.client.ChatSetting;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatHud.class)
public class ChatHudMixin {
	@Shadow private int scrolledLines;
	@Shadow @Final private List<ChatHudLine.Visible> visibleMessages;
	@Shadow private int getLineHeight() { return 0; }
	@Shadow public int getWidth() { return 0; }

	@Unique private final ArrayList<FadeUtils> messageTimestamps = new ArrayList<>();
	@Unique private float fadeTime = 150;

	@Unique private int chatLineIndex;
	@Unique private int chatDisplacementY = 0;

	@Inject(method = "render", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/hud/ChatHudLine$Visible;addedTime()I"
	))
	public void getChatLineIndex(CallbackInfo ci, @Local(ordinal = 13) int chatLineIndex) {
		// Capture which chat line is currently being rendered
		this.chatLineIndex = chatLineIndex;
	}

	@Unique
	private void calculateYOffset() {
		// Calculate current required offset to achieve slide in from bottom effect
		try {
			int lineHeight = this.getLineHeight();
			float maxDisplacement = (float)lineHeight;// * fadeOffsetYScale;
			double quad = messageTimestamps.get(chatLineIndex).getQuad(FadeUtils.Quad.In2);
			if (chatLineIndex == 0 && quad < 1 && this.scrolledLines == 0) {
				chatDisplacementY = (int)(maxDisplacement - quad *maxDisplacement);
			}
		} catch (Exception ignored) {}
	}

	@ModifyArg(method = "render", index = 1, at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
			ordinal = 1
	))
	private float applyYOffset(float y) {
		fadeTime = ChatSetting.INSTANCE.fadeTime.getValueFloat();
		if (ChatSetting.INSTANCE.yAnim.getValue()) {
			calculateYOffset();
/*
			if (FabricLoader.getInstance().getObjectShare().get("raised:hud") instanceof Integer distance) {
				y -= distance;
			} else if (FabricLoader.getInstance().getObjectShare().get("raised:distance") instanceof Integer distance) {
				y -= distance;
			}
*/
			return y + chatDisplacementY;
		} else {
			return y;
		}
	}

	@ModifyVariable(method = "render", ordinal = 3, at = @At(
			value = "STORE"
	))
	private double modifyOpacity(double originalOpacity) {
		double opacity = originalOpacity;
		if (ChatSetting.INSTANCE.fade.getValue()) {
			// Calculate current required opacity for currently rendered line to achieve fade in effect
			try {
				double quad = messageTimestamps.get(chatLineIndex).getQuad(FadeUtils.Quad.In2);
				if (quad < 1 && this.scrolledLines == 0) {
					opacity = opacity * (0.5 + MathHelper.clamp(quad, 0, 1) / 2);
				}
			} catch (Exception ignored) {
			}
		}
		return opacity;
	}

	@ModifyVariable(method = "render", at = @At(
			value = "STORE"
	))
	private MessageIndicator removeMessageIndicator(MessageIndicator messageIndicator) {
		if (ChatSetting.INSTANCE.hideIndicator.getValue()) {
			return null;
		}
		return messageIndicator;
	}

	@Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("TAIL"))
	private void addMessage(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
		messageTimestamps.add(0, new FadeUtils((long) fadeTime));
		while (this.messageTimestamps.size() > this.visibleMessages.size()) {
			this.messageTimestamps.remove(this.messageTimestamps.size() - 1);
		}
	}
}