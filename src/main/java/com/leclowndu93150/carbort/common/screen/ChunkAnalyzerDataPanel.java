package com.leclowndu93150.carbort.common.screen;

import com.leclowndu93150.carbort.Carbort;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChunkAnalyzerDataPanel extends ScrollPanel {
    private List<Block> blocks;
    private List<Integer> amounts;
    private List<Integer> entityAmounts;
    private List<? extends EntityType<?>> entities;

    public ChunkAnalyzerDataPanel(Minecraft client, int width, int height, int top, int left) {
        super(client, width, height, top, left);
    }

    public void setBlocksAndAmounts(List<Block> blocks, List<Integer> amounts) {
        this.blocks = blocks;
        this.amounts = amounts;
    }
    public void setEntitiesAndAmounts(List<? extends EntityType<?>> entities, List<Integer> entityAmounts) {
        this.entities = entities;
        this.entityAmounts = entityAmounts;
    }

    @Override
    protected int getContentHeight() {
        int height = 0;
        if (this.blocks != null) {
            height += blocks.size() * 18;
        }

        if (this.entities != null) {
            height += entities.size() * 18;
        }

        Carbort.LOGGER.debug("Height: {}", height);

        return height;
    }

    @Override
    protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
        Carbort.LOGGER.debug("Entities: {}", entities);
        if (blocks != null && entities != null) {
            int maxWidth = 0;
            Font font = Minecraft.getInstance().font;
            for (int amount : this.amounts) {
                MutableComponent literal = Component.literal(amount + "x");
                int width = font.width(literal.getString());
                if (width > maxWidth) maxWidth = width;
            }

            for (int entityAmount : this.entityAmounts) {
                MutableComponent literal = Component.literal(entityAmount + "x");
                int width = font.width(literal.getString());
                if (width > maxWidth) maxWidth = width;
            }

            for (int i = 0; i < this.blocks.size(); i++) {
                Block block = this.blocks.get(i);
                int amount = this.amounts.get(i);
                MutableComponent literal = Component.literal(amount + "x");

                guiGraphics.renderItem(new ItemStack(block), left + maxWidth + 6, relativeY + i * 18);
                guiGraphics.drawString(font, literal,
                        left + 6, relativeY + i * 18 + 4, FastColor.ARGB32.color(255, 255, 255));
                guiGraphics.drawString(font, block.getName(),
                        left + 4 + maxWidth + 24, relativeY + i * 18 + 4, FastColor.ARGB32.color(255, 255, 255));
            }

            for (int i = 0; i < this.entities.size(); i++) {
                EntityType<?> entity = this.entities.get(i);
                SpawnEggItem entityEgg = SpawnEggItem.byId(entity);
                int amount = this.entityAmounts.get(i);
                MutableComponent literal = Component.literal(amount + "x");
                assert entityEgg != null;
                guiGraphics.renderItem(new ItemStack(entityEgg), left + maxWidth + 6, relativeY + i * 18);
                guiGraphics.drawString(font, literal,
                        left + 6, relativeY + (i + this.blocks.size()) * 18 + 4, FastColor.ARGB32.color(255, 255, 255));
                guiGraphics.drawString(font, entity.getDescription(),
                        left + 4 + maxWidth + 24, relativeY + (i + this.blocks.size()) * 18 + 4, FastColor.ARGB32.color(255, 255, 255));
            }
        }

    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
}
