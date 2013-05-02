package no.plasmid.bird;

import no.plasmid.bird.im.Camera;
import no.plasmid.bird.im.Terrain;
import no.plasmid.bird.im.Vertex3d;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

public class App 
{
	
	private InputHandler inputHandler;
	private Renderer renderer;
	private ShaderManager shaderManager;
	private TerrainTileManager terrainTileManager;
	
	private RenderMode renderMode;
	
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
        	PixelFormat pf = new PixelFormat(32, 8, 16, 0, 16);
        	Display.setDisplayMode(new DisplayMode(Configuration.WINDOW_WIDTH, Configuration.WINDOW_HEIGTH));
        	Display.setTitle(Configuration.WINDOW_TITLE);
			Display.create(pf);
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}
        
        ServiceManager serviceManager = ServiceManager.getInstance();
        
        //Get input handler
        inputHandler = serviceManager.getInputHandler();
        
        //Get and initialize the renderer
        renderer = serviceManager.getRenderer();
        renderer.initializeRenderer();
        
        //Get the shader manager
        shaderManager = serviceManager.getShaderManager();
        //Create shaders
        shaderManager.createShader(1L, "/shader/terrain.vertex.shader",  "/shader/terrain.fragment.shader", renderer);
        shaderManager.createShader(2L, "/shader/terrain_tile_color.vertex.shader",  "/shader/terrain_tile_color.fragment.shader", renderer);
        
        //Load textures
        TextureManager textureManager = serviceManager.getTextureManager();
        textureManager.loadTexture(1L, 512, new String[]{"/img/sand512a.png", "/img/stonea512.png", "/img/dirt512a.png", "/img/grassa512.png"});
        
        //Get the terrain tile manager
        terrainTileManager = serviceManager.getTerrainTileManager();
        
        //Set default render mode
        renderMode = RenderMode.NORMAL;
    }
    
    /**
     * Run the application
     */
    private void runApplication() {
    	//Create the camera
    	Camera camera = new Camera();
    	camera.setPosition(new Vertex3d(new double[]{-10000.0, -1000.0, -10000.0}));
    	camera.setRotation(new Vertex3d(new double[]{0.0, 135.0, 0.0}));
    	
        //Create the terrain
    	Terrain terrain = new Terrain();
    	
    	//Start thread that updates terrain tiles
    	terrainTileManager.startTerainTileUpdateThread(terrain, camera);
    	
    	while (!inputHandler.isCloseRequested()) {
        	//Render scene
    		switch (renderMode) {
    		case NORMAL:
    			//Normal rendering
        		renderer.renderTerrainNormal(terrainTileManager.getTileList(), camera, 1L, 1L);
    			break;
    		case ID_COLOR:
    			//"Clown mode"
    			renderer.renderTerrainTileIdColors(terrainTileManager.getTileList(), camera, 2L);
    			break;
    		case TEMPERATURE:
    			//Render with color indicating the tile temperature
    			renderer.renderTerrainTemperatureColors(terrainTileManager.getTileList(), camera, 2L);
    			break;
    		case MOISTURE:
    			break;
			default:
				//Should not happen
				//TODO log
				break;
    		}
    		
    		//Handle input
    		inputHandler.handleInput();
    		
    		//Update camera
    		updateCamera(camera);
    		
    		//Lock to 60 FPS
    		Display.sync(60);
    		
    		//Update display
    		Display.update();
    	}
    	
    	terrainTileManager.stopTerrainTileUpdateThread();
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
			//Movement
			if (keyStatus[Keyboard.KEY_LEFT]) {
				camera.moveCamera(Configuration.CAMERA_MOVEMENT_SPEED, 0.0f, 0.0f);
			}
			if (keyStatus[Keyboard.KEY_RIGHT]) {
				camera.moveCamera(-Configuration.CAMERA_MOVEMENT_SPEED, 0.0f, 0.0f);
			}
			if (keyStatus[Keyboard.KEY_UP]) {
				camera.moveCamera(0.0f, 0.0f, Configuration.CAMERA_MOVEMENT_SPEED);
			}
			if (keyStatus[Keyboard.KEY_DOWN]) {
				camera.moveCamera(0.0f, 0.0f, -Configuration.CAMERA_MOVEMENT_SPEED);
			}
			if (keyStatus[Keyboard.KEY_PRIOR]) {
				camera.moveCamera(0.0f, -Configuration.CAMERA_MOVEMENT_SPEED, 0.0f);
			}
			if (keyStatus[Keyboard.KEY_NEXT]) {
				camera.moveCamera(0.0f, Configuration.CAMERA_MOVEMENT_SPEED, 0.0f);
			}
			//Render mode select
			if (keyStatus[Keyboard.KEY_1]) {
				renderMode = RenderMode.NORMAL;
			}
			if (keyStatus[Keyboard.KEY_2]) {
				renderMode = RenderMode.ID_COLOR;
			}
			if (keyStatus[Keyboard.KEY_3]) {
				renderMode = RenderMode.TEMPERATURE;
			}
			if (keyStatus[Keyboard.KEY_4]) {
				renderMode = RenderMode.MOISTURE;
			}
		}
    }
    
    public enum RenderMode {
    	NORMAL, ID_COLOR, TEMPERATURE, MOISTURE;
    }
}
