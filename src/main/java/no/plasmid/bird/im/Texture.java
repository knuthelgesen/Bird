package no.plasmid.bird.im;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Texture {

	public static final int GL_TEXTURE_ID_NOT_REGISTERED = 0; // OpenGL texture id used when not registered by OpenGL

	private int glTextureId; // Texture ID from OpenGL

	private int pixelFormat; // Pixel format, OpenGL enumeration
	private int bytesPerPixel;

	private int width;

	private int height;

	private int depth;		//Actually number of 2D images
	
	private ByteBuffer imageData;

	public Texture(int pixelFormat, int width, int height, int depth) {
		glTextureId = GL_TEXTURE_ID_NOT_REGISTERED;

		this.pixelFormat = pixelFormat;

		this.width = width;
		this.height = height;
		this.depth = depth;

		//Allocate space for image data
		if (pixelFormat == GL11.GL_RGB) {
			//No alpha
			this.bytesPerPixel = 3;
		} else {
			//With alpha
			this.bytesPerPixel = 4;
		}
		
		imageData = BufferUtils.createByteBuffer(width * height * depth * bytesPerPixel);
	}
	
	public void checkImageAttributes(int imagePixelFormat, int imageWidth, int imageHeight) {
		if (imagePixelFormat != pixelFormat) {
			throw new IllegalArgumentException("Image pixel format does not match pixel format expected by texture");
		}
		if (imageWidth != width) {
			throw new IllegalArgumentException("Image width does not match width expected by texture");
		}
		if (imageHeight != height) {
			throw new IllegalArgumentException("Image height does not match height expected by texture");
		}
	}

	public int getGllTextureId() {
		return glTextureId;
	}
	
	public void setGlTextureId(int glTextureId) {
		this.glTextureId = glTextureId;
	}
	
	public int getPixelFormat() {
		return pixelFormat;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getDepth() {
		return depth;
	}

	public ByteBuffer getImageData() {
		return imageData;
	}

	public void addImageData(ByteBuffer data, int imageCounter) {
		imageData.position(width * height * imageCounter * bytesPerPixel);
		imageData.put(data);
		imageData.rewind();
	}

}
