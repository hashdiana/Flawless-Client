package me.flawless.asm.mixins;

import me.flawless.FlawLess;
import me.flawless.api.events.impl.SendMessageEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow
    public abstract void sendChatMessage(String content);

    @Unique
    private boolean ignore;

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (ignore) return;
        if (message.startsWith(FlawLess.PREFIX)) {
            FlawLess.COMMAND.command(message.split(" "));
            ci.cancel();
        } else {
            SendMessageEvent event = new SendMessageEvent(message);
            FlawLess.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
            } else if (!event.message.equals(event.defaultMessage)) {
                ignore = true;
                sendChatMessage(event.message);
                ignore = false;
                ci.cancel();
            }
        }
    }
}
