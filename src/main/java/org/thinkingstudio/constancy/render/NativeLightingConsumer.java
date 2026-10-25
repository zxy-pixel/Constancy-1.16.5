package org.thinkingstudio.constancy.render;

import java.util.Arrays;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.BlockRenderView;

/** Preserve Forge's baked-light pipeline while honoring per-quad emissive/AO flags. */
public final class NativeLightingConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final BlockRenderView world;
    public NativeLightingConsumer(VertexConsumer delegate, BlockRenderView world) { this.delegate = delegate; this.world = world; }
    @Override public void quad(MatrixStack.Entry matrix, BakedQuad quad, float[] brightness, float red, float green,
            float blue, int[] lights, int overlay, boolean useColor) {
        if (quad instanceof NativeQuad) {
            NativeQuad nativeQuad = (NativeQuad) quad;
            if (nativeQuad.material.emissive(0)) {
                brightness = new float[]{1, 1, 1, 1};
                lights = new int[]{0xF000F0, 0xF000F0, 0xF000F0, 0xF000F0};
            } else if (world != null && nativeQuad.material.disableAo(0)) {
                float shade = world.getBrightness(quad.getFace(), quad.hasShade());
                brightness = new float[]{shade, shade, shade, shade};
            }
        }
        delegate.addVertexData(matrix, quad, brightness, red, green, blue, 1.0f, lights, overlay, useColor);
    }
    @Override public VertexConsumer vertex(double x, double y, double z) { delegate.vertex(x, y, z); return this; }
    @Override public VertexConsumer color(int r, int g, int b, int a) { delegate.color(r, g, b, a); return this; }
    @Override public VertexConsumer texture(float u, float v) { delegate.texture(u, v); return this; }
    @Override public VertexConsumer overlay(int u, int v) { delegate.overlay(u, v); return this; }
    @Override public VertexConsumer light(int u, int v) { delegate.light(u, v); return this; }
    @Override public VertexConsumer normal(float x, float y, float z) { delegate.normal(x, y, z); return this; }
    @Override public void next() { delegate.next(); }
}
