package com.leclowndu93150.carbort.networking;

import com.leclowndu93150.carbort.Carbort;
import com.leclowndu93150.carbort.CarbortConfig;
import com.leclowndu93150.carbort.common.items.ChunkAnalyzerItem;
import com.leclowndu93150.carbort.common.screen.ChunkAnalyzerMenu;
import com.leclowndu93150.carbort.common.screen.ChunkAnalyzerScreen;
import com.leclowndu93150.carbort.registry.DataComponentRegistry;
import com.leclowndu93150.carbort.utils.CapabilityUtils;
import com.leclowndu93150.carbort.utils.ChunkAnalyzerHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PayloadActions {
    public static void chunkAnalyzerAction(ChunkAnalyzerTogglePayload payload, IPayloadContext ctx) {
        if (payload.payloadType() == 0) {
            Player player = ctx.player();
            player.level().getServer().doRunTask(new TickTask(0, () -> {
                if (player instanceof ServerPlayer serverPlayer && player.containerMenu instanceof ChunkAnalyzerMenu menu) {
                    ItemStack itemStack = getAnalyzer(player);
                    IEnergyStorage energyStorage = CapabilityUtils.itemEnergyStorage(itemStack);
                    int drained = energyStorage.extractEnergy(CarbortConfig.chunkAnalyzerEnergyUsage, true);
                    if (drained == CarbortConfig.chunkAnalyzerEnergyUsage) {
                        ChunkAnalyzerHelper helper = new ChunkAnalyzerHelper(player, player.level());
                        Object2IntMap<Block> blocks = helper.scan();
                        Object2IntMap<LivingEntity> entities = helper.scanEntities();
                        energyStorage.extractEnergy(CarbortConfig.chunkAnalyzerEnergyUsage, false);
                        PacketDistributor.sendToPlayer(serverPlayer, new ChunkAnalyzerTogglePayload((byte) 1));
                        Map<Class<? extends Entity>, EntityType<?>> entityTypeMap = new HashMap<>();
                        for (Field field : EntityType.class.getDeclaredFields()) {
                            if (EntityType.class.isAssignableFrom(field.getType())) {
                                try {
                                    EntityType<?> entityType = (EntityType<?>) field.get(null);
                                    entityTypeMap.put(entityType.getBaseClass(), entityType);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        List<Integer> blocks1 = blocks.keySet()
                                .stream()
                                .map(BuiltInRegistries.BLOCK::getId)
                                .toList();

                        List<Integer> entities1 = entities.keySet()
                                .stream()
                                .map(entityClass -> {
                                    EntityType<?> entityType = entityTypeMap.get(entityClass);
                                    if (entityType != null) {
                                        return BuiltInRegistries.ENTITY_TYPE.getId(entityType);
                                    } else {
                                        throw new IllegalArgumentException("No EntityType found for class: " + entityClass.getName());
                                    }
                                })
                                .collect(Collectors.toList());
                        ChunkAnalyzerDataPayload payload1 = new ChunkAnalyzerDataPayload(blocks1, blocks.values().stream().toList(), entities1, entities.values().stream().toList());
                        PacketDistributor.sendToPlayer(serverPlayer, payload1);
                    } else  {
                        PacketDistributor.sendToPlayer(serverPlayer, new ChunkAnalyzerTogglePayload((byte) 2));
                    }
                }
            }));
        } else if (payload.payloadType() == 1) {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ChunkAnalyzerScreen screen1) {
                screen1.setScanning(false);
                screen1.setNotEnoughEnergy(false);
            }
        } else if (payload.payloadType() == 2) {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ChunkAnalyzerScreen screen1) {
                screen1.setScanning(false);
                screen1.setNotEnoughEnergy(true);
            }
        }
    }
    private static @Nullable ItemStack getAnalyzer(Player player) {
        if (player.getMainHandItem().getItem() instanceof ChunkAnalyzerItem) {
            return player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof ChunkAnalyzerItem) {
            return player.getOffhandItem();
        }
        return null;
    }

    public static void chunkAnalyzerData(ChunkAnalyzerDataPayload payload, IPayloadContext ctx) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof ChunkAnalyzerScreen screen1) {
            screen1.setBlocks(payload.blocks(), payload.amounts());
            screen1.setEntities(payload.entities(), payload.entityamount());
        }
    }
}
