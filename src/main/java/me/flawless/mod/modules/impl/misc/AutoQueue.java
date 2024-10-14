package me.flawless.mod.modules.impl.misc;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.mod.modules.Module;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.HashMap;

public class AutoQueue extends Module {
    public static HashMap<String, String> asks = new HashMap<>(){
        {
            put("红石火把", "15");
            put("猪被闪电", "僵尸猪人");
            put("小箱子", "27");
            put("开服年份", "2020");
            put("定位末地遗迹", "0");
        }
    };

    public AutoQueue() {
        super("AutoQueue", Category.Misc);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof GameMessageS2CPacket packet) {
            for (String key : asks.keySet()) {
                if (packet.content().getString().contains(key)) {
                    String[] abc = new String[]{"A", "B", "C"};
                    for (String s : abc) {
                        if (packet.content().getString().contains(s + "." + asks.get(key))) {
                            mc.player.networkHandler.sendChatMessage(s.toLowerCase());
                            return;
                        }
                    }
                }
            }
        }
    }
}
