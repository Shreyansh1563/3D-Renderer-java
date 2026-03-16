package engine;

import graphics.Camera;
import graphics.Renderer;
import scene.GameObject;
import scene.Scene;

public class Engine implements Runnable{
    private Renderer renderer;
    private Display display;
    private Camera camera;
    private GameObject object;
    private Scene scene;

    private boolean running = false;

    private Thread gameThread;

    private static final double TARGET_FPS = 60.0;
    private static final double NS_PER_FRAME = 1_000_000_000.0 / TARGET_FPS;

    public Engine(Renderer renderer, Display display, Camera camera, GameObject object){
        this.renderer = renderer;
        this.display = display;
        this.camera = camera;
        this.object = object;
    }

    public Engine(Renderer renderer, Display display, Camera camera, Scene scene){
        this.renderer = renderer;
        this.display = display;
        this.camera = camera;
        this.scene = scene;
    }

    public synchronized void start(){
        if (running) return;
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
    }

    public synchronized void stop(){
        running = false;
    }

    @Override
    public void run(){

        long lastTime = System.nanoTime();
        double delta = 0;

        while(running){
            long now = System.nanoTime();
            delta += (now - lastTime) /NS_PER_FRAME;
            lastTime = now;

            while(delta >= 1){
                // update();
                updateScene();
                delta--;
            }
            
            renderScene();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }


    }

    private void update(){
        object.transform.rotation.y += 0.01f;
        // camera.position.z -= 0.01f;
    }

    private void render(){
        renderer.clear();
        renderer.render(object, camera);

        // display.repaint();
        renderer.updateImage();   // build the frame buffer here
        display.repaint(); 
    }

    public void renderScene(){
        renderer.clear();
        renderer.renderScene(scene);
        renderer.updateImage();
        display.repaint();
    }

    private void updateScene(){
        var lis = scene.getObjects();
        lis.get(0).transform.rotation.x += 0.01f;
        lis.get(1).transform.rotation.y -= 0.01f;
        // camera.position.z -= .05f;
        // camera.rotation.y += 0.01f;
    }
}
