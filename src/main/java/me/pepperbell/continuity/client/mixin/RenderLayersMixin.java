// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.mixin;
import me.pepperbell.continuity.client.config.ContinuityConfig;
import me.pepperbell.continuity.client.resource.CustomBlockLayers;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.thinkingstudio.constancy.client.NativeResources;
@Mixin(RenderLayers.class)
abstract class RenderLayersMixin {
    @Inject(method = {"getBlockLayer", "getMovingBlockLayer"}, at = @At("HEAD"), cancellable = true)
    private static void continuity$layer(BlockState state, CallbackInfoReturnable<RenderLayer> cir) {
        if (ContinuityConfig.INSTANCE.customBlockLayers.get()) {
            RenderLayer layer = CustomBlockLayers.getLayer(state);
            if (layer != null) cir.setReturnValue(layer);
        }
    }
    @Inject(method = "canRenderInLayer(Lnet/minecraft/block/BlockState;Lnet/minecraft/client/render/RenderLayer;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void continuity$passes(BlockState state, RenderLayer layer, CallbackInfoReturnable<Boolean> cir) {
        if (NativeResources.needsExtraLayer(state, layer)) { cir.setReturnValue(true); return; }
        if (ContinuityConfig.INSTANCE.customBlockLayers.get()) {
            RenderLayer custom = CustomBlockLayers.getLayer(state);
            if (custom != null) cir.setReturnValue(layer == custom);
        }
    }
}
