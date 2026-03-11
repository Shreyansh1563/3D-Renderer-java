package scene;

import Math.Vec3;
import Math.Vec4;

public class Vertex {

    public Vec3 screenPos;     // x,y,z after projection
    public Vec3 viewPos;       // position in view space
    public Vec3 viewOverW;     // for perspective correction
    public Vec3 normalOverW;   // perspective corrected normal
    public float invW;
}