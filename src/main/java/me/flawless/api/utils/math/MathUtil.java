package me.flawless.api.utils.math;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class MathUtil {
    public static Direction getFacingOrder(float yaw, float pitch) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.sin(f);
        float i = MathHelper.cos(f);
        float j = MathHelper.sin(g);
        float k = MathHelper.cos(g);
        boolean bl = j > 0.0F;
        boolean bl2 = h < 0.0F;
        boolean bl3 = k > 0.0F;
        float l = bl ? j : -j;
        float m = bl2 ? -h : h;
        float n = bl3 ? k : -k;
        float o = l * i;
        float p = n * i;
        Direction direction = bl ? Direction.EAST : Direction.WEST;
        Direction direction2 = bl2 ? Direction.UP : Direction.DOWN;
        Direction direction3 = bl3 ? Direction.SOUTH : Direction.NORTH;
        if (l > n) {
            if (m > o) {
                return direction2;
            } else {
                return direction;
            }
        } else if (m > p) {
            return direction2;
        } else {
            return direction3;
        }
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }
    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        return Math.min(value, max);
    }
    public static double square(double input) {
        return input * input;
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }
    public static double random(double min, double max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static float rad(float angle) {
        return (float) (angle * Math.PI / 180);
    }

    public static double interpolate(double previous, double current, float delta) {
        return previous + (current - previous) * (double) delta;
    }
}
