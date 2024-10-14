package me.flawless.api.managers;

import me.flawless.mod.modules.impl.misc.TimerModule;

public class TimerManager {

    public float timer = 1f;

    public void set(float factor) {
        if (factor < 0.1f) factor = 0.1f;
        timer = factor;
    }

    public float lastTime;
    public void reset() {
        timer = getDefault();
        lastTime = timer;
    }

    public void tryReset() {
        if (lastTime != getDefault()) {
            reset();
        }
    }

    public float get() {
        return timer;
    }

    public float getDefault() {
        return TimerModule.INSTANCE.isOn() ? TimerModule.INSTANCE.multiplier.getValueFloat() : 1f;
    }
}

