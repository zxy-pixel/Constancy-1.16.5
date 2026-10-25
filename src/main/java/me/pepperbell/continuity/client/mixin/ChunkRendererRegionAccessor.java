package me.pepperbell.continuity.client.mixin;

import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The 1.16 chunk region exposes colors but does not implement WorldView. */
@Mixin(ChunkRendererRegion.class)
public interface ChunkRendererRegionAccessor {
    @Accessor("world") World continuity$getWorld();
}
