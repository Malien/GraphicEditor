package components;

import primitives.Vec2;

public interface Collider {

    boolean contains(Vec2<Float> point);

}
