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

        List<Vec3> projectedVertices = new ArrayList<>();
        List<Vec3> viewSpaceNoramls = new ArrayList<>();
        List<Float> invWs = new ArrayList<>();
        List<Vec3> viewOverW = new ArrayList<>();
        List<Vec3> normalOverW = new ArrayList<>();
        List<Vec3> viewVertices = new ArrayList<>();

        transformAndProjectVertices(
            object,
            model,
            view,
            projection,
            viewVertices,
            viewOverW,
            projectedVertices,
            viewSpaceNoramls,
            normalOverW,
            invWs
        );

        for (Triangle triangle : object.mesh.triangles) {
            processTriangle(triangle, viewOverW, viewVertices, projectedVertices, normalOverW, invWs, object);
        }
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


    private void transformAndProjectVertices(
            GameObject object,
            Mat4 model,
            Mat4 view,
            Mat4 projection,
            List<Vec3> viewVertices,
            List<Vec3> viewOverW,
            List<Vec3> projected,
            List<Vec3> viewSpaceNormals,
            List<Vec3> normalOverW,
            List<Float> invWs
    ) {
        Mat4 modelView = view.multiply(model);
        // Mat4 mvp = projection.multiply(modelView);

        for (int i = 0; i < object.mesh.vertices.size(); i++) {
            Vec3 v = object.mesh.vertices.get(i);
            Vec3 n = object.mesh.normals.get(i);

            Vec4 vertex = new Vec4(v.x, v.y, v.z, 1);

            // Vec4 world = model.multiply(vertex);
            Vec4 viewV = modelView.multiply(vertex);
            Vec4 clip = projection.multiply(viewV);

            // viewSpace.add(new Vec3(viewV.x, viewV.y, viewV.z));
            // viewOverW.add(new Vec3(viewV.x, viewV.y, viewV.z))
            viewVertices.add(new Vec3(viewV.x, viewV.y, viewV.z));

            Vec4 normal4 = new Vec4(n.x, n.y, n.z, 0);
            Vec4 viewN4  = modelView.multiply(normal4);

            viewSpaceNormals.add(
                new Vec3(viewN4.x, viewN4.y, viewN4.z).normalize()
            );

            float invW = 1.0f / clip.w;
            // normalOverW.add(new Vec3(viewN4.x, viewN4.y, viewN4.z).normalize().multiply(invW));
            invWs.add(invW);

            if (clip.w <= 0) {
                projected.add(null);
                continue;
            }
            
            

            viewOverW.add(new Vec3(viewV.x, viewV.y, viewV.z).multiply(invW));

            normalOverW.add(
                new Vec3(viewN4.x, viewN4.y, viewN4.z)
                .normalize()
                .multiply(invW)
            );

            clip.x /= clip.w;
            clip.y /= clip.w;
            clip.z /= clip.w;

            int screenX = (int) ((clip.x + 1f) * 0.5f * width);
            int screenY = (int) ((1f - clip.y) * 0.5f * height);

            projected.add(new Vec3(screenX, screenY, clip.z));
        }
    }


    private void processTriangle(
            Triangle triangle,
            List<Vec3> viewOverW,
            List<Vec3> viewVertices,
            List<Vec3> projected,
            List<Vec3> normalOverW,
            List<Float> invWs,
            GameObject object
    ) {

        Vec3 p0 = projected.get(triangle.v0);
        Vec3 p1 = projected.get(triangle.v1);
        Vec3 p2 = projected.get(triangle.v2);

        if (p0 == null || p1 == null || p2 == null)
            return;

        Vec3 v0OverW= viewOverW.get(triangle.v0);
        Vec3 v1OverW = viewOverW.get(triangle.v1);
        Vec3 v2OverW = viewOverW.get(triangle.v2);
        float invW0 = invWs.get(triangle.v0);
        float invW1 = invWs.get(triangle.v1);
        float invW2 = invWs.get(triangle.v2);

        Vec3 faceNormal = computeNormal(viewVertices.get(triangle.v0), viewVertices.get(triangle.v1), viewVertices.get(triangle.v2));
        Vec3 n0, n1, n2;
        if (shadingMode == ShadingMode.FLAT) {
            // Same normal for whole triangle
            n0 = faceNormal;
            n1 = faceNormal;
            n2 = faceNormal;

        } else {
            // Smooth shading
            n0 = normalOverW.get(triangle.v0);
            n1 = normalOverW.get(triangle.v1);
            n2 = normalOverW.get(triangle.v2);
        }

        rasterizeTriangle(
            p0, p1, p2,
            v0OverW, v1OverW, v2OverW,
            n0, n1, n2,
            invW0, invW1, invW2
        );
    }


    private Vec3 computeNormal(Vec3 v0, Vec3 v1, Vec3 v2) {
        Vec3 edge1 = v1.subtract(v0);
        Vec3 edge2 = v2.subtract(v0);
        return edge1.cross(edge2).normalize();
    }


    private void rasterizeTriangle(
        Vec3 p0, Vec3 p1, Vec3 p2,
        Vec3 v0OverW, Vec3 v1OverW, Vec3 v2OverW,
        Vec3 n0OverW, Vec3 n1OverW, Vec3 n2OverW,
        float invW0, float invW1, float invW2
    ){
        drawTriangle(
            p0.x, p0.y, p0.z, invW0,
            p1.x, p1.y, p1.z, invW1,
            p2.x, p2.y, p2.z, invW2,
            v0OverW, v1OverW, v2OverW,
            n0OverW, n1OverW, n2OverW
        );
    }

    private void drawTriangle(
        float x0, float y0, float z0, float invW0,
        float x1, float y1, float z1, float invW1,
        float x2, float y2, float z2, float invW2,
        Vec3 v0View, Vec3 v1View, Vec3 v2View,
        Vec3 n0, Vec3 n1, Vec3 n2
    ) {

        float minx = Math.max(0, Math.min(x0, Math.min(x1, x2)));
        float maxx = Math.min(width - 1, Math.max(x0, Math.max(x1, x2)));
        float miny = Math.max(0, Math.min(y0, Math.min(y1, y2)));
        float maxy = Math.min(height - 1, Math.max(y0, Math.max(y1, y2)));

        float area = edgeFunction(x0, y0, x1, y1, x2, y2);
        if (area == 0) return;

        if (backfaceCullingEnabled && area < 0)
            return;

        for (int y = (int) miny; y <= maxy; y++) {
            for (int x = (int) minx; x <= maxx; x++) {

                float px = x + 0.5f;
                float py = y + 0.5f;

                float w0 = edgeFunction(x1, y1, x2, y2, px, py);
                float w1 = edgeFunction(x2, y2, x0, y0, px, py);
                float w2 = edgeFunction(x0, y0, x1, y1, px, py);

                if ((w0 >= 0 && w1 >= 0 && w2 >= 0 && area > 0) ||
                    (w0 <= 0 && w1 <= 0 && w2 <= 0 && area < 0)) {

                    float alpha = w0 / area;
                    float beta  = w1 / area;
                    float gamma = w2 / area;

                    // float depth = alpha * z0 / w0 + beta * z1/w1 + gamma * z2/w2;
                    float depth = alpha * z0 + beta * z1 + gamma * z2;

                    int index = y * width + x;

                    if (depth < depthBuffer[index]) {

                        depthBuffer[index] = depth;

                        // ===== INTERPOLATION =====

                        // interpolate 1/w
                        float invW =
                                alpha * invW0 +
                                beta  * invW1 +
                                gamma * invW2;

                        // interpolate attributes / w
                        Vec3 viewOverW =
                                v0View.multiply(alpha)
                                .add(v1View.multiply(beta))
                                .add(v2View.multiply(gamma));

                        Vec3 normalOverW =
                                n0.multiply(alpha)
                                .add(n1.multiply(beta))
                                .add(n2.multiply(gamma));

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