package shapes;

import components.Collider;
import primitives.Vec3;

public interface Shape {

    float[] vertices();
    int mode();
    Vec3<Float> color();
    void setColor(Vec3<Float> color);

    Collider collider();

}
