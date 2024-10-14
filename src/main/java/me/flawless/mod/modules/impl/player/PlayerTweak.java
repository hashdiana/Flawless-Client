package me.flawless.mod.modules.impl.player;

import me.flawless.api.utils.entity.InventoryUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;

public class PlayerTweak extends Module {

    public static PlayerTweak INSTANCE;
    public final BooleanSetting noEntityTrace =
            add(new BooleanSetting("NoEntityTrace", true).setParent());
    public final BooleanSetting onlyPickaxe =
            add(new BooleanSetting("OnlyPickaxe", true, v -> noEntityTrace.isOpen()));
    public final BooleanSetting multiTask =
            add(new BooleanSetting("MultiTask", true));
    public final BooleanSetting respawn =
            add(new BooleanSetting("Respawn", true));
    private final BooleanSetting noAbort =
            add(new BooleanSetting("NoMineAbort", false));
    private final BooleanSetting noReset =
            add(new BooleanSetting("NoMineReset", false));
    private final BooleanSetting noDelay =
            add(new BooleanSetting("NoMineDelay", false));
    private final BooleanSetting pickaxeSwitch =
            add(new BooleanSetting("SwitchEat", false));
    public final BooleanSetting ghostHand =
            add(new BooleanSetting("IgnoreBedrock", false));
    private final BooleanSetting reach =
            add(new BooleanSetting("Reach", false));
    public final SliderSetting distance = add(new SliderSetting("Distance", 5, 1, 15, 0.1, v -> reach.getValue()));
    private final SliderSetting delay = add(new SliderSetting("UseDelay", 4, 0, 4, 1));
    public PlayerTweak() {
        super("PlayerTweak", Category.Player);
        INSTANCE = this;
    }

    boolean swapped = false;
    int lastSlot = 0;

    @Override
    public void onUpdate() {
        if (nullCheck()) return;
        if (respawn.getValue() && mc.currentScreen instanceof DeathScreen) {
            mc.player.requestRespawn();
            mc.setScreen(null);
        }
        if (mc.itemUseCooldown <= 4 - delay.getValueInt()) {
            mc.itemUseCooldown = 0;
        }
        if (pickaxeSwitch.getValue()) {
            if (!(mc.player.getMainHandStack().getItem() instanceof PickaxeItem) && mc.player.getMainHandStack().getItem() != Items.ENCHANTED_GOLDEN_APPLE) {
                swapped = false;
                return;
            }
            int gapple = InventoryUtil.findItem(Items.ENCHANTED_GOLDEN_APPLE);
            if (gapple == -1) {
                if (swapped) {
                    InventoryUtil.switchToSlot(lastSlot);
                    swapped = false;
                }
                return;
            }
            if (mc.options.useKey.isPressed()) {
                if (mc.player.getMainHandStack().getItem() instanceof PickaxeItem && mc.player.getOffHandStack().getItem() != Items.ENCHANTED_GOLDEN_APPLE) {
                    lastSlot = mc.player.getInventory().selectedSlot;
                    InventoryUtil.switchToSlot(gapple);
                    swapped = true;
                }
            } else if (swapped) {
                InventoryUtil.switchToSlot(lastSlot);
                swapped = false;
            }
        }
    }

    public boolean isActive;

    @Override
    public void onDisable() {
        isActive = false;
    }

    public boolean reach() {
        return isOn() && reach.getValue();
    }
    public boolean noAbort() {
        return isOn() && noAbort.getValue() && !mc.options.useKey.isPressed();
    }

    public boolean noReset() {
        return isOn() && noReset.getValue();
    }
    public boolean noDelay() {
        return isOn() && noDelay.getValue();
    }
    public boolean multiTask() {
        return isOn() && multiTask.getValue();
    }

    public boolean noEntityTrace() {
        if (isOff() || !noEntityTrace.getValue()) return false;

        if (onlyPickaxe.getValue()) {
            return mc.player.getMainHandStack().getItem() instanceof PickaxeItem || mc.player.isUsingItem() && !(mc.player.getMainHandStack().getItem() instanceof SwordItem);
        }
        return true;
    }

    public boolean ghostHand() {
        return isOn() && ghostHand.getValue() && !mc.options.useKey.isPressed() && !mc.options.sneakKey.isPressed();
    }
}
