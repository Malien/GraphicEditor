package shapes;

import org.lwjgl.opengl.GL11;
import primitives.Vec3;

public class Rect implements Shape {

    private float[] ver = new float[8];
    private Vec3<Float> color = new Vec3<>();

    public Rect(float x, float y, float width, float height) {
        ver[0] = x;
        ver[1] = y;
        ver[2] = x + width;
        ver[3] = y;
        ver[4] = x + width;
        ver[5] = y + height;
        ver[6] = x;
        ver[7] = y + height;
    }

    public Rect(float x, float y, float width, float height, Vec3<Float> color) {
        this(x, y, width, height);
        this.color = color;
    }

    @Override
    public float[] vertices() {
        return ver;
    }

    @Override
    public int mode() {
        return GL11.GL_POLYGON;
    }

    @Override
    public Vec3<Float> color() {
        return color;
    }
}
