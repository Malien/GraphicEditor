package primitives;

import java.util.Objects;

public class Vec3<T> {
    T x;
    T y;
    T z;

    public Vec3(T x, T y, T z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3() {
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

    public T getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "Vec3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public void setZ(T z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec3<?> vec3 = (Vec3<?>) o;
        return Objects.equals(x, vec3.x) &&
                Objects.equals(y, vec3.y) &&
                Objects.equals(z, vec3.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
