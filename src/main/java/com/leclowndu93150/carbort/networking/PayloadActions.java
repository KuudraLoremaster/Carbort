package com.leclowndu93150.carbort.networking;

import com.leclowndu93150.carbort.Carbort;
import com.leclowndu93150.carbort.CarbortConfig;
import com.leclowndu93150.carbort.common.items.ChunkAnalyzerItem;
import com.leclowndu93150.carbort.common.screen.ChunkAnalyzerMenu;
import com.leclowndu93150.carbort.common.screen.ChunkAnalyzerScreen;
import com.leclowndu93150.carbort.utils.CapabilityUtils;
import com.leclowndu93150.carbort.utils.ChunkAnalyzerHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
                        Object2IntMap<Block> blocks = helper.scanBlocks();
                        Object2IntMap<LivingEntity> entities = helper.scanEntities(player);
                        Carbort.LOGGER.debug("Amounts: {}", entities.values());
                        energyStorage.extractEnergy(CarbortConfig.chunkAnalyzerEnergyUsage, false);
                        PacketDistributor.sendToPlayer(serverPlayer, new ChunkAnalyzerTogglePayload((byte) 1));

                        List<Integer> blocks1 = blocks.keySet()
                                .stream()
                                .map(BuiltInRegistries.BLOCK::getId)
                                .toList();

                        List<Integer> entities1 = entities.keySet()
                                .stream()
                                .filter(entity -> !(entity instanceof Player))
                                .map(entity -> BuiltInRegistries.ENTITY_TYPE.getId(entity.getType()))
                                .toList();

                        ChunkAnalyzerDataPayload payload1 = new ChunkAnalyzerDataPayload(blocks1, blocks.values().stream().toList(), entities1, entities.values().stream().toList());
                        PacketDistributor.sendToPlayer(serverPlayer, payload1);
                    } else {
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
            screen1.setEntities(payload.entities(), payload.entityAmounts());
        }
    }
}
