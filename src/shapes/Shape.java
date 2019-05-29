package shapes;

import components.Collider;
import primitives.Vec2;
import primitives.Vec3;

import java.io.Serializable;

public interface Shape extends Serializable {

    float[] vertices();
    int mode();
    Vec3<Float> color();
    void setColor(Vec3<Float> color);

    ShapeHandle getHandle(Vec2<Float> pos, float radius);

    void translate(Vec2<Float> vec);

    Collider collider(float scale);

    default void updateForScale(float scale) {
    }

    default float[] handles() {
        return vertices();
    }

}
