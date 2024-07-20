package com.leclowndu93150.carbort.utils;

import com.leclowndu93150.carbort.Carbort;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.extensions.IEntityExtension;

import java.util.List;

public class ChunkAnalyzerHelper {
    public Player player;
    public Level level;

    public ChunkAnalyzerHelper(Player player, Level level) {
        this.player = player;
        this.level = level;
    }

    public Object2IntMap<Block> scan() {
        Object2IntMap<Block> blocks = new Object2IntOpenHashMap<>();
        ChunkAccess chunkAccess = level.getChunk(player.getOnPos());
        int initZ = chunkAccess.getPos().getMinBlockZ();
        int initX = chunkAccess.getPos().getMinBlockX();

        Carbort.LOGGER.debug("x, z: {}, {}", initX, initZ);

        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
            for (int z = initZ; z < initZ + 16; z++) {
                for (int x = initX; x < initX + 16; x++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(blockPos);
                    if (state.isAir() || !state.getFluidState().isEmpty()) continue;
                    Block block = state.getBlock();
                    if (blocks.containsKey(block)) {
                        int prevAmount = blocks.getInt(block);
                        blocks.put(block, prevAmount + 1);
                    } else {
                        blocks.put(block, 1);
                    }
                }
            }
        }
        return blocks;
    }
    public Object2IntMap<LivingEntity> scanEntities() {
        Object2IntMap<LivingEntity> entities = new Object2IntOpenHashMap<>();
        ChunkAccess chunkAccess = level.getChunk(player.getOnPos());


        int chunkMinX = chunkAccess.getPos().getMinBlockX();
        int chunkMinZ = chunkAccess.getPos().getMinBlockZ();
        int chunkMaxX = chunkAccess.getPos().getMaxBlockX();
        int chunkMaxZ = chunkAccess.getPos().getMaxBlockZ();
        AABB chunkBounds = new AABB(chunkMinX, level.getMinBuildHeight(), chunkMinZ, chunkMaxX + 1, level.getMaxBuildHeight(), chunkMaxZ + 1);

        // Iterate over all entities within the chunk bounds
        for (Entity entity : level.getEntities(null, chunkBounds)) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                entities.put(livingEntity, entities.getInt(livingEntity) + 1);
            }

        }

        return entities;
    }
}
