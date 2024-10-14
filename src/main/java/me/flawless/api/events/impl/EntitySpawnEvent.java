package me.flawless.api.events.impl;

import me.flawless.api.events.Event;
import net.minecraft.entity.Entity;

public class EntitySpawnEvent extends Event {
    private final Entity entity;
    public EntitySpawnEvent(Entity entity) {
        super(Stage.Pre);
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}