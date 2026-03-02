package scene;

import java.util.ArrayList;
import java.util.List;
import graphics.Camera;

public class Scene {

    private List<GameObject> objects = new ArrayList<>();
    private Camera camera;

    public Scene(Camera camera) {
        this.camera = camera;
    }

    public void addObject(GameObject obj) {
        objects.add(obj);
    }

    public List<GameObject> getObjects() {
        return objects;
    }

    public Camera getCamera() {
        return camera;
    }
}