package me.pepperbell.continuity.client.mixin.compat.embeddium;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import me.jellysquid.mods.sodium.client.world.biome.BiomeSlice;
import me.pepperbell.continuity.client.util.biome.BiomeRetriever;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Read the worker's cloned biome data rather than the mutable client world. */
@Mixin(value = WorldSlice.class, remap = false)
public abstract class EmbeddiumWorldSliceMixin implements BiomeRetriever.Provider {
    @Shadow @Final private BiomeSlice biomeSlice;
    @Override public Biome continuity$getBiome(BlockPos pos) {
        return biomeSlice.getBiome(pos.getX(), pos.getY(), pos.getZ());
    }
}
