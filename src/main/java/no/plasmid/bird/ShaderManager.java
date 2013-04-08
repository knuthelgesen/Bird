package no.plasmid.bird;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ShaderManager {

	private Map<Long, Integer> shaderMap;	//<id, OpenGL id>
	
	public ShaderManager() {
		shaderMap = new HashMap<Long, Integer>();
	}
	
	public void createShader(Long shaderId, String vertexShaderSourceFileName,
			String fragmentShaderSourceFileName, Renderer renderer) {
		try {
			String vertexSource = loadTextFile(vertexShaderSourceFileName);
			String fragmentSource = loadTextFile(fragmentShaderSourceFileName);
			int shader = renderer.createShader(vertexSource, fragmentSource);
			shaderMap.put(shaderId, shader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Integer getShader(Long shaderId) {
		return shaderMap.get(shaderId);
	}

	private String loadTextFile(String fileName) throws FileNotFoundException {
		URL fileURL = ShaderManager.class.getResource(fileName);
		if (fileURL == null) {
			throw new FileNotFoundException("Could not find shader source file " + fileName);
		}
		
		StringBuilder text = new StringBuilder();
		String NL = System.getProperty("line.separator");
		FileInputStream fis;
		Scanner scanner = null;
		try {
			fis = new FileInputStream(fileURL.getFile());
			scanner = new Scanner(fis);
			
			while (scanner.hasNextLine()) {
				text.append(scanner.nextLine() + NL);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		
		return text.toString();
	}
	
}
