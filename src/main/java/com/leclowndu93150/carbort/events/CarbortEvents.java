package com.leclowndu93150.carbort.events;

import com.leclowndu93150.carbort.Carbort;
import com.leclowndu93150.carbort.common.energy.ItemStackEnergyStorage;
import com.leclowndu93150.carbort.common.items.HealingAxeItem;
import com.leclowndu93150.carbort.common.energy.IEnergyItem;
import com.leclowndu93150.carbort.common.screen.ChunkAnalyzerScreen;
import com.leclowndu93150.carbort.networking.ChunkAnalyzerDataPayload;
import com.leclowndu93150.carbort.networking.ChunkAnalyzerTogglePayload;
import com.leclowndu93150.carbort.networking.PayloadActions;
import com.leclowndu93150.carbort.registry.MenuRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Carbort.MODID)
public class CarbortEvents {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void healingAxeHitEntity(AttackEntityEvent event) {
        if (event.getEntity().getMainHandItem().getItem() instanceof HealingAxeItem) {
            Player player = event.getEntity();
            if (!player.level().isClientSide) {
                if (event.getTarget() instanceof Monster) {
                    return;
                }
                if (event.getTarget() instanceof LivingEntity livingEntity) {
                    livingEntity.heal(1);
                    player.getFoodData().setSaturation(player.getFoodData().getSaturationLevel() - 2);
                    player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 2);
                }
            }
            event.setCanceled(true);
        }
    }

    @EventBusSubscriber(modid = Carbort.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof IEnergyItem energyItem) {
                    event.registerItem(Capabilities.EnergyStorage.ITEM,
                            (itemStack, ctx) -> new ItemStackEnergyStorage(energyItem.getCapacity(), itemStack), item);
                }
            }
        }

        @SubscribeEvent
        public static void registerPayloads(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar(Carbort.MODID);
            registrar.playBidirectional(ChunkAnalyzerTogglePayload.TYPE, ChunkAnalyzerTogglePayload.STREAM_CODEC, PayloadActions::chunkAnalyzerAction);
            registrar.playToClient(ChunkAnalyzerDataPayload.TYPE, ChunkAnalyzerDataPayload.STREAM_CODEC, PayloadActions::chunkAnalyzerData);
        }

        @SubscribeEvent
        public static void onClientSetup(RegisterMenuScreensEvent event) {
            event.register(MenuRegistry.CHUNK_ANALYZER_MENU.get(), ChunkAnalyzerScreen::new);
        }
    }
}
