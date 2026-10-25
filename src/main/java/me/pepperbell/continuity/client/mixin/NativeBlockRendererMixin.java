package me.pepperbell.continuity.client.mixin;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.thinkingstudio.constancy.render.NativeRenderContext;
import org.thinkingstudio.constancy.render.NativeLightingConsumer;

@Mixin(BlockModelRenderer.class)
abstract class NativeBlockRendererMixin {
    @ModifyVariable(method = "renderModel(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;JILnet/minecraftforge/client/model/data/IModelData;)Z", at = @At("HEAD"), argsOnly = true, remap = false)
    private BakedModel continuity$model(BakedModel model, BlockRenderView world, BakedModel original, BlockState state,
            BlockPos pos, MatrixStack matrices, VertexConsumer vertices, boolean cull, Random random, long seed, int overlay, IModelData data) {
        return NativeRenderContext.block(model, world, state, pos, seed, data, cull);
    }
    @ModifyVariable(method = "renderModel(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;JILnet/minecraftforge/client/model/data/IModelData;)Z", at = @At("HEAD"), argsOnly = true, remap = false)
    private VertexConsumer continuity$lighting(VertexConsumer vertices, BlockRenderView world, BakedModel model, BlockState state,
            BlockPos pos, MatrixStack matrices, VertexConsumer original, boolean cull, Random random, long seed, int overlay, IModelData data) {
        return model instanceof org.thinkingstudio.constancy.render.api.model.FabricBakedModel ? new NativeLightingConsumer(vertices, world) : vertices;
    }
}
