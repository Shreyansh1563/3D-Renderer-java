package scene;

import java.util.ArrayList;
import java.util.List;
import Math.Vec3;

public class Mesh {
    public List<Vec3> vertices = new ArrayList<>();
    public List<Triangle> triangles = new ArrayList<>();
    public List<Vec3> normals = new ArrayList<>();
    // public List<Edge> edges = new ArrayList<>();

    public static Mesh cubeMesh(float size) {

        Mesh mesh = new Mesh();
        float s = size / 2f;

        Vec3[] faceNormals = {
            new Vec3( 0, 0, 1),  // front
            new Vec3( 0, 0,-1),  // back
            new Vec3(-1, 0, 0),  // left
            new Vec3( 1, 0, 0),  // right
            new Vec3( 0, 1, 0),  // top
            new Vec3( 0,-1, 0)   // bottom
        };

        Vec3[][] faceVertices = {

            { new Vec3(-s,-s, s), new Vec3( s,-s, s), new Vec3( s, s, s), new Vec3(-s, s, s) }, // front
            { new Vec3( s,-s,-s), new Vec3(-s,-s,-s), new Vec3(-s, s,-s), new Vec3( s, s,-s) }, // back
            { new Vec3(-s,-s,-s), new Vec3(-s,-s, s), new Vec3(-s, s, s), new Vec3(-s, s,-s) }, // left
            { new Vec3( s,-s, s), new Vec3( s,-s,-s), new Vec3( s, s,-s), new Vec3( s, s, s) }, // right
            { new Vec3(-s, s, s), new Vec3( s, s, s), new Vec3( s, s,-s), new Vec3(-s, s,-s) }, // top
            { new Vec3(-s,-s,-s), new Vec3( s,-s,-s), new Vec3( s,-s, s), new Vec3(-s,-s, s) }  // bottom
        };

        for (int i = 0; i < 6; i++) {

            int startIndex = mesh.vertices.size();

            for (int v = 0; v < 4; v++) {
                mesh.vertices.add(faceVertices[i][v]);
                mesh.normals.add(faceNormals[i]);
            }

            mesh.triangles.add(new Triangle(startIndex, startIndex+1, startIndex+2));
            mesh.triangles.add(new Triangle(startIndex, startIndex+2, startIndex+3));
        }

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

    public static Mesh toruMesh(float R, float r, int segU, int segV) {

        Mesh mesh = new Mesh();

        for (int i = 0; i <= segU; i++) {

            float u = (float)(2 * Math.PI * i / segU);

            for (int j = 0; j <= segV; j++) {

                float v = (float)(2 * Math.PI * j / segV);

                float x = (R + r * (float)Math.cos(v)) * (float)Math.cos(u);
                float y = r * (float)Math.sin(v);
                float z = (R + r * (float)Math.cos(v)) * (float)Math.sin(u);

                Vec3 position = new Vec3(x, y, z);

                float nx = (float)Math.cos(u) * (float)Math.cos(v);
                float ny = (float)Math.sin(v);
                float nz = (float)Math.sin(u) * (float)Math.cos(v);

                Vec3 normal = new Vec3(nx, ny, nz).normalize();

                mesh.vertices.add(position);
                mesh.normals.add(normal);
            }
        }

        for (int i = 0; i < segU; i++) {
            for (int j = 0; j < segV; j++) {

                int a = i * (segV + 1) + j;
                int b = a + segV + 1;
                int c = a + 1;
                int d = b + 1;

                // First triangle
                mesh.triangles.add(new Triangle(a, c, b));

                // Second triangle
                mesh.triangles.add(new Triangle(c, d, b));
            }
        }

        return mesh;
    }

    public static Mesh planesMesh(){
        Mesh mesh = new Mesh();
        mesh.vertices.add(new Vec3(-1, -1, 1));
        mesh.vertices.add(new Vec3(1, -1, 1));
        mesh.vertices.add(new Vec3(-1, 1, -1));
        mesh.vertices.add(new Vec3(1, 1, -1));
        
        mesh.vertices.add(new Vec3(-1, 1, 1));
        mesh.vertices.add(new Vec3(1, 1, 1));
        mesh.vertices.add(new Vec3(-1, -1, -1));
        mesh.vertices.add(new Vec3(1, -1, -1));

        mesh.triangles.add(new Triangle(0, 1, 2));
        mesh.triangles.add(new Triangle(3, 2, 1));
        mesh.triangles.add(new Triangle(4, 5, 6));
        mesh.triangles.add(new Triangle(7, 6, 5));

        return mesh;
    }
}
