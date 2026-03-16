package scene;

import java.util.ArrayList;
import java.util.List;

import Math.Vec2;
import Math.Vec3;

public class Mesh {
    public List<Vec3> vertices = new ArrayList<>();
    public List<Triangle> triangles = new ArrayList<>();
    public List<Vec3> normals = new ArrayList<>();
    public List<Vec2> uvs = new ArrayList<>();
    // public List<Edge> edges = new ArrayList<>();

    public static Mesh createSphere(int latSteps, int lonSteps, float radius) {

        Mesh mesh = new Mesh();

        for (int lat = 0; lat <= latSteps; lat++) {

            float v = (float)lat / latSteps;
            float theta = (float)(Math.PI * v);

            for (int lon = 0; lon <= lonSteps; lon++) {

                float u = (float)lon / lonSteps;
                float phi = (float)(2 * Math.PI * u);

                float x = (float)(Math.sin(theta) * Math.cos(phi));
                float y = (float)Math.cos(theta);
                float z = (float)(Math.sin(theta) * Math.sin(phi));

                Vec3 pos = new Vec3(x * radius, y * radius, z * radius);

                mesh.vertices.add(pos);
                mesh.normals.add(new Vec3(x, y, z));
                mesh.uvs.add(new Vec2(u, v));
            }
        }

        for (int lat = 0; lat < latSteps; lat++) {
            for (int lon = 0; lon < lonSteps; lon++) {

                int i0 = lat * (lonSteps + 1) + lon;
                int i1 = i0 + 1;
                int i2 = i0 + lonSteps + 1;
                int i3 = i2 + 1;

                mesh.triangles.add(new Triangle(i0, i2, i1));
                mesh.triangles.add(new Triangle(i1, i2, i3));
            }
        }

        return mesh;
    }

    public static Mesh createTorus(int ringSteps, int tubeSteps, float R, float r) {

        Mesh mesh = new Mesh();

        for (int i = 0; i <= ringSteps; i++) {

            float u = (float)i / ringSteps * (float)(2 * Math.PI);

            for (int j = 0; j <= tubeSteps; j++) {

                float v = (float)j / tubeSteps * (float)(2 * Math.PI);

                float x = (float)((R + r * Math.cos(v)) * Math.cos(u));
                float y = (float)((R + r * Math.cos(v)) * Math.sin(u));
                float z = (float)(r * Math.sin(v));

                Vec3 pos = new Vec3(x, y, z);

                Vec3 normal = new Vec3(
                        (float)(Math.cos(u) * Math.cos(v)),
                        (float)(Math.sin(u) * Math.cos(v)),
                        (float)Math.sin(v)
                );

                mesh.vertices.add(pos);
                mesh.normals.add(normal.normalize());

                float uu = (float)i / ringSteps;
                float vv = (float)j / tubeSteps;

                mesh.uvs.add(new Vec2(uu, vv));
            }
        }

        for (int i = 0; i < ringSteps; i++) {
            for (int j = 0; j < tubeSteps; j++) {

                int a = i * (tubeSteps + 1) + j;
                int b = a + 1;
                int c = a + tubeSteps + 1;
                int d = c + 1;

                mesh.triangles.add(new Triangle(a, c, b));
                mesh.triangles.add(new Triangle(b, c, d));
            }
        }

        return mesh;
    }

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
