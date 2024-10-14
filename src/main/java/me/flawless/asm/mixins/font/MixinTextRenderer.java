package me.flawless.asm.mixins.font;

import me.flawless.mod.modules.impl.client.GameSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TextRenderer.class)
public class MixinTextRenderer {

    /**
     * @author me
     * @reason no shadow
     */
    @Overwrite
    private int drawInternal(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int backgroundColor, int light) {
        if (shadow) {
            shadow = GameSetting.shadow();
        }
        color = tweakTransparency(color);
        Matrix4f matrix4f = new Matrix4f(matrix);
        if (shadow) {
            this.drawLayer(text, x, y, color, true, matrix, vertexConsumerProvider, layerType, backgroundColor, light);
            matrix4f.translate(FORWARD_SHIFT);
        }

        x = this.drawLayer(text, x, y, color, false, matrix4f, vertexConsumerProvider, layerType, backgroundColor, light);
        return (int)x + (shadow ? 1 : 0);
    }

    /**
     * @author me
     * @reason no shadow
     */
    @Overwrite
    private int drawInternal(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColor, int light, boolean mirror) {
        if (shadow) {
            shadow = GameSetting.shadow();
        }
        if (mirror) {
            text = this.mirror(text);
        }

        color = tweakTransparency(color);
        Matrix4f matrix4f = new Matrix4f(matrix);
        if (shadow) {
            this.drawLayer(text, x, y, color, true, matrix, vertexConsumers, layerType, backgroundColor, light);
            matrix4f.translate(FORWARD_SHIFT);
        }

        x = this.drawLayer(text, x, y, color, false, matrix4f, vertexConsumers, layerType, backgroundColor, light);
        return (int)x + (shadow ? 1 : 0);
    }

    @Shadow
    private float drawLayer(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int underlineColor, int light) {
        return 0;
    }
    @Final
    @Shadow
    private static Vector3f FORWARD_SHIFT;
    @Shadow
    private float drawLayer(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int underlineColor, int light) {
        return 0;
    }
    @Shadow
    public String mirror(String text) {
        return null;
    }

    @Shadow
    private static int tweakTransparency(int argb) {
        return (argb & -67108864) == 0 ? argb | -16777216 : argb;
    }
}
