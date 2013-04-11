package no.plasmid.bird;

import java.util.List;

import no.plasmid.bird.im.Camera;
import no.plasmid.bird.im.Terrain;
import no.plasmid.bird.im.TerrainTile;
import no.plasmid.bird.im.Vertex3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;

public class Renderer {

	private ShaderManager shaderManager;
	
	/**
	 * Initialize the rendering system
	 */
	public void initializeRenderer() {
		shaderManager = ServiceManager.getInstance().getShaderManager();

		GL11.glViewport(0, 0, Configuration.WINDOW_WIDTH, Configuration.WINDOW_HEIGTH);
//		GL11.glFrustum(0, Configuration.WINDOW_WIDTH, 0, Configuration.WINDOW_HEIGTH, 1, 10000);

		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
//		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		checkGL();
	}
	
	/**
	 * Render the scene
	 */
	public void renderTerrain(List<TerrainTile> tileList, Terrain terrain, Camera camera, Long shaderId) {
		//Clear the display
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		//Prepare for 3D rendering
		prepare3D();
		checkGL();
		
		//Enable shader
		GL20.glUseProgram(shaderManager.getShader(shaderId));
		
		//Rotate the camera
		double[] cameraRotValues = camera.getRotation().getValues();
		GL11.glRotated(cameraRotValues[0], 1.0, 0.0, 0.0);
		GL11.glRotated(cameraRotValues[1], 0.0, 1.0, 0.0);
		GL11.glRotated(cameraRotValues[2], 0.0, 0.0, 1.0);
		//Move the camera
		double[] cameraPosValues = camera.getPosition().getValues();
		GL11.glTranslated(cameraPosValues[0], cameraPosValues[1], cameraPosValues[2]);
		
		for (TerrainTile tile : tileList) {
			try {
				if (tile.isReadyForDrawing()) {
					Vertex3d[][] strips = tile.getMesh().getStrips();
					Vertex3d[][] normals = tile.getMesh().getNormals();
					int[] vertexCounts = tile.getMesh().getVertexCounts();
					int detail = tile.getDetail();
					for (int tileX = 0; tileX < detail; tileX++) {
						GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
						{
							try {
								for (int i = 0; i < vertexCounts[tileX]; i++) {
									GL11.glNormal3d(normals[tileX][i].getValues()[0], normals[tileX][i].getValues()[1], normals[tileX][i].getValues()[2]);
									GL11.glVertex3d(strips[tileX][i].getValues()[0], strips[tileX][i].getValues()[1], strips[tileX][i].getValues()[2]);
								}
							} catch (Exception e) {
								GL11.glEnd();
								e.printStackTrace();
								continue;
							}
					}
						GL11.glEnd();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		
		//Disable shader
		GL20.glUseProgram(0);
	}

	/**
	 * Create a shader program from source
	 * @param vertexShaderSource
	 * @param fragmentShaderSource
	 * @return
	 */
	public int createShader(String vertexShaderSource,
			String fragmentShaderSource) {
		//Create vertex shader
		int vertexShader = compileShader(vertexShaderSource, GL20.GL_VERTEX_SHADER);
		//Create fragment shader
		int fragmentShader = compileShader(fragmentShaderSource, GL20.GL_FRAGMENT_SHADER);
		//Link shaders
		int shader = linkShader(vertexShader, fragmentShader);
		
		checkGL();
		
		return shader;
	}
	
	/**
	 * Check for OpenGL error, and throw exception if any are found
	 */
	private void checkGL() {
		final int code = GL11.glGetError();
		if (code != 0) {
			final String errorString = GLU.gluErrorString(code);
			final String message = "OpenGL error (" + code + "): " + errorString;
			throw new IllegalStateException(message);
		}
	}
	
	/**
	 * Prepare for 3D rendering.
	 */
	private void prepare3D() {
	    GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
	    GLU.gluPerspective(
	        90.0f,
	        (float) Configuration.WINDOW_WIDTH / (float) Configuration.WINDOW_HEIGTH,
	        1.0f,
	        40000.0f);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	
	/**
	 * Compile a shader
	 * @param shaderSource
	 * @param shaderType
	 * @return
	 */
	private int compileShader(String shaderSource, int shaderType) {
		int shader;
		
		//Create vertex shader
		shader = GL20.glCreateShader(shaderType);
		GL20.glShaderSource(shader, shaderSource);
		GL20.glCompileShader(shader);
		boolean compileOK = GL20.glGetShader(shader, GL20.GL_COMPILE_STATUS) == 1;
		if (!compileOK) {
			String errorMessage = "Shader failed to compile: " + GL20.glGetShaderInfoLog(shader, 1000);
	    	System.err.println(errorMessage);
	    	throw new IllegalStateException(errorMessage);
		}
		checkGL();
		
		return shader;
	}
	
	/**
	 * Link a vertex and a fragment shader to a complete shader program
	 * @param vertexShader
	 * @param fragmentShader
	 * @return
	 */
	private int linkShader(int vertexShader, int fragmentShader) {
		int shader = GL20.glCreateProgram();
		GL20.glAttachShader(shader, vertexShader);
		GL20.glAttachShader(shader, fragmentShader);
		GL20.glLinkProgram(shader);
		boolean compileOK = GL20.glGetProgram(shader, GL20.GL_LINK_STATUS) == 1;
		if (!compileOK) {
			String errorMessage = "Shader failed to compile: " + GL20.glGetProgramInfoLog(shader, 1000);
	    	System.err.println(errorMessage);
	    	throw new IllegalStateException(errorMessage);
		}
		checkGL();
		
		return shader;
	}
	
}
