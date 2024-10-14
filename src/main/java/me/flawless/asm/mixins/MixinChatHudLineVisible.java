package me.flawless.asm.mixins;

import me.flawless.api.interfaces.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.Visible.class)
public class MixinChatHudLineVisible implements IChatHudLine {
    @Unique
    private int id = 0;
    @Override
    public int flawless$getId() {
        return id;
    }

    @Override
    public void flawless$setId(int id) {
        this.id = id;
    }
}
