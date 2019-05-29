package shapes;

import components.Collider;
import primitives.Vec2;
import primitives.Vec3;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.GL_POLYGON;

public class Polygon implements Shape {

    protected ArrayList<Float> ver = new ArrayList<>();
    private Vec3<Float> color = new Vec3<>(0f, 0f, 0f);

    private float[] boundingBox = new float[4];

    public void add(float x, float y) {
        ver.add(x);
        ver.add(y);
        updateBB();
    }

    private void updateBB() {
        boundingBox[0] = ver.get(0);
        boundingBox[1] = ver.get(1);
        boundingBox[2] = ver.get(0);
        boundingBox[3] = ver.get(1);
        for (int i = 0; i < ver.size(); i += 2) {
            if (ver.get(i) < boundingBox[0]) boundingBox[0] = ver.get(i);
            if (ver.get(i) > boundingBox[1]) boundingBox[1] = ver.get(i);
            if (ver.get(i + 1) < boundingBox[2]) boundingBox[2] = ver.get(i + 1);
            if (ver.get(i + 1) > boundingBox[3]) boundingBox[3] = ver.get(i + 1);
        }
    }

    @Override
    public float[] vertices() {
        float[] out = new float[ver.size()];
        for (int i = 0; i < ver.size(); i++) out[i] = ver.get(i);
        return out;
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
        for (int i = 0; i < ver.size(); i += 2) {
            final int index = i;
            if (
                    pos.x > ver.get(i) - radius &&
                            pos.x < ver.get(i) + radius &&
                            pos.y > ver.get(i + 1) - radius &&
                            pos.y < ver.get(i + 1) + radius
            ) return vec -> {
                ver.set(index, ver.get(index) + vec.x);
                ver.set(index + 1, ver.get(index + 1) + vec.y);
                updateBB();
            };
        }
        return null;
    }

    @Override
    public Collider collider(float scale) {
        return point -> {
            float[][] sides = new float[ver.size() / 2][4];
            if (
                    point.x < boundingBox[0] ||
                            point.x > boundingBox[1] ||
                            point.y < boundingBox[2] ||
                            point.y > boundingBox[3]
            ) {
                return false;
            }
            for (int i = 0; i < sides.length; i++) {
                sides[i][0] = ver.get(2 * i);
                sides[i][1] = ver.get(2 * i + 1);
                sides[i][2] = ver.get((2 * i + 2) % ver.size());
                sides[i][3] = ver.get((2 * i + 3) % ver.size());
            }
            //offset ray endpoint outside of bounding box to ensure raycaster works properly
            float endx = boundingBox[2] + 1;
            float endy = boundingBox[3] + 1;
            float k = (endy - point.y) / (endx - point.y);
            float b = endy - k * endx;
            long count = Stream.of(sides)
                    .parallel()
                    .filter(side -> {
                        float k1 = (side[3] - side[1]) / (side[2] - side[0]);
                        float b1 = side[1] - k1 * side[0];
                        float x = (b1 - b) / (k - k1);
                        float maxx, minx;
                        if (side[0] > side[2]) {
                            maxx = side[0];
                            minx = side[2];
                        } else {
                            maxx = side[2];
                            minx = side[0];
                        }
//                        float y = k * x + b;
                        return x < maxx && x > minx && x > point.x;
                    })
                    .count();
            System.out.println(count);
            return count % 2 == 1;
        };
    }

    @Override
    public void translate(Vec2<Float> vec) {
        for (int i = 0; i < ver.size(); i += 2) {
            ver.set(i, ver.get(i) + vec.x);
            ver.set(i + 1, ver.get(i + 1) + vec.y);
        }
        updateBB();
    }
}
