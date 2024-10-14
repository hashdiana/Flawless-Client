package me.flawless.mod.modules.impl.misc;

import me.flawless.FlawLess;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.DeathEvent;
import me.flawless.api.events.impl.TotemEvent;
import me.flawless.api.managers.CommandManager;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.client.ChatSetting;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import net.minecraft.entity.player.PlayerEntity;

public class PopCounter
        extends Module {

    public static PopCounter INSTANCE;
    public final BooleanSetting unPop =
            add(new BooleanSetting("Dead", true));
    public PopCounter() {
        super("PopCounter", "Counts players totem pops", Category.Misc);
        INSTANCE = this;
    }

    @EventHandler
    public void onPlayerDeath(DeathEvent event) {
        PlayerEntity player = event.getPlayer();
        if (FlawLess.POP.popContainer.containsKey(player.getName().getString())) {
            int l_Count = FlawLess.POP.popContainer.get(player.getName().getString());
            if (l_Count == 1) {
                if (player.equals(mc.player)) {
                    sendMessage("§fYou§r died after popping " + "§f" + l_Count + "§r totem.", player.getId());
                } else {
                    sendMessage("§f" + player.getName().getString() + "§r died after popping " + "§f" + l_Count + "§r totem.", player.getId());
                }
            } else {
                if (player.equals(mc.player)) {
                    sendMessage("§fYou§r died after popping " + "§f" + l_Count + "§r totems.", player.getId());
                } else {
                    sendMessage("§f" + player.getName().getString() + "§r died after popping " + "§f" + l_Count + "§r totems.", player.getId());
                }
            }
        } else if (unPop.getValue()) {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r died.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + "§r died.", player.getId());
            }
        }
    }

    @EventHandler
    public void onTotem(TotemEvent event) {
        PlayerEntity player = event.getPlayer();
        int l_Count = 1;
        if (FlawLess.POP.popContainer.containsKey(player.getName().getString())) {
            l_Count = FlawLess.POP.popContainer.get(player.getName().getString());
        }
        if (l_Count == 1) {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r popped " + "§f" + l_Count + "§r totem.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + " §rpopped " + "§f" + l_Count + "§r totems.", player.getId());
            }
        } else {
            if (player.equals(mc.player)) {
                sendMessage("§fYou§r popped " + "§f" + l_Count + "§r totem.", player.getId());
            } else {
                sendMessage("§f" + player.getName().getString() + " §rhas popped " + "§f" + l_Count + "§r totems.", player.getId());
            }
        }
    }
    
    public void sendMessage(String message, int id) {
        if (!nullCheck()) {
            if (ChatSetting.INSTANCE.messageStyle.getValue() == ChatSetting.Style.MoonGod) {
                CommandManager.sendChatMessageWidthId("§f[" + "§3" + getName() + "§f] " + message, id);
                return;
            }
            CommandManager.sendChatMessageWidthId("§6[!] " + message, id);
        }
    }
}

