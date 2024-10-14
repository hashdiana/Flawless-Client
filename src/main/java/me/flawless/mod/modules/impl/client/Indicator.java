package me.flawless.mod.modules.impl.client;

import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.render.Render2DUtil;
import me.flawless.api.utils.world.BlockUtil;
import me.flawless.mod.gui.font.FontRenderers;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.impl.combat.*;
import me.flawless.mod.modules.impl.player.Flatten;
import me.flawless.mod.modules.impl.movement.Speed;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class Indicator extends Module {
    public static Indicator INSTANCE;

    public Indicator() {
        super("Indicator", Category.Client);
        INSTANCE = this;
    }

    MatrixStack matrixStack;
    float offset;
    float height;

    @Override
    public void onRender2D(DrawContext drawContext, float tickDelta) {
        if (nullCheck()) return;
        matrixStack = drawContext.getMatrices();
        height = FontRenderers.Calibri.getFontHeight();
        offset = 0;
        if (Burrow.INSTANCE.isOn()) {
            draw("BURROW", HoleKick.isInWeb(mc.player) ? ColorType.Red : ColorType.Green);
        }
        if (BlockUtil.isHole(EntityUtil.getPlayerPos(true))) {
            draw("SAFE",ColorType.Green);
        } else {
            draw("UNSAFE",ColorType.Red);
        }
        if (Speed.INSTANCE.isOn()) {
            draw("BHOP", ColorType.White);
        }
        if (HoleKick.INSTANCE.isOn()) {
            draw("PUSH", ColorType.White);
        }
        if (AutoTrap.INSTANCE.isOn()) {
            draw("TRAP", ColorType.White);
        }
        if (Flatten.INSTANCE.isOn()) {
            draw("FLAT", ColorType.White);
        }
        if (AutoCity.INSTANCE.isOn()) {
            draw("CITY", ColorType.White);
        }
        if (Surround.INSTANCE.isOn()) {
            draw("DEF", ColorType.White);
        }
        if (AutoWeb.INSTANCE.isOn()) {
            draw("AW", ColorType.White);
        }
        if (AutoCrystal.INSTANCE.isOn()) {
            draw("AC", (AutoCrystal.INSTANCE.displayTarget != null && AutoCrystal.INSTANCE.lastDamage > 0) ? ColorType.Green : ColorType.Red);
        }
        if (AutoAnchor.INSTANCE.isOn()) {
            draw("AN", (AutoAnchor.INSTANCE.displayTarget != null && AutoAnchor.INSTANCE.currentPos != null) ? ColorType.Green : ColorType.Red);
        }
    }
    
    private void draw(String s, ColorType type) {
        int color = -1;
        if (type == ColorType.Red) {
            color = new Color(255, 0, 0).getRGB();
        }
        if (type == ColorType.Green) {
            color = new Color(47, 173, 26).getRGB();
        }

        double width = FontRenderers.Calibri.getWidth(s) + 8;
        Render2DUtil.horizontalGradient(matrixStack, 10, mc.getWindow().getScaledHeight() - 200 + offset, (float) (10 + width / 2), mc.getWindow().getScaledHeight() - 200 + offset + height, new Color(0, 0, 0, 0), new Color(0, 0, 0, 100));
        Render2DUtil.horizontalGradient(matrixStack, (float) (10 + width / 2), mc.getWindow().getScaledHeight() - 200 + offset, (float) (10 + width), mc.getWindow().getScaledHeight() - 200 + offset + height, new Color(0, 0, 0, 100), new Color(0, 0, 0, 0));

        FontRenderers.Calibri.drawString(matrixStack, s, 14, mc.getWindow().getScaledHeight() - 195 + offset, color);
        offset -= height + 3;
    }

    private enum ColorType {
        Red,
        Green,
        White
    }
}
