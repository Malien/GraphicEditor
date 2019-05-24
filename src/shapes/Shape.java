package shapes;

import primitives.Vec3;

public interface Shape {

    float[] vertices();
    int mode();
    Vec3<Float> color();

}
