package Math;

public class Vec4 {
    public float x, y, z, w;

    public Vec4(float x, float y, float z, float w){
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4 subtract(Vec4 other){
        return new Vec4(x-other.x, y-other.y, z-other.z, w-other.w);
    }

    public Vec4 multiply(float no){
        return new Vec4(no * x, no * y, no * z, no * w);
    }

    public Vec4 add(Vec4 other){
        return new Vec4(x + other.x, y + other.y, z + other.z, w + other.w);
    }
}
