package scene;

import Math.Vec3;

public class GameObject {
    public Mesh mesh;
    public Transform transform;

    public GameObject(Mesh mesh) {
        this.mesh = mesh;
        this.transform = new Transform(new Vec3(0, 0, 0), new Vec3(0,0,0), new Vec3(1,1,1));
    }

    public GameObject(Mesh mesh, Transform transform){
        this.mesh = mesh;
        this.transform = transform;
    }
}
