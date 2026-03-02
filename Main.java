import Math.Vec3;
import engine.Display;
import engine.Engine;
import graphics.Camera;
import graphics.Renderer;
import scene.GameObject;
import scene.Mesh;
import scene.Scene;
import scene.Transform;

public class Main {
    public static void main(String[] args) {
        
        Mesh cube = Mesh.cubeMesh();
        Mesh sphere = Mesh.sphereMesh(1, 12, 12);
        Mesh torus = Mesh.createTorus(40, 20, 6, 2);

        Transform transform = new Transform(new Vec3(0, 0, -5), new Vec3(0, 0, 0), new Vec3(1, 1, 1));
        Transform transform2 = new Transform(new Vec3(1, 0, -5), new Vec3(0, 0, 0), new Vec3(1, 1, 1));
        Transform transform3 = new Transform(new Vec3(0, 0, -5), new Vec3(0, 0, (float)Math.toRadians(90)), new Vec3(1, 1, 1));

        GameObject object1 = new GameObject(cube, transform);
        GameObject object2 = new GameObject(sphere, transform2);
        GameObject object3 = new GameObject(torus, transform3);

        // Mesh sphere = Mesh.createWireSphere(2f, 16, 10);
    //     // GameObject sph = new GameObject(cube, transform);

        
    // // float fov   = (float)Math.toRadians(70);
    // // float aspect = (float)width / height;
    // // float near  = 0.1f;
    // // float far   = 100f;


        Camera camera = new Camera((float)Math.toRadians(70), 1, 0.1f, 1000);
        camera.position.z = 12;
        // camera.rotation.x = -0.495498f;
        Scene scene = new Scene(camera);
        scene.addObject(object3);
        // scene.addObject(object2);

        Renderer renderer = new Renderer(500, 500);
        // renderer.setBackfaceCulling(false);
        
    //     renderer.render(object, camera);

        Display display = Display.createDisplay(500, 500, renderer);
    //     Display display2 = Display.createDisplay(500, 500, renderer);

    //     Engine engine = new Engine(renderer, display, camera, object);

    //     engine.start();
        
        Engine scEngine = new Engine(renderer, display, camera, scene);

        scEngine.start();
    }


}
