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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
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
                        Set<Class<? extends Entity>> entityClasses = new HashSet<>();
                        DefaultedRegistry<EntityType<?>> entityRegistry = BuiltInRegistries.ENTITY_TYPE;
                        for (EntityType<?> entityType : entityRegistry) {
                            try {
                                // Create an instance of the entity (null may be required for the world context)
                                Entity entityInstance = entityType.create(player.level()); // You might need a valid Level if required
                                if (entityInstance != null) {
                                    entityClasses.add(entityInstance.getClass());
                                    player.sendSystemMessage(Component.literal("" + entityInstance));
                                    entityTypeMap.put(entityInstance.getClass(), entityInstance.getType());

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        };

                        entityTypeMap.forEach((entityClass, entityType) -> {
                            player.sendSystemMessage(Component.literal("Mapped: " + entityClass.getName() + " to " + entityType));
                        });
                        List<Integer> blocks1 = blocks.keySet()
                                .stream()
                                .map(BuiltInRegistries.BLOCK::getId)
                                .toList();

                        List<Integer> entities1 = entities.keySet()
                                .stream()
                                .filter(entityClass ->
                                        !(Player.class.isAssignableFrom(entityClass.getClass()) ||
                                                ServerPlayer.class.isAssignableFrom(entityClass.getClass()) ||
                                                LocalPlayer.class.isAssignableFrom(entityClass.getClass())))
                                .map(entityClass -> {
                                    player.sendSystemMessage(Component.literal("" + entityClass.getClass()));
                                    EntityType<?> entityType = entityTypeMap.get(entityClass.getClass());
                                    if (entityType != null) {
                                        Entity entityInstance = entityType.create(player.level()); // You might need a valid Level if required
                                        player.sendSystemMessage(Component.literal("if you get this message it should work"));
                                        return BuiltInRegistries.ENTITY_TYPE.getId(entityInstance.getType());
                                    } else {
                                        ((ServerPlayer) player).sendSystemMessage(Component.literal("No EntityType found for class: " + entityClass.getName()));
                                        return null;
                                    }
                                })
                                .filter(id -> id != null)
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
