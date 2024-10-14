package me.flawless.mod.modules.impl.movement;

import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.EnumSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class NoSlow extends Module {
    public static NoSlow INSTANCE;
    private final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.InvMove));
    private final BooleanSetting active = add(new BooleanSetting("Active", true, v -> page.getValue() == Page.InvMove));
    private final BooleanSetting sneak = add(new BooleanSetting("Sneak", false, v -> page.getValue() == Page.InvMove));
    //private final BooleanSetting strict = add(new BooleanSetting("Strict", false, v -> page.getValue() == Page.InvMove));
    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Vanilla, v -> page.getValue() == Page.NoSlow));
    public enum Page {
        InvMove,
        NoSlow
    }

    public enum Mode {
        Vanilla,
        NCP,
        None
    }
    public NoSlow() {
        super("NoSlow", Category.Movement);
        INSTANCE = this;
    }
    
    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.NCP && mc.player.isUsingItem() && !mc.player.isRiding() && !mc.player.isFallFlying()) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
        if (active.getValue()) {
            if (!(mc.currentScreen instanceof ChatScreen)) {
                for (KeyBinding k : new KeyBinding[]{mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey}) {
                    k.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
                }

                mc.options.forwardKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.forwardKey.getBoundKeyTranslationKey()).getCode()));
                mc.options.sprintKey.setPressed(Sprint.INSTANCE.isOn() || InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sprintKey.getBoundKeyTranslationKey()).getCode()));

                if (sneak.getValue()) {
                    mc.options.sneakKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.sneakKey.getBoundKeyTranslationKey()).getCode()));
                }
            }
        }
    }

/*    @EventHandler
    public void onPacketSend(PacketEvent.Send e) {
        if (e.getPacket() instanceof ClickSlotC2SPacket) {
            if (strict.getValue() && active.getValue() && mc.player.isOnGround() && MovementUtil.isMoving() && !mc.world.getBlockCollisions(mc.player, mc.player.getBoundingBox().offset(0.0, 0.0656, 0.0)).iterator().hasNext()) {
                if (mc.player.isSprinting()) {
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                }
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.0656, mc.player.getZ(), false));
            }
        }
    }

    @EventHandler
    public void onPacketSendPost(PacketEvent.SendPost e) {
        if (strict.getValue() && active.getValue() && e.getPacket() instanceof ClickSlotC2SPacket) {
            if (mc.player.isSprinting()) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            }
        }
    }*/
    public boolean noSlow() {
        return isOn() && mode.getValue() != Mode.None;
    }
}
