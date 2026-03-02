package scene;

import Math.Mat4;
import Math.Vec3;

public class Transform {

    public Vec3 position, rotation, scale;

    public Transform(Vec3 position, Vec3 rotation, Vec3 scale){
        this.position = position;
        this.scale = scale;
        this.rotation = rotation;
    }


    public Mat4 getModelMatrix(){

        Mat4 translation = Mat4.translation(position.x, position.y, position.z);

        Mat4 rotationX = Mat4.rotationX(rotation.x);
        Mat4 rotationY = Mat4.rotationY(rotation.y);
        Mat4 rotationZ = Mat4.rotationZ(rotation.z);

        Mat4 scaleMatrix = Mat4.scale(scale.x, scale.y, scale.z);

        Mat4 rotation = rotationZ
                            .multiply(rotationY)
                            .multiply(rotationX);

        return translation
                .multiply(rotation)
                .multiply(scaleMatrix);
    }
}