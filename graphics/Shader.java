package graphics;

import Math.Vec3;

public class Shader {

    // Light direction
    private Vec3 lightDirection = new Vec3(1, 1, 1).normalize();

    // Lighting parameters
    private float ambient = 0.2f;
    private float specularStrength = 0.5f;
    private int shininess = 32;

    public int shadeFragment(FragmentData data) {

        // Normal
        Vec3 normal = data.normal.normalize();

        // Light direction
        Vec3 lightDir = lightDirection.normalize();

        // Diffuse lighting (Lambert)
        float lambert = Math.max(0.0f, normal.dot(lightDir));

        // View direction (camera is at origin in view space)
        Vec3 viewDir = data.viewPosition.multiply(-1).normalize();

        // Blinn-Phong halfway vector
        Vec3 halfway = lightDir.add(viewDir).normalize();

        // Specular term
        float spec = (float)Math.pow(
                Math.max(normal.dot(halfway), 0.0f),
                shininess
        );

        // Combine lighting
        float brightness = ambient
                + (1.0f - ambient) * lambert
                + specularStrength * spec;

        // Clamp brightness
        brightness = Math.min(brightness, 1.0f);

        // Base object color
        int baseR = 200;
        int baseG = 200;
        int baseB = 200;

        // Apply brightness
        int r = Math.min(255, (int)(baseR * brightness));
        int g = Math.min(255, (int)(baseG * brightness));
        int b = Math.min(255, (int)(baseB * brightness));

        // Convert to RGB
        return (r << 16) | (g << 8) | b;
    }
}