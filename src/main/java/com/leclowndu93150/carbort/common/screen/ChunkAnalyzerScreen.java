package com.leclowndu93150.carbort.common.screen;

import com.leclowndu93150.carbort.Carbort;
import com.leclowndu93150.carbort.networking.ChunkAnalyzerTogglePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChunkAnalyzerScreen extends AbstractContainerScreen<ChunkAnalyzerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Carbort.MODID, "textures/gui/chunk_analyzer.png");

    private boolean scanning;
    private boolean notEnoughEnergy;
    private List<Block> blocks;
    private List<Integer> amounts;
    private List<? extends EntityType<?>> entities;
    private List<Integer> entityamounts;

    private int imageWidth;
    private int imageHeight;
    private ChunkAnalyzerDataPanel dataPanel;

    public ChunkAnalyzerScreen(ChunkAnalyzerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 190;
        this.imageHeight = 136;
    }

    public void setNotEnoughEnergy(boolean notEnoughEnergy) {
        this.notEnoughEnergy = notEnoughEnergy;
    }

    public void setScanning(boolean scanning) {
        this.scanning = scanning;
    }

    public void setBlocks(List<Integer> blocks, List<Integer> amounts) {
        this.blocks = blocks.stream().map(BuiltInRegistries.BLOCK::byId).toList();
        this.amounts = amounts;
    }
    public void setEntities(List<Integer> entities, List<Integer> amounts) {
        this.entities = entities.stream().map(BuiltInRegistries.ENTITY_TYPE::byId).toList();
        this.entityamounts = amounts;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 4;
        this.titleLabelY = 20;
        this.inventoryLabelY = -500;
        this.imageWidth = 190;
        this.imageHeight = 136;
        int btnX = (width + imageWidth) / 2 - 32;
        int y = (height - imageHeight) / 2;

        addDataPanel();
        setPanelBlocks();
        setPanelEntities();
        addScanButton(btnX, y);
        addRenderableWidget(this.dataPanel);
    }

    private void setPanelBlocks() {
        this.dataPanel.setBlocksAndAmounts(this.blocks, this.amounts);
    }

    private void setPanelEntities() {
        this.dataPanel.setEntitiesAndAmounts(this.entities, this.entityamounts);
    }

    private void addDataPanel() {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int width = (x + imageWidth - 4) - (x + 4);
        int height = (y + imageHeight - 6) - (y + 16);

        this.dataPanel = new ChunkAnalyzerDataPanel(Minecraft.getInstance(), width, height, y + 16, x + 4);
        addRenderableWidget(this.dataPanel);
    }

    private void addScanButton(int btnX, int y) {
        addRenderableWidget(Button.builder(Component.literal("Scan"), btn -> {
            this.notEnoughEnergy = false;
            PacketDistributor.sendToServer(new ChunkAnalyzerTogglePayload((byte) 0));
            this.scanning = true;
        }).bounds(btnX - 11, y + 3, 32, 12).build());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        setPanelBlocks();
        setPanelEntities();
        int btnX = (width + imageWidth) / 2 - 32;
        int y = (height - imageHeight) / 2;
        addScanButton(btnX, y);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.scanning) {
            renderScanningText(guiGraphics, "Scanning...");
        }

        if (this.notEnoughEnergy) {
            renderScanningText(guiGraphics, "Not enough energy");
        }
    }

    private void renderScanningText(GuiGraphics guiGraphics, String text) {
        Font font = Minecraft.getInstance().font;
        int x = width / 2;
        int y = height / 2;
        guiGraphics.drawCenteredString(font, text, x, y - 4, FastColor.ARGB32.color(255, 255, 255));
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        guiGraphics.fill(x + 4, y + 16, x + imageWidth - 4, y + imageHeight - 6, FastColor.ARGB32.color(53, 53, 53));
    }
}
