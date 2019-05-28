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
    Corner getCorner(Vec2<Float> pos, float radius);
//    void translate(Vec2<Float> vec);

    Collider collider();

    default void translate(Vec2<Float> vec) {
        float[] ver = vertices();
        for (int i=0; i<ver.length; i+=2) {
            ver[i] += vec.x;
            ver[i+1] += vec.y;
        }
    }

}
