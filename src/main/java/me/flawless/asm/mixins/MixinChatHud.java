package me.flawless.asm.mixins;

import me.flawless.api.interfaces.IChatHud;
import me.flawless.api.interfaces.IChatHudLine;
import me.flawless.api.managers.CommandManager;
import me.flawless.api.utils.math.FadeUtils;
import me.flawless.api.utils.render.ColorUtil;
import me.flawless.api.utils.render.TextUtil;
import me.flawless.mod.modules.impl.client.ChatSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
@Mixin(ChatHud.class)
public abstract class MixinChatHud implements IChatHud {
    @Shadow
    @Final
    private List<ChatHudLine.Visible> visibleMessages;
    @Shadow
    @Final
    private List<ChatHudLine> messages;
    @Unique
    private int next = 0;
    @Shadow
    public abstract void addMessage(Text message);

    @Override
    public void flawless$add(Text message, int id) {
        next = id;
        addMessage(message);
        next = 0;
    }
    
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void onAddMessageAfterNewChatHudLineVisible(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo info) {
        ((IChatHudLine) (Object) visibleMessages.get(0)).flawless$setId(next);
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void onAddMessageAfterNewChatHudLine(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo info) {
        ((IChatHudLine) (Object) messages.get(0)).flawless$setId(next);
    }
    
    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V")
    private void onAddMessage(Text message, @Nullable MessageSignatureData signature, int ticks, @Nullable MessageIndicator indicator, boolean refresh, CallbackInfo info) {
       if (next != 0) {
           visibleMessages.removeIf(msg -> msg == null || ((IChatHudLine) (Object) msg).flawless$getId() == next);
           messages.removeIf(msg -> msg == null || ((IChatHudLine) (Object) msg).flawless$getId() == next);
       }
    }

    @Redirect(method = {"addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"},
            at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 2, remap = false))
    public int chatLinesSize(List<ChatHudLine.Visible> list) {
        return ChatSetting.INSTANCE.isOn() && ChatSetting.INSTANCE.infiniteChat.getValue() ? -2147483647 : list.size();
    }

    @Redirect(method = {"render"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
    private int drawStringWithShadow(DrawContext drawContext, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
        if (ChatSetting.chatMessage.containsKey(text) && ChatSetting.chatMessage.get(text).getString().startsWith(CommandManager.syncCode)) {
            if (ChatSetting.INSTANCE.pulse.booleanValue) {
                return TextUtil.drawStringPulse(drawContext, text, x, y, ColorUtil.injectAlpha(ChatSetting.INSTANCE.color.getValue(), ((color >> 24) & 0xff)),ColorUtil.injectAlpha(ChatSetting.INSTANCE.pulse.getValue(), ((color >> 24) & 0xff)), ChatSetting.INSTANCE.pulseSpeed.getValue(), ChatSetting.INSTANCE.pulseCounter.getValueInt());
            }
            return drawContext.drawTextWithShadow(textRenderer, text, x, y, ColorUtil.injectAlpha(ChatSetting.INSTANCE.color.getValue(), ((color >> 24) & 0xff)).getRGB());
        }
        return drawContext.drawTextWithShadow(textRenderer, text, x, y, color);
    }
    @Unique
    private final HashMap<ChatHudLine.Visible, FadeUtils> map = new HashMap<>();
    @Unique
    private ChatHudLine.Visible last;

    @ModifyArg(method = {"render"}, at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0, remap = false))
    private int get(int i) {
        last = visibleMessages.get(i);
        if (last != null && !map.containsKey(last)) {
            map.put(last, new FadeUtils(ChatSetting.INSTANCE.animateTime.getValueInt()));
        }
        return i;
    }

    @Inject(method = {"render"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I", ordinal = 0, shift = At.Shift.BEFORE)})
    private void translate(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (map.containsKey(last)) {
            context.getMatrices().translate(ChatSetting.INSTANCE.animateOffset.getValue() * (1 - map.get(last).getQuad(ChatSetting.INSTANCE.animQuad.getValue())), 0.0, 0.0f);
        }
    }
}
