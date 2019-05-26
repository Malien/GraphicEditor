package primitives;

import java.util.Objects;

public class Vec3<T> {
    public T x;
    public T y;
    public T z;

    public Vec3(T x, T y, T z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3() {
    }

    public Vec3<T> copy() {
        return new Vec3<>(x,y,z);
    }

    @Override
    public String toString() {
        return "Vec3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
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
