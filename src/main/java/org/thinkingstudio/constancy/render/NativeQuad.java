package org.thinkingstudio.constancy.render;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import org.thinkingstudio.constancy.render.api.material.RenderMaterial;

public final class NativeQuad extends BakedQuad {
    public final RenderMaterial material;
    public NativeQuad(int[] data, int tint, Direction face, Sprite sprite, boolean shade, RenderMaterial material) {
        super(data, tint, face, sprite, shade);
        this.material = material;
    }
}
