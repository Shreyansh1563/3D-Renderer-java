package graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Texture {

    public int width;
    public int height;
    public int[] pixels;

    public Texture(String path) {

        try {

            BufferedImage img = ImageIO.read(new File(path));

            width = img.getWidth();
            height = img.getHeight();

            pixels = new int[width * height];

            img.getRGB(0, 0, width, height, pixels, 0, width);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Texture(int width, int height){
        this.width = width;
        this.height = height;

        pixels = new int[width * height];

        generateCheckerboard();
    }

    // public int sample(float u, float v) {

    //     int x = (int)(u * (width - 1));
    //     int y = (int)(v * (height - 1));

    //     return pixels[y * width + x];
    // }

    public int sample(float u, float v) {

        u = Math.max(0, Math.min(1, u));
        v = Math.max(0, Math.min(1, v));

        int x = (int)(u * (width - 1));
        int y = (int)(v * (height - 1));

        return pixels[y * width + x];
    }

    private void generateCheckerboard() {

        int size = 32;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                boolean checker = ((x / size) + (y / size)) % 2 == 0;

                int color = checker ? 0xFFFFFF : 0x222222;

                pixels[y * width + x] = color;
            }
        }
    }
}