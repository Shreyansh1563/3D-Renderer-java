package graphics;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Math.Mat4;
import Math.Vec2;
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

    private VertexProcessor vertexProcessor = new VertexProcessor();
    private Rasterizer rasterizer = new Rasterizer();


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

        for(int i=0; i<object.mesh.vertices.size(); i++){
            vertices.add(vertexProcessor.process(
                model,
                view, 
                projection, 
                object.mesh.vertices.get(i), 
                object.mesh.normals.get(i), 
                object.mesh.uvs.get(i),
                width,
                height
            ));
        }


        for (Triangle triangle : object.mesh.triangles) {
            rasterizer.processTriangle(triangle, vertices, object, colorBuffer, depthBuffer, width, height);
        }
    }
}