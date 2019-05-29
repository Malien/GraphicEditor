package util;

import primitives.Vec2;

public final class Transfrom {

    public static Vec2<Float> viewportToWorldSpace(Vec2<Double> viewport, Vec2<Float> translation, float scale) {
        return new Vec2<>((viewport.x.floatValue()-translation.x)/scale, (viewport.y.floatValue()-translation.y)/scale);
    }

    public static double length(Vec2<Double> start, Vec2<Double> end) {
        return Math.sqrt(Math.pow(end.x - start.x, 2) + Math.pow(end.y - start.y, 2));
    }

}
