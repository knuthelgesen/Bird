package no.plasmid.bird.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import no.plasmid.bird.im.Texture3D;

import org.lwjgl.opengl.GL11;

public class PNGImageLoader {

	private static final ColorModel GL_ALPHA_COLOR_MODEL = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 }, true,
			false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);

	private static final ColorModel GL_COLOR_MODEL = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 0 }, false, false,
			ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);

	/**
	 * Load images from file into a 3D texture, in the order the filenames are given. Will not register the texture with OpenGL
	 * @param texture
	 * @param fileNames
	 * @throws FileNotFoundException
	 */
	public void load3DTexture(Texture3D texture, String[] fileNames) throws FileNotFoundException {
		int imageCounter = 0;
		for (String fileName : fileNames) {
			URL imageUrl = PNGImageLoader.class.getResource(fileName);

			// Check that the image file is found
			if (imageUrl == null) {
				throw new FileNotFoundException("Image file " + fileName + "not found.");
			}

			// Load the image itself
			Image img = new ImageIcon(imageUrl).getImage();
			BufferedImage image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

			Graphics g = image.getGraphics();
			g.drawImage(img, 0, 0, null);
			g.dispose();

			// Determine pixelformat
			int imagePixelFormat = GL11.GL_RGB;
			if (image.getColorModel().hasAlpha()) {
				imagePixelFormat = GL11.GL_RGBA;
			}
			
			//Check that the image attributes are as expected
			texture.checkImageAttributes(imagePixelFormat, image.getWidth(), image.getHeight());
			
			texture.addImageData(extractImageData(image), imageCounter++);
		}
//		texture.getImageData().rewind();
//		texture.getImageData().flip();
	}
	
	/**
	 * Extract the raw image data from a buffered image
	 * @param image
	 * @return
	 */
	private ByteBuffer extractImageData(BufferedImage image) {
		ByteBuffer imageBuffer;
		BufferedImage tmpImage;
		WritableRaster raster;

		// Crate a raster and temporary image that can be used by OpenGL as a source for a texture
		if (image.getColorModel().hasAlpha()) {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, image.getWidth(), image.getHeight(), 4, null);
			tmpImage = new BufferedImage(GL_ALPHA_COLOR_MODEL, raster, false, new Hashtable<String, Object>());
		} else {
			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, image.getWidth(), image.getHeight(), 3, null);
			tmpImage = new BufferedImage(GL_COLOR_MODEL, raster, false, new Hashtable<String, Object>());
		}

		// Copy the data into the temporary image
		Graphics g = tmpImage.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();

		// Build a byte buffer from the temp image that will be used by OpenGL to produce a texture
		byte[] imageData = ((DataBufferByte) tmpImage.getRaster().getDataBuffer()).getData();

		imageBuffer = ByteBuffer.allocateDirect(imageData.length);
		imageBuffer.order(ByteOrder.nativeOrder());
		imageBuffer.put(imageData);
		imageBuffer.flip();

		return imageBuffer;
	}
}
