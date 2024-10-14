package me.flawless.api.events.impl;

import me.flawless.api.events.Event;

public class UpdateWalkingPlayerEvent extends Event {
    private boolean cancelRotate = false;
    public UpdateWalkingPlayerEvent(Stage stage) {
        super(stage);
    }

    public void cancelRotate() {
        setCancelRotate(true);
    }
    public void setCancelRotate(boolean cancelRotate) {
        this.cancelRotate = cancelRotate;
    }

    public boolean isCancelRotate() {
        return cancelRotate;
    }
}
