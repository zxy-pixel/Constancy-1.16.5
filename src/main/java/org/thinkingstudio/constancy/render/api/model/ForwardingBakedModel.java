/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Modified 2026-10-22: relocated and adapted for the Constancy Forge backport.
package org.thinkingstudio.constancy.render.api.model;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import org.thinkingstudio.constancy.render.api.render.RenderContext;

/**
 * Base class for specialized model implementations that need to wrap other baked models.
 * Avoids boilerplate code for pass-through methods.
 */
public abstract class ForwardingBakedModel implements BakedModel, FabricBakedModel {
	/** implementations must set this somehow. */
	protected BakedModel wrapped;

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		if (wrapped instanceof FabricBakedModel) ((FabricBakedModel) wrapped).emitBlockQuads(blockView, state, pos, randomSupplier, context);
        else context.fallbackConsumer().accept(wrapped);
	}

	@Override
	public boolean isVanillaAdapter() {
		return !(wrapped instanceof FabricBakedModel) || ((FabricBakedModel) wrapped).isVanillaAdapter();
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		if (wrapped instanceof FabricBakedModel) ((FabricBakedModel) wrapped).emitItemQuads(stack, randomSupplier, context);
        else context.fallbackConsumer().accept(wrapped);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState blockState, Direction face, Random rand) {
		return wrapped.getQuads(blockState, face, rand);
	}

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction face, Random random, net.minecraftforge.client.model.data.IModelData data) {
        return wrapped.getQuads(state, face, random, data);
    }

    @Override
    public net.minecraftforge.client.model.data.IModelData getModelData(BlockRenderView world, BlockPos pos, BlockState state,
            net.minecraftforge.client.model.data.IModelData data) {
        return wrapped.getModelData(world, pos, state, data);
    }

	@Override
	public boolean useAmbientOcclusion() {
		return wrapped.useAmbientOcclusion();
	}

    @Override public boolean isAmbientOcclusion(BlockState state) { return wrapped.isAmbientOcclusion(state); }
    @Override public Sprite getParticleTexture(net.minecraftforge.client.model.data.IModelData data) { return wrapped.getParticleTexture(data); }
    @Override public boolean doesHandlePerspectives() { return wrapped.doesHandlePerspectives(); }
    @Override public BakedModel handlePerspective(ModelTransformation.Mode mode, net.minecraft.client.util.math.MatrixStack matrices) {
        BakedModel result = wrapped.handlePerspective(mode, matrices);
        if (result == wrapped) return this;
        // Forge may select a different model for the camera transform.
        return new me.pepperbell.continuity.client.model.EmissiveBakedModel(result);
    }

	@Override
	public boolean hasDepth() {
		return wrapped.hasDepth();
	}

	@Override
	public boolean isBuiltin() {
		return wrapped.isBuiltin();
	}

	@Override
	public Sprite getSprite() {
		return wrapped.getSprite();
	}

	@Override
	public boolean isSideLit() {
		return wrapped.isSideLit();
	}

	@Override
	public ModelTransformation getTransformation() {
		return wrapped.getTransformation();
	}

	@Override
	public ModelOverrideList getOverrides() {
		return wrapped.getOverrides();
	}
}
