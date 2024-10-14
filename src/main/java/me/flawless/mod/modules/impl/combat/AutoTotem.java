/**
 * AutoTotem Module
 */
package me.flawless.mod.modules.impl.combat;

import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.UpdateWalkingPlayerEvent;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.entity.InventoryUtil;
import me.flawless.api.utils.math.Timer;
import me.flawless.mod.gui.clickgui.ClickGuiScreen;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
	private final BooleanSetting mainHand =
			add(new BooleanSetting("MainHand",false));
	private final BooleanSetting crystal =
			add(new BooleanSetting("Crystal",false, v -> !mainHand.getValue()));
	private final SliderSetting health =
			add(new SliderSetting("Health", 16.0f, 0.0f, 36.0f, 0.1));
	public AutoTotem() {
		super("AutoTotem", Category.Combat);
		this.setDescription("Automatically replaced totems.");
	}
	int totems = 0;
	private final Timer timer = new Timer();

	@Override
	public String getInfo() {
		return String.valueOf(totems);
	}

	@EventHandler
	public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
		update();
	}

	@Override
	public void onUpdate() {
		update();
	}

	private void update() {
		if (nullCheck()) return;
		totems = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
		if (mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof InventoryScreen) && !(mc.currentScreen instanceof ClickGuiScreen)) {
			return;
		}
		if (!timer.passedMs(200)) {
			return;
		}
		if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.getValue()) {
			if (!mainHand.getValue() && crystal.getValue() && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL) {
				int itemSlot = InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL);
				if (itemSlot != -1) {
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
					EntityUtil.syncInventory();
					timer.reset();
				}
			}
			return;
		}
		if (mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING || mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
			return;
		}
		int itemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING);
		if (itemSlot != -1) {
			if (mainHand.getValue()) {
				InventoryUtil.switchToSlot(0);
				if (mc.player.getInventory().getStack(0).getItem() != Items.TOTEM_OF_UNDYING) {
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
					EntityUtil.syncInventory();
				}
			} else {
				mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
				mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
				mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, mc.player);
				EntityUtil.syncInventory();
			}
			timer.reset();
		}
	}
}
