package me.flawless.asm.mixins;

import me.flawless.FlawLess;
import me.flawless.api.events.Event;
import me.flawless.api.events.impl.TravelEvent;
import me.flawless.api.utils.Wrapper;
import me.flawless.mod.modules.impl.client.CombatSetting;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerEntity.class)
public class MixinPlayerEntity implements Wrapper {

    @Inject(method = "canChangeIntoPose", at = @At("RETURN"), cancellable = true)
    private void poseNotCollide(EntityPose pose, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == mc.player && !CombatSetting.INSTANCE.crawl.getValue() && pose == EntityPose.SWIMMING) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravelPre(Vec3d movementInput, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if(player != mc.player)
            return;

        TravelEvent event = new TravelEvent(Event.Stage.Pre, player);
        FlawLess.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("RETURN"), cancellable = true)
    private void onTravelPost(Vec3d movementInput, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if(player != mc.player)
            return;

        TravelEvent event = new TravelEvent(Event.Stage.Post, player);
        FlawLess.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
