package me.flawless.api.managers;

import me.flawless.FlawLess;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.utils.Wrapper;
import me.flawless.api.utils.math.MathUtil;
import me.flawless.api.utils.math.Timer;
import me.flawless.api.utils.render.JelloUtil;
import me.flawless.mod.modules.impl.client.Fonts;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;

public class ServerManager implements Wrapper {

    public ServerManager() {
        FlawLess.EVENT_BUS.subscribe(this);
    }
    private final Timer timeDelay = new Timer();
    private final ArrayDeque<Float> tpsResult = new ArrayDeque<>(20);
    private long time;
    private long tickTime;
    private float tps;

    public float getTPS() {
        return round2(tps);
    }

    public float getCurrentTPS() {
        return round2(20.0f * ((float) tickTime / 1000f));
    }

    public float getTPSFactor() {
        return (float) tickTime / 1000f;
    }

    public static float round2(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ChatMessageS2CPacket)) {
            timeDelay.reset();
        }
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            if (time != 0L) {
                tickTime = System.currentTimeMillis() - time;

                if (tpsResult.size() > 20)
                    tpsResult.poll();

                tpsResult.add(20.0f * (1000.0f / (float) (tickTime)));

                float average = 0.0f;

                for (Float value : tpsResult) average += MathUtil.clamp(value, 0f, 20f);

                tps = average / (float) tpsResult.size();
            }
            time = System.currentTimeMillis();
        }
    }

    boolean worldNull = true;

    public void run() {
        JelloUtil.updateJello();
        if (worldNull && mc.world != null) {
            Fonts.INSTANCE.enable();
            FlawLess.MODULE.onLogin();
            worldNull = false;
        } else if (!worldNull && mc.world == null) {
            FlawLess.save();
            FlawLess.MODULE.onLogout();
            worldNull = true;
        }
    }
}
