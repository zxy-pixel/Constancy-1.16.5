package me.pepperbell.continuity.client.mixin;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.thinkingstudio.constancy.render.NativeRenderContext;
import org.thinkingstudio.constancy.render.NativeLightingConsumer;

@Mixin(ItemRenderer.class)
abstract class NativeItemRendererMixin {
    @ModifyVariable(method = "renderBakedItemModel", at = @At("HEAD"), argsOnly = true)
    private BakedModel continuity$model(BakedModel model, BakedModel original, ItemStack stack, int light, int overlay,
            MatrixStack matrices, VertexConsumer vertices) {
        return NativeRenderContext.item(model, stack);
    }
    @ModifyVariable(method = "renderBakedItemModel", at = @At("HEAD"), argsOnly = true)
    private VertexConsumer continuity$lighting(VertexConsumer vertices) { return new NativeLightingConsumer(vertices, null); }
}
