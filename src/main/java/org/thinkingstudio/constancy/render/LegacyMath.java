package org.thinkingstudio.constancy.render;

import net.minecraft.util.math.Direction;

public final class LegacyMath {
    public static float getLerpProgress(float value, float start, float end) { return (value - start) / (end - start); }
    public static Direction rotateClockwise(Direction face, Direction.Axis axis) {
        int x = face.getOffsetX(), y = face.getOffsetY(), z = face.getOffsetZ();
        switch (axis) {
            case X: return Direction.fromVector(x, z, -y);
            case Y: return Direction.fromVector(-z, y, x);
            case Z: return Direction.fromVector(y, -x, z);
            default: throw new AssertionError(axis);
        }
    }
    public static Direction rotateCounterclockwise(Direction face, Direction.Axis axis) {
        return rotateClockwise(rotateClockwise(rotateClockwise(face, axis), axis), axis);
    }
}
