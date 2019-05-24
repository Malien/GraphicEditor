package primitives;

import java.util.Objects;

public class Vec2<T> {
    T x;
    T y;

    public Vec2() {
    }

    public Vec2(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return x;
    }

    public void setX(T x) {
        this.x = x;
    }

    public T getY() {
        return y;
    }

    public void setY(T y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2<?> vec2 = (Vec2<?>) o;
        return Objects.equals(x, vec2.x) &&
                Objects.equals(y, vec2.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "primitives.Vec2{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
