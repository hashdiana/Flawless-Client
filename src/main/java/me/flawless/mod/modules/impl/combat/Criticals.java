package me.flawless.mod.modules.impl.combat;

import io.netty.buffer.Unpooled;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.EnumSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module {
    public static Criticals INSTANCE;
    public Criticals() {
        super("Criticals", Category.Combat);
        INSTANCE = this;
    }

    public final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Packet));

    public enum Mode {
        NCP, Strict, Packet, LowPacket
    }

    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        Entity entity;
        if (event.getPacket() instanceof PlayerInteractEntityC2SPacket packet && getInteractType(packet) == InteractType.ATTACK && !((entity = getEntity(packet)) instanceof EndCrystalEntity)) {
            if ((mc.player.isOnGround() || mc.player.getAbilities().flying) && !mc.player.isInLava() && !mc.player.isSubmergedInWater() && entity != null) {
                mc.player.addCritParticles(entity);
                doCrit();
            }
        }
    }

    public void doCrit() {
        if (mode.getValue() == Mode.Strict && mc.world.getBlockState(mc.player.getBlockPos()).getBlock() != Blocks.COBWEB) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.062600301692775, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.07260029960661, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
        } else if (mode.getValue() == Mode.NCP) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0625D, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
        } else if (mode.getValue() == Mode.Packet) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.00001058293536, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.00000916580235, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.00000010371854, mc.player.getZ(), false));
        } else if (mode.getValue() == Mode.LowPacket) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.000000271875, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
        }
    }

    public static Entity getEntity(PlayerInteractEntityC2SPacket packet) {
        PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
        packet.write(packetBuf);
        return mc.world == null ? null : mc.world.getEntityById(packetBuf.readVarInt());
    }

    public static InteractType getInteractType(PlayerInteractEntityC2SPacket packet) {
        PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
        packet.write(packetBuf);

        packetBuf.readVarInt();
        return packetBuf.readEnumConstant(InteractType.class);
    }

    public enum InteractType {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}
