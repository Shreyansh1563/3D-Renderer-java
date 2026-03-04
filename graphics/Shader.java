package graphics;

import Math.Vec3;

public class Shader{

    private Vec3 lightDirection = new Vec3(1,1,1).normalize();
    private float ambient = 0.2f;

    public int shadeFragment(FragmentData data) {
        
        Vec3 normal = data.normal.normalize();
        float lambert = Math.max(0, normal.dot(lightDirection));
        float brightness = ambient + (1 - ambient) * lambert;

        int baseR = 200;
        int baseG = 200;
        int baseB = 200;

        int r = (int) (baseR * brightness);
        int g = (int) (baseG * brightness);
        int b = (int) (baseB * brightness);

        return (r << 16) | (g << 8) | b;
    }
    
}
