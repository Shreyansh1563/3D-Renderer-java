package Math;

public class Mat4 {
    public float[][] mat = new float[4][4];


    public static Mat4 identity(){
        Mat4 result = new Mat4();
        for(int i=0; i<4; i++){
            result.mat[i][i] = 1;
        }
        return result;
    }


    public static Mat4 translation(float x, float y, float z){
        Mat4 result = identity();
        result.mat[0][3] = x;
        result.mat[1][3] = y;
        result.mat[2][3] = z;
        return result;
    }


    public static Mat4 rotationX(float angle) {

        Mat4 result = identity();

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        result.mat[1][1] = cos;
        result.mat[1][2] = -sin;
        result.mat[2][1] = sin;
        result.mat[2][2] = cos;

        return result;
    }

    public static Mat4 rotationY(float angle) {

        Mat4 result = identity();

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        result.mat[0][0] = cos;
        result.mat[0][2] = sin;
        result.mat[2][0] = -sin;
        result.mat[2][2] = cos;

        return result;
    }

    public static Mat4 rotationZ(float angle) {

        Mat4 result = identity();

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        result.mat[0][0] = cos;
        result.mat[0][1] = -sin;
        result.mat[1][0] = sin;
        result.mat[1][1] = cos;

        return result;
    }

    public static Mat4 scale(float sx, float sy, float sz) {

        Mat4 result = new Mat4();

        result.mat[0][0] = sx;
        result.mat[1][1] = sy;
        result.mat[2][2] = sz;
        result.mat[3][3] = 1;

        return result;
    }

    public static Mat4 perspective(float fov, float aspect, float near, float far) {

        Mat4 result = new Mat4();

        float f = (float)(1.0 / Math.tan(fov / 2.0f));

        result.mat[0][0] = f / aspect;
        result.mat[1][1] = f;

        result.mat[2][2] = (far + near) / (near - far);
        result.mat[2][3] = (2 * far * near) / (near - far);

        result.mat[3][2] = -1.0f;
        result.mat[3][3] = 0.0f;

        return result;
    }


    public Mat4 multiply(Mat4 other) {
        Mat4 result = new Mat4();

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                for (int k = 0; k < 4; k++) {
                    result.mat[row][col] += this.mat[row][k] * other.mat[k][col];
                }
            }
        }

        return result;
    }


    public Vec4 multiply(Vec4 v) {
        float[] r = new float[4];
        float[] vec = {v.x, v.y, v.z, v.w};

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                r[row] += mat[row][col] * vec[col];
            }
        }

        return new Vec4(r[0], r[1], r[2], r[3]);
    }
}
