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

    private Vec3 lightDirection = new Vec3(1, 1, 0).normalize();
    private boolean backfaceCullingEnabled = true;


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

        List<Vec3> viewSpaceVertices = new ArrayList<>();
        List<Vec3> projectedVertices = new ArrayList<>();

        transformAndProjectVertices(
                object,
                model,
                view,
                projection,
                viewSpaceVertices,
                projectedVertices
        );

        for (Triangle triangle : object.mesh.triangles) {
            processTriangle(triangle, viewSpaceVertices, projectedVertices);
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
            List<Vec3> viewSpace,
            List<Vec3> projected
) {

        for (Vec3 v : object.mesh.vertices) {

            Vec4 vertex = new Vec4(v.x, v.y, v.z, 1);

            Vec4 world = model.multiply(vertex);
            Vec4 viewV = view.multiply(world);
            Vec4 clip = projection.multiply(viewV);

            viewSpace.add(new Vec3(viewV.x, viewV.y, viewV.z));

            if (clip.w <= 0) {
            // // if (viewV.z > -camera.near) {
            // if (viewV.z >= 0) {
                projected.add(null);
                continue;
            }
            

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
            List<Vec3> viewSpace,
            List<Vec3> projected
    ) {

        Vec3 p0 = projected.get(triangle.v0);
        Vec3 p1 = projected.get(triangle.v1);
        Vec3 p2 = projected.get(triangle.v2);

        if (p0 == null || p1 == null || p2 == null)
            return;

        Vec3 v0 = viewSpace.get(triangle.v0);
        Vec3 v1 = viewSpace.get(triangle.v1);
        Vec3 v2 = viewSpace.get(triangle.v2);

        Vec3 normal = computeNormal(v0, v1, v2);

        if (isBackface(normal, v0))
            return;

        int shadedColor = computeLighting(normal);

        rasterizeTriangle(p0, p1, p2, shadedColor);
    }


    private Vec3 computeNormal(Vec3 v0, Vec3 v1, Vec3 v2) {
        Vec3 edge1 = v1.subtract(v0);
        Vec3 edge2 = v2.subtract(v0);
        return edge1.cross(edge2).normalize();
    }


    private boolean isBackface(Vec3 normal, Vec3 viewVertex) {
        if (!backfaceCullingEnabled) return false;
        return normal.dot(viewVertex) >= 0;
    }


    private int computeLighting(Vec3 normal) {

        float lambert = Math.max(0, normal.dot(lightDirection));
        float ambient = 0.2f;
        float brightness = ambient + (1 - ambient) * lambert;

        int baseR = 200;
        int baseG = 200;
        int baseB = 200;

        int r = (int) (baseR * brightness);
        int g = (int) (baseG * brightness);
        int b = (int) (baseB * brightness);

        return (r << 16) | (g << 8) | b;
    }

    
    private void rasterizeTriangle(Vec3 p0, Vec3 p1, Vec3 p2, int color) {
        drawTriangle(
                p0.x, p0.y, p0.z,
                p1.x, p1.y, p1.z,
                p2.x, p2.y, p2.z,
                color
        );
    }


    private void drawTriangle(
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            int color
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

                    float depth = alpha * z0 + beta * z1 + gamma * z2;

                    int index = y * width + x;

                    if (depth < depthBuffer[index]) {
                        depthBuffer[index] = depth;
                        colorBuffer[index] = color;
                    }
                }
            }
        }
    }


    private float edgeFunction(float x0, float y0, float x1, float y1, float x, float y) {
        return (x - x0) * (y1 - y0) - (y - y0) * (x1 - x0);
    }
}