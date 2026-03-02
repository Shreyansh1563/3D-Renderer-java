package engine;

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import graphics.Renderer;

public class Display extends JPanel{
    public Renderer renderer;

    public Display(Renderer renderer){
        this.renderer = renderer;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // renderer.updateImage();
        g.drawImage(renderer.getImage(), 0, 0, null);
    }

    public static Display createDisplay(int width, int height, Renderer renderer){
        JFrame frame = new JFrame("3D renderer");
        Display display = new Display(renderer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(display);
        frame.setSize(width, height);
        frame.setVisible(true);
        return display;
    }
}
