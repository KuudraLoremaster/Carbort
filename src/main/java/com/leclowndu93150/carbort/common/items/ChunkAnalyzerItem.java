package com.leclowndu93150.carbort.common.items;

import com.leclowndu93150.carbort.CarbortConfig;
import com.leclowndu93150.carbort.common.energy.IEnergyItem;
import com.leclowndu93150.carbort.common.screen.ChunkAnalyzerMenu;
import com.leclowndu93150.carbort.common.screen.ChunkAnalyzerScreen;
import com.leclowndu93150.carbort.networking.PayloadActions;
import com.leclowndu93150.carbort.registry.DataComponentRegistry;
import com.leclowndu93150.carbort.utils.CapabilityUtils;
import com.leclowndu93150.carbort.utils.ChunkAnalyzerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChunkAnalyzerItem extends Item implements IEnergyItem, MenuProvider {
    public ChunkAnalyzerItem(Properties properties) {
        super(properties);
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        player.openMenu(this);
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    public int getCapacity() {
        return CarbortConfig.chunkAnalyzerMaxEnergy;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return CarbortConfig.chunkAnalyzerMaxEnergy > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        IEnergyStorage energyStorage = CapabilityUtils.itemEnergyStorage(stack);
        return Math.round(13.0F - ((1 - ((float) energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored())) * 13.0F));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x8CBFE4;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Chunk Analyzer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChunkAnalyzerMenu(player, containerId);
    }
}
