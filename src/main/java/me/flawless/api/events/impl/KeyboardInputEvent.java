package me.flawless.api.events.impl;

import me.flawless.api.events.Event;

public class KeyboardInputEvent extends Event {
    public KeyboardInputEvent() {
        super(Stage.Post);
    }
}
