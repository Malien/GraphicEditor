package primitives;

import java.io.Serializable;
import java.util.Objects;

public class Vec4<T> implements Serializable {
    public T x;
    public T y;
    public T z;
    public T w;

    public Vec4(T x, T y, T z, T w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4<T> copy() {
        return new Vec4<>(x,y,z,w);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec4<?> vec4 = (Vec4<?>) o;
        return Objects.equals(x, vec4.x) &&
                Objects.equals(y, vec4.y) &&
                Objects.equals(z, vec4.z) &&
                Objects.equals(w, vec4.w);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, w);
    }

    @Override
    public String toString() {
        return "Vec4{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", w=" + w +
                '}';
    }
}
