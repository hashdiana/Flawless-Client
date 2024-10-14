package me.flawless.mod.modules.impl.combat;

import me.flawless.FlawLess;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.Render3DEvent;
import me.flawless.api.events.impl.UpdateWalkingPlayerEvent;
import me.flawless.api.managers.RotateManager;
import me.flawless.api.utils.combat.CombatUtil;
import me.flawless.api.utils.entity.EntityUtil;
import me.flawless.api.utils.entity.InventoryUtil;
import me.flawless.api.utils.math.FadeUtils;
import me.flawless.api.utils.math.Timer;
import me.flawless.api.utils.render.ColorUtil;
import me.flawless.api.utils.render.Render3DUtil;
import me.flawless.api.utils.world.BlockUtil;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.ColorSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.HashMap;

public class HoleFiller extends Module {
    public static HoleFiller INSTANCE;
    private final Timer timer = new Timer();
    public final SliderSetting placeDelay =
            add(new SliderSetting("PlaceDelay", 50, 0, 500).setSuffix("ms"));
    private final SliderSetting blocksPer =
            add(new SliderSetting("BlocksPer", 1, 1, 8));
    private final SliderSetting placeRange =
            add(new SliderSetting("PlaceRange", 5, 0, 8.).setSuffix("m"));
    private final SliderSetting enemyRange =
            add(new SliderSetting("EnemyRange", 6, 0, 8.).setSuffix("m"));
    private final SliderSetting holeRange =
            add(new SliderSetting("HoleRange", 2, 0, 8.).setSuffix("m"));
    private final SliderSetting selfRange =
            add(new SliderSetting("SelfRange", 2, 0, 8.).setSuffix("m"));
    private final SliderSetting predictTicks =
            add(new SliderSetting("Predict", 1, 1, 8).setSuffix("tick"));
    private final BooleanSetting detectMining =
            add(new BooleanSetting("DetectMining", false));
    private final BooleanSetting rotate =
            add(new BooleanSetting("Rotate", true));
    private final BooleanSetting packetPlace =
            add(new BooleanSetting("PacketPlace", true));
    private final BooleanSetting breakCrystal =
            add(new BooleanSetting("Break", true).setParent());
    private final BooleanSetting eatPause =
            add(new BooleanSetting("EatingPause", true, v -> breakCrystal.isOpen()));
    private final BooleanSetting usingPause =
            add(new BooleanSetting("UsingPause", true));
    private final BooleanSetting inventory =
            add(new BooleanSetting("InventorySwap", true));
    public final BooleanSetting inAirPause =
            add(new BooleanSetting("InAirPause", true));
    private final BooleanSetting web =
            add(new BooleanSetting("Web", true));
    public final BooleanSetting render =
            add(new BooleanSetting("Render", true).setParent());
    final ColorSetting box =
            add(new ColorSetting("Box", new Color(255, 255, 255, 255), v -> render.isOpen()).injectBoolean(true));
    final SliderSetting lineWidth =
            add(new SliderSetting("LineWidth", 1.5d, 0.01d, 3d, 0.01, v -> render.isOpen()));
    final ColorSetting fill =
            add(new ColorSetting("Fill", new Color(255, 255, 255, 100), v -> render.isOpen()).injectBoolean(true));
    public final SliderSetting fadeTime = add(new SliderSetting("FadeTime", 500, 0, 5000, v -> render.isOpen()));
    public final BooleanSetting pre = add(new BooleanSetting("Pre", false, v -> render.isOpen()));

    int progress = 0;
    public HoleFiller() {
        super("HoleFiller", Category.Combat);
        INSTANCE = this;
        FlawLess.EVENT_BUS.subscribe(new HoleFillRender());
    }


    @EventHandler
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        if (event.isPost() || !timer.passedMs((long) placeDelay.getValue())) return;
        progress = 0;

        if (getBlock() == -1) {
            return;
        }
        if (usingPause.getValue() && mc.player.isUsingItem()) {
            return;
        }
        if (inAirPause.getValue() && !mc.player.isOnGround()) return;
        CombatUtil.getEnemies(enemyRange.getValue()).stream()
                .flatMap(enemy -> BlockUtil.getSphere(holeRange.getValueFloat(), CombatUtil.getEntityPosVec(enemy, predictTicks.getValueInt())).stream())
                .filter(pos -> pos.toCenterPos().distanceTo(mc.player.getPos()) > selfRange.getValue() && (BlockUtil.isHole(pos, true, true, false) || CombatUtil.isDoubleHole(pos)))
                .distinct()
                .forEach(this::tryPlaceBlock);
    }

    private void tryPlaceBlock(BlockPos pos) {
        if (pos == null) return;
        if (detectMining.getValue() && BlockUtil.isMining(pos)) return;
        if (pre.getValue()) {
            if (BlockUtil.clientCanPlace(pos, true)) HoleFillRender.addBlock(pos);
        }
        if (!(progress < blocksPer.getValue())) return;
        int block = getBlock();
        if (block == -1) return;

        if (!BlockUtil.canPlace(pos, placeRange.getValue(), true)) return;
        if (breakCrystal.getValue()) {
            CombatUtil.attackCrystal(pos, rotate.getValue(), eatPause.getValue());
        } else if (BlockUtil.hasEntity(pos, false)) return;
        if (rotate.getValue()) {
            RotateManager.lastEvent.cancelRotate();
        }
        int old = mc.player.getInventory().selectedSlot;
        doSwap(block);
        BlockUtil.placeBlock(pos, rotate.getValue(), packetPlace.getValue());
        if (inventory.getValue()) {
            doSwap(block);
            EntityUtil.syncInventory();
        } else {
            doSwap(old);
        }
        progress++;
        timer.reset();
        HoleFillRender.addBlock(pos);
    }

    private void doSwap(int slot) {
        if (inventory.getValue()) {
            InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
        } else {
            InventoryUtil.switchToSlot(slot);
        }
    }

    private int getBlock() {
        if (inventory.getValue()) {
            if (web.getValue() && InventoryUtil.findBlockInventorySlot(Blocks.COBWEB) != -1) {
                return InventoryUtil.findBlockInventorySlot(Blocks.COBWEB);
            }
            return InventoryUtil.findBlockInventorySlot(Blocks.OBSIDIAN);
        } else {
            if (web.getValue() && InventoryUtil.findBlock(Blocks.COBWEB) != -1) {
                return InventoryUtil.findBlock(Blocks.COBWEB);
            }
            return InventoryUtil.findBlock(Blocks.OBSIDIAN);
        }
    }

    public class HoleFillRender {
        public static final HashMap<BlockPos, placePosition> renderMap = new HashMap<>();
        public static void addBlock(BlockPos pos) {
            renderMap.put(pos, new placePosition(pos));
        }

        @EventHandler
        public void onRender3D(Render3DEvent event) {
            if (!INSTANCE.render.getValue()) return;
            if (renderMap.isEmpty()) return;
            boolean shouldClear = true;
            for (HoleFillRender.placePosition placePosition : renderMap.values()) {
                if (!BlockUtil.clientCanPlace(placePosition.pos, true)) {
                    placePosition.isAir = false;
                }
                if (!placePosition.timer.passedMs((long) (placeDelay.getValue() + 100)) && placePosition.isAir) {
                    placePosition.firstFade.reset();
                }
                if (placePosition.firstFade.getQuad(FadeUtils.Quad.In2) == 1) continue;
                shouldClear = false;
                MatrixStack matrixStack = event.getMatrixStack();
                if (INSTANCE.fill.booleanValue) {
                    Render3DUtil.drawFill(matrixStack, new Box(placePosition.pos), ColorUtil.injectAlpha(INSTANCE.fill.getValue(), (int) ((double) fill.getValue().getAlpha() * (1 - placePosition.firstFade.getQuad(FadeUtils.Quad.In2)))));
                }
                if (INSTANCE.box.booleanValue) {
                    Render3DUtil.drawBox(matrixStack, new Box(placePosition.pos), ColorUtil.injectAlpha(INSTANCE.box.getValue(), (int) ((double) box.getValue().getAlpha() * (1 - placePosition.firstFade.getQuad(FadeUtils.Quad.In2)))));
                }
            }
            if (shouldClear) renderMap.clear();
        }


        public static class placePosition {
            public final FadeUtils firstFade;
            public final BlockPos pos;
            public final Timer timer;
            public boolean isAir;
            public placePosition(BlockPos placePos) {
                this.firstFade = new FadeUtils((long) HoleFiller.INSTANCE.fadeTime.getValue());
                this.pos = placePos;
                this.timer = new Timer();
                this.isAir = true;
            }
        }
    }
}
