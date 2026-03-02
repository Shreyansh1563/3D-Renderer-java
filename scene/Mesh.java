package scene;

import java.util.ArrayList;
import java.util.List;
import Math.Vec3;

public class Mesh {
    public List<Vec3> vertices = new ArrayList<>();
    public List<Triangle> triangles = new ArrayList<>();
    // public List<Edge> edges = new ArrayList<>();


    public static Mesh cubeMesh() {

        Mesh mesh = new Mesh();

        // Vertices
        mesh.vertices.add(new Vec3(-1, -1, -1)); // 0
        mesh.vertices.add(new Vec3( 1, -1, -1)); // 1
        mesh.vertices.add(new Vec3( 1,  1, -1)); // 2
        mesh.vertices.add(new Vec3(-1,  1, -1)); // 3

        mesh.vertices.add(new Vec3(-1, -1,  1)); // 4
        mesh.vertices.add(new Vec3( 1, -1,  1)); // 5
        mesh.vertices.add(new Vec3( 1,  1,  1)); // 6
        mesh.vertices.add(new Vec3(-1,  1,  1)); // 7


        // ---- FRONT (-Z) ----
        mesh.triangles.add(new Triangle(0, 2, 1));
        mesh.triangles.add(new Triangle(0, 3, 2));

        // ---- BACK (+Z) ----
        mesh.triangles.add(new Triangle(4, 5, 6));
        mesh.triangles.add(new Triangle(4, 6, 7));

        // ---- LEFT (-X) ----
        mesh.triangles.add(new Triangle(0, 7, 3));
        mesh.triangles.add(new Triangle(0, 4, 7));

        // ---- RIGHT (+X) ----
        mesh.triangles.add(new Triangle(1, 2, 6));
        mesh.triangles.add(new Triangle(1, 6, 5));

        // ---- TOP (+Y) ----
        mesh.triangles.add(new Triangle(3, 7, 6));
        mesh.triangles.add(new Triangle(3, 6, 2));

        // ---- BOTTOM (-Y) ----
        mesh.triangles.add(new Triangle(0, 1, 5));
        mesh.triangles.add(new Triangle(0, 5, 4));

        return mesh;
    }

    public static Mesh sphereMesh(float radius, int stacks, int slices) {
        Mesh mesh = new Mesh();

        // ----- Vertices -----
        for (int i = 0; i <= stacks; i++) {
            float v = (float) i / stacks;          // 0 → 1
            float phi = (float) (Math.PI * v);     // 0 → PI

            for (int j = 0; j <= slices; j++) {
                float u = (float) j / slices;      // 0 → 1
                float theta = (float) (2.0 * Math.PI * u); // 0 → 2PI

                float x = (float) (Math.sin(phi) * Math.cos(theta));
                float y = (float) Math.cos(phi);
                float z = (float) (Math.sin(phi) * Math.sin(theta));

                mesh.vertices.add(new Vec3(
                        radius * x,
                        radius * y,
                        radius * z
                ));
            }
        }

        // ----- Triangles -----
        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {

                int first = i * (slices + 1) + j;
                int second = first + slices + 1;

                // CCW winding (outside facing)
                mesh.triangles.add(new Triangle(first, second, first + 1));
                mesh.triangles.add(new Triangle(second, second + 1, first + 1));
            }
        }

        return mesh;
    }

    public static Mesh createTorus(
        int majorSegments,
        int minorSegments,
        float majorRadius,
        float minorRadius) {

        Mesh mesh = new Mesh();

        for (int i = 0; i <= majorSegments; i++) {

            float u = (float)(2 * Math.PI * i / majorSegments);
            float cosU = (float)Math.cos(u);
            float sinU = (float)Math.sin(u);

            for (int j = 0; j <= minorSegments; j++) {

                float v = (float)(2 * Math.PI * j / minorSegments);
                float cosV = (float)Math.cos(v);
                float sinV = (float)Math.sin(v);

                float x = (majorRadius + minorRadius * cosV) * cosU;
                float y = (majorRadius + minorRadius * cosV) * sinU;
                float z = minorRadius * sinV;

                mesh.vertices.add(new Vec3(x, z, y)); // swap for Y-up if needed
            }
        }

        int stride = minorSegments + 1;

        for (int i = 0; i < majorSegments; i++) {
            for (int j = 0; j < minorSegments; j++) {

                int current = i * stride + j;
                int next = (i + 1) * stride + j;

                // mesh.triangles.add(new Triangle(
                //         current,
                //         next,
                //         current + 1
                // ));

                // mesh.triangles.add(new Triangle(
                //         current + 1,
                //         next,
                //         next + 1
                // ));
                mesh.triangles.add(new Triangle(current, current + 1, next));
                mesh.triangles.add(new Triangle(current + 1, next + 1, next));
            }
        }

        return mesh;
    }
}
