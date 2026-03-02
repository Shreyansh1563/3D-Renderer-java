package graphics;

import Math.Mat4;
import Math.Vec3;

public class Camera {

    public Vec3 position;
    public Vec3 rotation;

    public float fov;
    public float aspect;
    public float near;
    public float far;

    
    public Camera(float fov, float aspect, float near, float far) {
        this.position = new Vec3(0, 0, 0);
        this.rotation = new Vec3(0, 0, 0);

        this.fov = fov;
        this.aspect = aspect;
        this.near = near;
        this.far = far;
    }

    public Mat4 getViewMatrix() {
        Mat4 rotX = Mat4.rotationX(-rotation.x);
        Mat4 rotY = Mat4.rotationY(-rotation.y);
        Mat4 rotZ = Mat4.rotationZ(-rotation.z);

        Mat4 rotationMatrix = rotX.multiply(rotY).multiply(rotZ);

        Mat4 translationMatrix = Mat4.translation(
            -position.x,
            -position.y,
            -position.z
        );

        return rotationMatrix.multiply(translationMatrix);
    }

    public Mat4 getProjectionMatrix() {
        return Mat4.perspective(fov, aspect, near, far);
    }
}