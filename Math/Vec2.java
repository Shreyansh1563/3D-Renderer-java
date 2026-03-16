package Math;

public class Vec2 {
    public float x, y;

    public Vec2(float x, float y){
        this.x = x;
        this.y = y;
    }

    public Vec2 multiply(float no){
        return new Vec2(x*no, y*no);
    }

    public Vec2 add(Vec2 other){
        return new Vec2(x+other.x, y+other.y);
    }

    public Vec2 divide(float no){
        return new Vec2(x / no, y / no);
    }
}
