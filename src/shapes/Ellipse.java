package shapes;

import components.Collider;
import primitives.Vec2;
import primitives.Vec3;

import static org.lwjgl.opengl.GL11.GL_POLYGON;

public class Ellipse implements Shape {

    private static final int VERTEX_PER_LENGTH = 50;
    private static final int MIN_VERTEX_COUNT = 20;
    public float x;
    public float y;
    public float width;
    public float height;
    private float[] ver;
    private Vec3<Float> color;
    private float scale;

    public Ellipse(float x, float y, float width, float height, float scale) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
        updateForScale(scale);
    }

    public Ellipse(float x, float y, float width, float height, float scale, Vec3<Float> color) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
        updateForScale(scale);
    }

    @Override
    public float[] vertices() {
        return ver;
    }

    @Override
    public int mode() {
        return GL_POLYGON;
    }

    @Override
    public Vec3<Float> color() {
        return color;
    }

    @Override
    public void setColor(Vec3<Float> color) {
        this.color = color;
    }

    @Override
    public ShapeHandle getHandle(Vec2<Float> pos, float radius) {
        if (pos.x > x - radius && pos.x < x + radius && pos.y > y - radius && pos.y < y + radius) {
            return vec -> {
                x += vec.x;
                width -= vec.x;
                y += vec.y;
                height -= vec.y;
                updateForScale(scale);
            };
        }
        if (pos.x > x + width - radius && pos.x < x + width + radius && pos.y > y - radius && pos.y < y + radius) {
            return vec -> {
                width += vec.x;
                y += vec.y;
                height -= vec.y;
                updateForScale(scale);
            };
        }
        if (pos.x > x + width - radius && pos.x < x + width + radius && pos.y > y + height - radius && pos.y < y + height + radius) {
            return vec -> {
                width += vec.x;
                height += vec.y;
                updateForScale(scale);
            };
        }
        if (pos.x > x - radius && pos.x < x + radius && pos.y > y + height - radius && pos.y < y + height + radius) {
            return vec -> {
                x += vec.x;
                width -= vec.x;
                height += vec.y;
                updateForScale(scale);
            };
        }
        return null;
    }

    @Override
    public void translate(Vec2<Float> vec) {
        x += vec.x;
        y += vec.y;
        updateForScale(scale);
    }

    @Override
    public Collider collider() {
        return point -> Math.pow(point.x - x - width / 2, 2) / Math.pow(width / 2, 2) + Math.pow(point.y - y - height / 2, 2) / Math.pow(height / 2, 2) < 1;
    }

    @Override
    public void updateForScale(float scale) {
        this.scale = scale;
        float length = (width + height) * scale * (float) Math.PI;
        int resolution = (int) (length / VERTEX_PER_LENGTH);
        if (resolution < MIN_VERTEX_COUNT) resolution = MIN_VERTEX_COUNT;
        double step = Math.PI / resolution;
        ver = new float[resolution * 2];
        for (int i = 0; i < ver.length; i += 2) {
            ver[i] = (float) (width / 2 + x + width * Math.sin(step * i) / 2);
            ver[i + 1] = (float) (height / 2 + y + height * Math.cos(step * i) / 2);
        }
    }

    @Override
    public boolean handlesSameAsVertices() {
        return false;
    }

    @Override
    public float[] handles() {
        return new float[]{x, y, x + width, y, x + width, y + height, x, y + height};
    }
}
