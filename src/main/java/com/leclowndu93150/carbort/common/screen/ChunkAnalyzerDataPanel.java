package com.leclowndu93150.carbort.common.screen;

import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChunkAnalyzerDataPanel extends ScrollPanel {
    private List<Block> blocks;
    private List<Integer> amounts;
    private List<Integer> entityamounts;
    private List<? extends EntityType<?>> entities;

    public ChunkAnalyzerDataPanel(Minecraft client, int width, int height, int top, int left) {
        super(client, width, height, top, left);
    }

    public void setBlocksAndAmounts(List<Block> blocks, List<Integer> amounts) {
        this.blocks = blocks;
        this.amounts = amounts;
    }
    public void setEntitiesAndAmounts(List<? extends EntityType<?>> entities, List<Integer> entityamounts) {
        this.entities = entities;
        this.entityamounts = entityamounts;
    }

    @Override
    protected int getContentHeight() {
        return this.blocks != null ? blocks.size() * 18 : 0;
    }

    @Override
    protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
        if (blocks != null && entities != null) {
            int maxWidth = 0;
            Font font = Minecraft.getInstance().font;
            for (int amount : this.amounts) {
                MutableComponent literal = Component.literal(amount + "x");
                int width = font.width(literal.getString());
                if (width > maxWidth) maxWidth = width;
            }
            for (int entityamount : this.entityamounts) {
                MutableComponent literal = Component.literal(entityamount + "x");
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
                int amount = this.entityamounts.get(i);
                MutableComponent literal = Component.literal(amount + "x");

                guiGraphics.drawString(font, literal,
                        left + 6, relativeY + i * 18 + 4, FastColor.ARGB32.color(255, 255, 255));
                guiGraphics.drawString(font, entity.getDescription(),
                        left + 4 + maxWidth + 24, relativeY + i * 18 + 4, FastColor.ARGB32.color(255, 255, 255));
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
