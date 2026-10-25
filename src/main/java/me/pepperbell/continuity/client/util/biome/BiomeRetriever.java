// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
// Modified 2026-10-23: support renderer-owned biome snapshots.
package me.pepperbell.continuity.client.util.biome;
import me.pepperbell.continuity.client.mixin.ChunkRendererRegionAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;
public final class BiomeRetriever {
    public static void init() { }
    @Nullable public static Biome getBiome(BlockRenderView world, BlockPos pos) {
        if (world instanceof Provider) return ((Provider) world).continuity$getBiome(pos);
        if (world instanceof WorldView) return ((WorldView) world).getBiome(pos);
        if (world instanceof ChunkRendererRegionAccessor) {
            return ((ChunkRendererRegionAccessor) world).continuity$getWorld().getBiome(pos);
        }
        return null;
    }
    public interface Provider {
        Biome continuity$getBiome(BlockPos pos);
    }
}
