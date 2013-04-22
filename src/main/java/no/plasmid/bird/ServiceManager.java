package no.plasmid.bird;

public class ServiceManager {

	private static ServiceManager instance = new ServiceManager();
	
	/**
	 * Get the instance
	 * @return
	 */
	public static ServiceManager getInstance() {
		return instance;
	}
	
	/*
	 * Services
	 */
	private InputHandler inputHandler;
	private Renderer renderer;
	private ShaderManager shaderManager;
	private TerrainTileManager terrainTileManager;
	private TextureManager textureManager;
	
	/**
	 * Private constructor
	 */
	private ServiceManager() {
		inputHandler = new InputHandler();
		renderer = new Renderer();
		shaderManager = new ShaderManager();
		terrainTileManager = new TerrainTileManager();
		textureManager = new TextureManager();
	}

	public InputHandler getInputHandler() {
		return inputHandler;
	}

	public Renderer getRenderer() {
		return renderer;
	}

	public ShaderManager getShaderManager() {
		return shaderManager;
	}

	public TerrainTileManager getTerrainTileManager() {
		return terrainTileManager;
	}
	
	public TextureManager getTextureManager() {
		return textureManager;
	}
}
