package me.flawless.mod.modules.impl.player;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.mod.gui.clickgui.ClickGuiScreen;
import me.flawless.mod.modules.Module;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class XCarry extends Module {
    public XCarry() {
        super("XCarry", Category.Player);
    }

    Screen lastScreen = null;
    @Override
    public void onUpdate() {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GameMenuScreen) && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof ClickGuiScreen)) lastScreen = mc.currentScreen;
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (lastScreen instanceof InventoryScreen && event.getPacket() instanceof CloseHandledScreenC2SPacket) {
            event.cancel();
        }
    }
}