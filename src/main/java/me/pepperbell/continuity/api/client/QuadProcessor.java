// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.api.client;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.thinkingstudio.constancy.render.api.mesh.Mesh;
import org.thinkingstudio.constancy.render.api.mesh.MutableQuadView;
import org.thinkingstudio.constancy.render.api.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.math.BlockPos;
import java.util.Random;
import net.minecraft.world.BlockRenderView;

public interface QuadProcessor {
    /** Conservative extra render passes, indexed by the internal BlendMode ordinal. */
    default int getExtraLayerMask() { return 0x1e; }
	ProcessingResult processQuad(MutableQuadView quad, Sprite sprite, BlockRenderView blockView, BlockState appearanceState, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, int pass, ProcessingContext context);

	interface ProcessingContext extends ProcessingDataProvider {
		void addEmitterConsumer(Consumer<QuadEmitter> consumer);

		void addMesh(Mesh mesh);

		QuadEmitter getExtraQuadEmitter();

		void markHasExtraQuads();
	}

	enum ProcessingResult {
		NEXT_PROCESSOR,
		NEXT_PASS,
		STOP,
		DISCARD;
	}

	interface Factory<T extends CtmProperties> {
		QuadProcessor createProcessor(T properties, Function<SpriteIdentifier, Sprite> textureGetter);
	}
}
