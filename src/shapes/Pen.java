package shapes;

import components.Collider;

import java.util.Optional;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;

public class Pen extends Polygon {

    private static final float DISTANCE = 10;

    @Override
    public int mode() {
        return GL_LINE_STRIP;
    }

    @Override
    public Collider collider(float scale) {
        return point -> {
            float[][] sides = new float[ver.size() / 2 - 1][4];
            for (int i = 0; i < sides.length; i += 1) {
                sides[i][0] = ver.get(2 * i);
                sides[i][1] = ver.get(2 * i + 1);
                sides[i][2] = ver.get(2 * i + 2);
                sides[i][3] = ver.get(2 * i + 3);
            }
            Optional<Boolean> res = Stream.of(sides)
                    .parallel()
                    .map(side -> {
                        float maxx, minx, maxy, miny;
                        if (side[0] > side[2]) {
                            maxx = side[0];
                            minx = side[2];
                        } else {
                            maxx = side[2];
                            minx = side[0];
                        }
                        if (side[1] > side[3]) {
                            maxy = side[1];
                            miny = side[3];
                        } else {
                            maxy = side[3];
                            miny = side[1];
                        }
                        if (point.x > maxx || point.x < minx || point.y > maxy || point.y < miny) return false;
                        float distance = Math.abs((side[2] - side[0]) * (side[1] - point.y) - (side[0] - point.x) * (side[3] - side[1]))
                                / (float) Math.sqrt(Math.pow(side[2] - side[0], 2) + Math.pow(side[3] - side[1], 2));
                        return distance <= DISTANCE / scale;
                    })
                    .reduce((prev, curr) -> prev || curr);
            if (res.isPresent()) {
                return res.get();
            }
            return false;
        };
    }
}
