package me.pepperbell.continuity.client.mixin.compat.embeddium;

import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuffers;
import me.jellysquid.mods.sodium.client.render.pipeline.BlockRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.thinkingstudio.constancy.render.NativeRenderContext;

/** Keep Embeddium's chunk buffers, sprite tracking and draw path. */
@Mixin(value = BlockRenderer.class, remap = false)
public abstract class EmbeddiumBlockRendererMixin {
    @ModifyVariable(method = "renderModel", at = @At("HEAD"), argsOnly = true)
    private BakedModel continuity$prepare(BakedModel model, BlockRenderView world, BlockState state, BlockPos pos,
            BakedModel original, ChunkModelBuffers buffers, boolean cull, long seed, IModelData data) {
        return NativeRenderContext.block(model, world, state, pos, seed, data, cull);
    }
}
