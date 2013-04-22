package no.plasmid.bird;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import no.plasmid.bird.im.Texture3D;
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
	
	public void load3DTexture(Long textureId, String[] fileNames) {
		try {
			Texture3D texture = new Texture3D(GL11.GL_RGBA, 512, 512, 2);
			PNGImageLoader loader = new PNGImageLoader();
			loader.load3DTexture(texture, fileNames);
			ServiceManager.getInstance().getRenderer().register3DTextureWithOpenGL(texture);
			textureMap.put(textureId, texture.getGllTextureId());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
