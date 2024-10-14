package me.flawless.api.interfaces;

import net.minecraft.client.gl.Framebuffer;

public interface IShaderEffect {
    void flawless$addFakeTargetHook(String name, Framebuffer buffer);
}