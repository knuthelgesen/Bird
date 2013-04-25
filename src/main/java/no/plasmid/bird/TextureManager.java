package no.plasmid.bird;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import no.plasmid.bird.im.Texture;
import no.plasmid.bird.util.PNGImageLoader;

public class TextureManager {

	private Map<Long, Integer> textureMap;		//<id, OpenGL id>
	
	public TextureManager() {
		textureMap = new HashMap<Long, Integer>();
	}
	
	public Integer getTexture(Long textureId) {
		return textureMap.get(textureId);
	}
	
	public void load2DTexture(String fileName) {
		
	}
	
	public void loadTexture(Long textureId, int size, String[] fileNames) {
		try {
			Texture texture = new Texture(GL11.GL_RGBA, size, size, fileNames.length);
			PNGImageLoader loader = new PNGImageLoader();
			loader.loadTexture(texture, fileNames);
			ServiceManager.getInstance().getRenderer().registerTextureWithOpenGL(texture);
			textureMap.put(textureId, texture.getGllTextureId());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
