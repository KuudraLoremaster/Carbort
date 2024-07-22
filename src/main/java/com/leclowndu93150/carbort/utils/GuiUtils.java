package com.leclowndu93150.carbort.utils;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public final class GuiUtils {
    private GuiUtils() {
        throw new IllegalStateException("Attempted to construct utility class!");
    }

    private static <E extends Entity> void render(EntityRenderDispatcher renderManager, E entity, double xPos, double yPos, double zPos, float rotation, float delta, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        final EntityRenderer<? super E> entityRenderer = renderManager.getRenderer(entity);

        try {
            final Vec3 renderOffset = entityRenderer.getRenderOffset(entity, delta);
            final double finalX = xPos + renderOffset.x();
            final double finalY = yPos + renderOffset.y();
            final double finalZ = zPos + renderOffset.z();
            poseStack.pushPose();
            poseStack.translate(finalX, finalY, finalZ);
            entityRenderer.render(entity, rotation, delta, poseStack, buffer, packedLight);



            poseStack.translate(-renderOffset.x(), -renderOffset.y(), -renderOffset.z());
            poseStack.popPose();
        } catch (final Exception exception) {
            final CrashReport crashReport = CrashReport.forThrowable(exception, "Rendering entity in world");
            final CrashReportCategory entityCategory = crashReport.addCategory("Entity being rendered");
            entity.fillCrashReportCategory(entityCategory);
            final CrashReportCategory detailsCategory = crashReport.addCategory("Renderer details");
            detailsCategory.setDetail("Assigned renderer", entityRenderer);
            detailsCategory.setDetail("Location",
                    CrashReportCategory.formatLocation(Minecraft.getInstance().level, xPos, yPos, zPos));
            detailsCategory.setDetail("Rotation", rotation);
            detailsCategory.setDetail("Delta", delta);
            throw new ReportedException(crashReport);
        }
    }
    public static void renderEntity(PoseStack stack, Entity entity, Vec3 rotation, Vec3 scale, Vec3 offset, int xPos, int yPos, float partialTicks) {
        stack.pushPose();
        stack.translate(xPos, yPos, 1050.0F);
        stack.scale(1.0F, 1.0F, -1.0F);
        stack.translate(0.0D, 0.0D, 1000.0D);
        stack.scale((float) scale.x(), (float) scale.y(), (float) scale.z());
        final Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
        stack.mulPose(quaternion);
        stack.translate(offset.x(), offset.y(), offset.z());
        stack.mulPose(new Quaternionf((float) -rotation.x(), (float) -rotation.y(), (float) -rotation.z(), 1));

        Lighting.setupForEntityInInventory();

        final EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        renderManager.setRenderShadow(false);
        final MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> render(renderManager, entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, stack, buffer, 15728880));
        renderManager.setRenderShadow(true);
        buffer.endBatch();

        stack.popPose();

        Lighting.setupFor3DItems();
    }


}