package Math;

public class Vec3 {
    public float x, y, z;

    public Vec3(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float length(){
        return (float)Math.sqrt(x*x + y*y + z*z);
    }

    public Vec3 normalize(){
        float length = this.length();
        if(length == 0) return new Vec3(0, 0, 0);

        return new Vec3(x/length, y/length, z/length);
    }

    public float dot(Vec3 other){
        return x * other.x + y * other.y + z * other.z;
    }

    public Vec3 subtract(Vec3 other){
        return new Vec3(x-other.x, y-other.y, z-other.z);
    }

    public Vec3 cross(Vec3 other){
        return new Vec3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
}
