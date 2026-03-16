package scene;

import Math.Vec3;
import graphics.Texture;

public class GameObject {
    public Mesh mesh;
    public Transform transform;
    public Texture texture;

    public GameObject(Mesh mesh) {
        this.mesh = mesh;
        this.transform = new Transform(new Vec3(0, 0, 0), new Vec3(0,0,0), new Vec3(1,1,1));
    }

    public GameObject(Mesh mesh, Transform transform){
        this.mesh = mesh;
        this.transform = transform;
    }

    public GameObject(Mesh mesh, Transform transform, Texture texture){
        this.mesh = mesh;
        this.texture = texture;
        this.transform = transform;
    }
}
