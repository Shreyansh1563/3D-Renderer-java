package graphics;

import java.util.List;

import Math.Vec2;
import Math.Vec3;
import scene.GameObject;
import scene.Triangle;
import scene.Vertex;

public class Rasterizer {

    private boolean backfaceCullingEnabled = true;
    private Shader shader = new Shader();
    private ShadingMode shadingMode = ShadingMode.SMOOTH;

    public void processTriangle(
            Triangle triangle,
            List<Vertex> vertices,
            GameObject object,
            int[] colorBuffer,
            float[] depthBuffer,
            int width,
            int height
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

        drawTriangle(colorBuffer, depthBuffer, v0, v1, v2, n0, n1, n2, width, height);
    }

    private Vec3 computeNormal(Vec3 v0, Vec3 v1, Vec3 v2) {
        Vec3 edge1 = v1.subtract(v0);
        Vec3 edge2 = v2.subtract(v0);
        return edge1.cross(edge2).normalize();
    }

    private float edgeFunction(float x0, float y0, float x1, float y1, float x, float y) {
        return (x - x0) * (y1 - y0) - (y - y0) * (x1 - x0);
    }


    private void drawTriangle(
        int[] colorBuffer, float[] depthBuffer, Vertex v0, Vertex v1, Vertex v2, Vec3 normal0, Vec3 normal1, Vec3 normal2, int width, int height
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

                        Vec2 uv = v0.uvOverW.multiply(alpha)
                                .add(v1.uvOverW.multiply(beta))
                                .add(v2.uvOverW.multiply(gamma))
                                .divide(invW);

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
                        frag.uv = uv;

                        int color = shader.shadeFragment(frag);

                        colorBuffer[index] = color;
                    }
                }
            }
        }
    }

    public void setShadingMode(ShadingMode mode){
        this.shadingMode = mode;
    }

    public void setBackfaceCulling(boolean enabled) {
        this.backfaceCullingEnabled = enabled;
    }
}
