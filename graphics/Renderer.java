package graphics;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Math.Mat4;
import Math.Vec3;
import Math.Vec4;
import scene.GameObject;
import scene.Scene;
import scene.Triangle;
import scene.Vertex;

public class Renderer {

    private final int width;
    private final int height;

    private final int[] colorBuffer;
    private final float[] depthBuffer;
    private final BufferedImage image;

    private boolean backfaceCullingEnabled = true;
    private Shader shader = new Shader();
    private ShadingMode shadingMode = ShadingMode.SMOOTH;


    public Renderer(int width, int height) {
        this.width = width;
        this.height = height;

        this.colorBuffer = new int[width * height];
        this.depthBuffer = new float[width * height];
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }


    public BufferedImage getImage() {
        return image;
    }

    public void updateImage() {
        image.setRGB(0, 0, width, height, colorBuffer, 0, width);
    }


    public void setBackfaceCulling(boolean enabled) {
        this.backfaceCullingEnabled = enabled;
    }


    public int[] getColorBuffer(){
        return colorBuffer;
    }

    public void clear() {
        Arrays.fill(colorBuffer, 0x000000);
        Arrays.fill(depthBuffer, Float.POSITIVE_INFINITY);
    }

    public void renderScene(Scene scene){
        for(GameObject obj: scene.getObjects()){
            render(obj, scene.getCamera());
        }
    }

    public void render(GameObject object, Camera camera) {

        Mat4 model = object.transform.getModelMatrix();
        Mat4 view = camera.getViewMatrix();
        Mat4 projection = camera.getProjectionMatrix();

        List<Vertex> vertices = new ArrayList<>();

        transformAndProjectVertices(object, model, view, projection, vertices);

        for (Triangle triangle : object.mesh.triangles) {
            processTriangle(triangle, vertices, object);
        }
    }


    private void transformAndProjectVertices(
            GameObject object,
            Mat4 model,
            Mat4 view,
            Mat4 projection,
            List<Vertex> vertices
    ) {
        Mat4 modelView = view.multiply(model);

        for (int i = 0; i < object.mesh.vertices.size(); i++) {
            Vertex vert = new Vertex();

            Vec3 v = object.mesh.vertices.get(i);
            Vec3 n = object.mesh.normals.get(i);

            Vec4 vertex = new Vec4(v.x, v.y, v.z, 1);

            Vec4 viewV = modelView.multiply(vertex);
            Vec4 clip = projection.multiply(viewV);
            
            vert.viewPos = new Vec3(viewV.x, viewV.y, viewV.z); 

            Vec4 normal4 = new Vec4(n.x, n.y, n.z, 0);
            Vec4 viewN4  = modelView.multiply(normal4);

            vert.invW = 1.0f / clip.w;

            if (clip.w <= 0) {
                vertices.add(null);
                continue;
            }
            
            vert.viewOverW =new Vec3(viewV.x, viewV.y, viewV.z).multiply(vert.invW);

            vert.normalOverW = new Vec3(viewN4.x, viewN4.y, viewN4.z).normalize().multiply(vert.invW);

            clip.x /= clip.w;
            clip.y /= clip.w;
            clip.z /= clip.w;

            int screenX = (int) ((clip.x + 1f) * 0.5f * width);
            int screenY = (int) ((1f - clip.y) * 0.5f * height);

            vert.screenPos = new Vec3(screenX, screenY, clip.z);

            vertices.add(vert);
        }
    }


    private void processTriangle(
            Triangle triangle,
            List<Vertex> vertices,
            GameObject object
    ) {

        Vertex v0 = vertices.get(triangle.v0);
        Vertex v1 = vertices.get(triangle.v1);
        Vertex v2 = vertices.get(triangle.v2);

        if(v0 == null || v1 == null || v2 == null){
            return;
        }


        Vec3 faceNormal = computeNormal(v0.viewPos, v1.viewPos, v2.viewPos);
        Vec3 n0, n1, n2;
        if (shadingMode == ShadingMode.FLAT) {
            // Same normal for whole triangle
            n0 = faceNormal;
            n1 = faceNormal;
            n2 = faceNormal;

        } else {
            // Smooth shading
            n0 = v0.normalOverW;
            n1 = v1.normalOverW;
            n2 = v2.normalOverW;
        }

        drawTriangle(v0, v1, v2, n0, n1, n2);
    }


    private Vec3 computeNormal(Vec3 v0, Vec3 v1, Vec3 v2) {
        Vec3 edge1 = v1.subtract(v0);
        Vec3 edge2 = v2.subtract(v0);
        return edge1.cross(edge2).normalize();
    }


    private void drawTriangle(
        Vertex v0, Vertex v1, Vertex v2, Vec3 normal0, Vec3 normal1, Vec3 normal2
    ) {


        float minx = Math.max(0, Math.min(v0.screenPos.x, Math.min(v1.screenPos.x, v2.screenPos.x)));
        float maxx = Math.min(width - 1, Math.max(v0.screenPos.x, Math.max(v1.screenPos.x, v2.screenPos.x)));
        float miny = Math.max(0, Math.min(v0.screenPos.y, Math.min(v1.screenPos.y, v2.screenPos.y)));
        float maxy = Math.min(height - 1, Math.max(v0.screenPos.y, Math.max(v1.screenPos.y, v2.screenPos.y)));

        float area = edgeFunction(v0.screenPos.x, v0.screenPos.y, v1.screenPos.x, v1.screenPos.y, v2.screenPos.x, v2.screenPos.y);
        if (area == 0) return;

        if (backfaceCullingEnabled && area < 0)
            return;

        for (int y = (int) miny; y <= maxy; y++) {
            for (int x = (int) minx; x <= maxx; x++) {

                float px = x + 0.5f;
                float py = y + 0.5f;

                float w0 = edgeFunction(v1.screenPos.x, v1.screenPos.y, v2.screenPos.x, v2.screenPos.y, px, py);
                float w1 = edgeFunction(v2.screenPos.x, v2.screenPos.y, v0.screenPos.x, v0.screenPos.y, px, py);
                float w2 = edgeFunction(v0.screenPos.x, v0.screenPos.y, v1.screenPos.x, v1.screenPos.y, px, py);

                if (w0 * area >= 0 && w1 * area >= 0 && w2 * area >= 0) {

                    float alpha = w0 / area;
                    float beta  = w1 / area;
                    float gamma = w2 / area;

                    float depth = alpha * v0.screenPos.z + beta * v1.screenPos.z + gamma * v2.screenPos.z;

                    int index = y * width + x;

                    if (depth < depthBuffer[index]) {

                        depthBuffer[index] = depth;

                        // ===== INTERPOLATION =====

                        // interpolate 1/w
                        float invW =
                                alpha * v0.invW +
                                beta  * v1.invW +
                                gamma * v2.invW;

                        // interpolate attributes / w
                        Vec3 viewOverW =
                                v0.viewOverW.multiply(alpha)
                                .add(v1.viewOverW.multiply(beta))
                                .add(v2.viewOverW.multiply(gamma));

                        Vec3 normalOverW =
                                normal0.multiply(alpha)
                                .add(normal1.multiply(beta))
                                .add(normal2.multiply(gamma));

                        // reconstruct correct values

                        float w = 1.0f / invW;

                        Vec3 viewPos = viewOverW.multiply(w);

                        Vec3 normal =
                                normalOverW
                                .multiply(w)
                                .normalize();

                        FragmentData frag = new FragmentData();
                        frag.viewPosition = viewPos;
                        frag.normal = normal;

                        int color = shader.shadeFragment(frag);

                        colorBuffer[index] = color;
                    }
                }
            }
        }
    }

    private float edgeFunction(float x0, float y0, float x1, float y1, float x, float y) {
        return (x - x0) * (y1 - y0) - (y - y0) * (x1 - x0);
    }

    public void setShadingMode(ShadingMode mode){
        this.shadingMode = mode;
    }
}