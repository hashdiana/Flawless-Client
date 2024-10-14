package me.flawless.mod.modules.impl.misc;

import me.flawless.FlawLess;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.entity.MovementUtil;
import me.flawless.api.utils.math.FadeUtils;
import me.flawless.api.utils.math.Timer;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.ColorSetting;
import me.flawless.mod.modules.settings.impl.EnumSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

import java.awt.*;
import java.text.DecimalFormat;

public class TimerModule extends Module {
	public final SliderSetting multiplier = add(new SliderSetting("Speed", 1, 0.1, 5, 0.01));
	private final BooleanSetting tickShift = add(new BooleanSetting("TickShift", true).setParent());
	private final SliderSetting shiftTimer = add(new SliderSetting("ShiftTimer", 2, 1, 10, 0.1, v -> tickShift.isOpen()));
	private final SliderSetting accumulate = add(new SliderSetting("Charge", 2000f, 1f, 10000f, 50f, v -> tickShift.isOpen()).setSuffix("ms"));
	private final SliderSetting minAccumulate = add(new SliderSetting("MinCharge", 500f, 1f, 10000f, 50f, v -> tickShift.isOpen()).setSuffix("ms"));
	private final BooleanSetting smooth = add(new BooleanSetting("Smooth", true, v -> tickShift.isOpen()).setParent());
	private final EnumSetting<FadeUtils.Quad> quad = add(new EnumSetting<>("Quad", FadeUtils.Quad.In, v -> smooth.isOpen() && tickShift.isOpen()));
	private final BooleanSetting reset = add(new BooleanSetting("Reset", true, v -> tickShift.isOpen()));
	private final BooleanSetting indicator = add(new BooleanSetting("Indicator", true, v -> tickShift.isOpen()).setParent());
	private final ColorSetting work = add(new ColorSetting("Completed", new Color(0, 255, 0), v -> indicator.isOpen() && tickShift.isOpen()));
	private final ColorSetting charging = add(new ColorSetting("Charging", new Color(255, 0, 0), v -> indicator.isOpen() && tickShift.isOpen()));
	private final SliderSetting yOffset = add(new SliderSetting("YOffset", 0, -200, 200, 1, v -> indicator.isOpen() && tickShift.isOpen()));
	public static TimerModule INSTANCE;
	public TimerModule() {
		super("Timer", Category.Misc);
		this.setDescription("Increases the speed of Minecraft.");
		INSTANCE = this;
	}

	@Override
	public void onDisable() {
		FlawLess.TIMER.reset();
	}

	@Override
	public void onUpdate() {
		FlawLess.TIMER.tryReset();
	}

	@Override
	public void onEnable() {
		FlawLess.TIMER.reset();
	}

	private final Timer timer = new Timer();
	private final Timer timer2 = new Timer();
	static DecimalFormat df = new DecimalFormat("0.0");
	private final FadeUtils end = new FadeUtils(500);

	long lastMs = 0;
	boolean moving = false;
	@Override
	public void onRender2D(DrawContext drawContext, float tickDelta) {
		if (!tickShift.getValue()) return;
		timer.setMs(Math.min(Math.max(0, timer.getPassedTimeMs()), accumulate.getValueInt()));
		if (MovementUtil.isMoving() && !EntityUtil.isInsideBlock()) {

			if (!moving) {
				if (timer.passedMs(minAccumulate.getValue())) {
					timer2.reset();
					lastMs = timer.getPassedTimeMs();
				} else {
					lastMs = 0;
				}
				moving = true;
			}

			timer.reset();

			if (timer2.passed(lastMs)) {
				FlawLess.TIMER.reset();
			} else {
				if (smooth.getValue()) {
					double timer = FlawLess.TIMER.getDefault() + (1 - end.getQuad(quad.getValue())) * (shiftTimer.getValueFloat() - 1) * (lastMs / accumulate.getValue());
					FlawLess.TIMER.set((float) Math.max(FlawLess.TIMER.getDefault(), timer));
				} else {
					FlawLess.TIMER.set(shiftTimer.getValueFloat());
				}
			}
		} else {
			if (moving) {
				FlawLess.TIMER.reset();
				if (reset.getValue()) {
					timer.reset();
				} else {
					timer.setMs(Math.max(lastMs - timer2.getPassedTimeMs(), 0));
				}
				moving = false;
			}
			end.setLength(timer.getPassedTimeMs());
			end.reset();
		}

		if (indicator.getValue()) {
			double current = (moving ? (Math.max(lastMs - timer2.getPassedTimeMs(), 0)) : timer.getPassedTimeMs());
			boolean completed = moving && current > 0 || current >= minAccumulate.getValueInt();
			double max = accumulate.getValue();
			String text = df.format(current / max * 100L) + "%";
			drawContext.drawText(mc.textRenderer, text, mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(text) / 2, mc.getWindow().getScaledHeight() / 2 + mc.textRenderer.fontHeight - yOffset.getValueInt(), completed ? this.work.getValue().getRGB() : this.charging.getValue().getRGB(), true);
		}
	}

	@Override
	public String getInfo() {
		if (!tickShift.getValue()) return null;
		double current = (moving ? (Math.max(lastMs - timer2.getPassedTimeMs(), 0)) : timer.getPassedTimeMs());
		double max = accumulate.getValue();
		double value = Math.min(current / max * 100, 100);
		return df.format(value) + "%";
	}

	@EventHandler
	public void onReceivePacket(PacketEvent.Receive event) {
		if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
			lastMs = 0;
		}
	}
}