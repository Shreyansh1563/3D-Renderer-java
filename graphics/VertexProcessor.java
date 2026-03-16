package graphics;


import Math.Mat4;
import Math.Vec2;
import Math.Vec3;
import Math.Vec4;
import scene.Vertex;

public class VertexProcessor {
    public Vertex process(
            Mat4 model,
            Mat4 view,
            Mat4 projection,
            Vec3 vertex,
            Vec3 normal,
            Vec2 uv,
            int width, 
            int height
    ) {
        Mat4 modelView = view.multiply(model);

        // Vec3 v = object.mesh.vertices.get(i);
        // Vec3 n = object.mesh.normals.get(i);

        Vec4 vertex4 = new Vec4(vertex.x, vertex.y, vertex.z, 1);

        Vec4 viewV = modelView.multiply(vertex4);
        Vec4 clip = projection.multiply(viewV);

        if (clip.w <= 0) {
            return null;
        }

        Vertex vert = new Vertex();
        
        vert.viewPos = new Vec3(viewV.x, viewV.y, viewV.z); 

        Vec4 normal4 = new Vec4(normal.x, normal.y, normal.z, 0);
        Vec4 viewN4  = modelView.multiply(normal4);

        vert.invW = 1.0f / clip.w;
        vert.viewOverW =new Vec3(viewV.x, viewV.y, viewV.z).multiply(vert.invW);
        vert.normalOverW = new Vec3(viewN4.x, viewN4.y, viewN4.z).normalize().multiply(vert.invW);
        vert.uv = uv;
        vert.uvOverW = vert.uv.multiply(vert.invW);

        clip.x /= clip.w;
        clip.y /= clip.w;
        clip.z /= clip.w;

        int screenX = (int) ((clip.x + 1f) * 0.5f * width);
        int screenY = (int) ((1f - clip.y) * 0.5f * height);

        vert.screenPos = new Vec3(screenX, screenY, clip.z);

        return vert;

        // for (int i = 0; i < object.mesh.vertices.size(); i++) {
            
        // }
    }
}
