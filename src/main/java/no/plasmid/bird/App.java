package no.plasmid.bird;

import no.plasmid.bird.im.Camera;
import no.plasmid.bird.im.Terrain;
import no.plasmid.bird.im.TerrainTile;
import no.plasmid.bird.im.Vertex3d;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public class App 
{
	
	private InputHandler inputHandler;
	private Renderer renderer;
	
    public static void main( String[] args )
    {
    	App app = new App();
    	app.initializeApplication();
    	app.runApplication();
    	app.cleanupApplication();
    }
    
    /**
     * Perform initialization to prepare for running
     */
    private void initializeApplication() {
    	//Open the program window
        try {
        	Display.setDisplayMode(new DisplayMode(Configuration.WINDOW_WIDTH, Configuration.WINDOW_HEIGTH));
        	Display.setTitle(Configuration.WINDOW_TITLE);
			Display.create();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}
        
        //Create input handler
        inputHandler = new InputHandler();
        
        //Create and initialize the renderer
        renderer = new Renderer();
        renderer.initializeRenderer();
    }
    
    /**
     * Run the application
     */
    private void runApplication() {
    	//Create the camera
    	Camera camera = new Camera();
    	camera.setPosition(new Vertex3d(new double[]{0.0, -8.0, 0.0}));
    	camera.setRotation(new Vertex3d(new double[]{0.0, 135.0, 0.0}));
    	
        //Create the terrain
    	Terrain terrain = new Terrain();
    	
    	while (!inputHandler.isCloseRequested()) {
    		//Render scene
    		renderer.render(terrain, camera);
    		
    		//Handle input
    		inputHandler.handleInput();
    		
    		//Update camera
    		updateCamera(camera);
    		
    		//Lock to 60 FPS
    		Display.sync(60);
    		
    		//Update display
    		Display.update();
    	}
    }
    
    /**
     * Clean up after the application
     */
    private void cleanupApplication() {
    	//Destroy the program window
    	Display.destroy();
    }

    private void updateCamera(Camera camera) {
    	boolean[] keyStatus = inputHandler.getKeyStatus();
    	
		if (keyStatus[Keyboard.KEY_LSHIFT] || keyStatus[Keyboard.KEY_RSHIFT]) {
			if (keyStatus[Keyboard.KEY_LEFT]) {
				camera.rotateCamera(0.0f, -1.5f, 0.0f);
			}
			if (keyStatus[Keyboard.KEY_RIGHT]) {
				camera.rotateCamera(0.0f, 1.5f, 0.0f);
			}
			if (keyStatus[Keyboard.KEY_UP]) {
				camera.rotateCamera(1.5f, 0.0f, 0.0f);
			}
			if (keyStatus[Keyboard.KEY_DOWN]) {
				camera.rotateCamera(-1.5f, 0.0f, 0.0f);
			}
			if (keyStatus[Keyboard.KEY_PRIOR]) {
//				camera.zAngle -= 0.2f;
			}
			if (keyStatus[Keyboard.KEY_NEXT]) {
//				camera.zAngle += 0.2f;
			}
		} else {
			if (keyStatus[Keyboard.KEY_LEFT]) {
				camera.moveCamera(40.2f, 0.0f, 0.0f);
			}
			if (keyStatus[Keyboard.KEY_RIGHT]) {
				camera.moveCamera(-40.2f, 0.0f, 0.0f);
			}
			if (keyStatus[Keyboard.KEY_UP]) {
				camera.moveCamera(0.0f, 0.0f, 40.2f);
			}
			if (keyStatus[Keyboard.KEY_DOWN]) {
				camera.moveCamera(0.0f, 0.0f, -40.2f);
			}
			if (keyStatus[Keyboard.KEY_PRIOR]) {
				camera.moveCamera(0.0f, -40.2f, 0.0f);
			}
			if (keyStatus[Keyboard.KEY_NEXT]) {
				camera.moveCamera(0.0f, 40.2f, 0.0f);
			}
		}
    }
}
