package me.flawless.api.managers;

import me.flawless.FlawLess;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.utils.math.FadeUtils;
import me.flawless.api.utils.Wrapper;
import me.flawless.mod.modules.impl.render.BreakESP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;

public class MineManager implements Wrapper {
    public MineManager() {
        FlawLess.EVENT_BUS.subscribe(this);
    }
    public final HashMap<Integer, BreakData> breakMap = new HashMap<>();

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof BlockBreakingProgressS2CPacket packet) {
            if (packet.getPos() == null) return;
            BreakData breakData = new BreakData(packet.getPos(), packet.getEntityId());
            if (breakMap.containsKey(packet.getEntityId()) && breakMap.get(packet.getEntityId()).pos.equals(packet.getPos())) {
                return;
            }
            if (breakData.getEntity() == null) {
                return;
            }
            if (MathHelper.sqrt((float) breakData.getEntity().getEyePos().squaredDistanceTo(packet.getPos().toCenterPos())) > 8) {
                return;
            }
            breakMap.put(packet.getEntityId(), breakData);
        }
    }

    public boolean isMining(BlockPos pos) {
        boolean mining = false;

        for (BreakData breakData : new HashMap<>(breakMap).values()) {
            if (breakData.getEntity() == null) {
                continue;
            }
            if (breakData.getEntity().getEyePos().distanceTo(pos.toCenterPos()) > 7) {
                continue;
            }
            if (breakData.pos.equals(pos)) {
                mining = true;
                break;
            }
        }

        return mining;
    }
    public static class BreakData {
        public final BlockPos pos;
        public final int entityId;
        public final FadeUtils fade;
        public BreakData(BlockPos pos, int entityId) {
            this.pos = pos;
            this.entityId = entityId;
            this.fade = new FadeUtils((long) BreakESP.INSTANCE.animationTime.getValue());
        }

        public Entity getEntity() {
            if (mc.world == null) return null;
            Entity entity = mc.world.getEntityById(entityId);
            if (entity instanceof PlayerEntity) {
                return entity;
            }
            return null;
        }
    }
}
