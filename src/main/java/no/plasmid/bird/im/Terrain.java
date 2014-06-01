package no.plasmid.bird.im;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL11;

import no.plasmid.bird.Configuration;
import no.plasmid.bird.util.PNGImageLoader;
import no.plasmid.bird.util.PerlinNoise;

public class Terrain {

	private double[][] heightMap;
	private double[][] temperatureMap;
	private double[][] moistureMap;
	private double[][] steepnessMap;
	private TerrainTile[][] tiles;
	
	//These values are common for all the tiles. Therefore stored here
	private double[][] tileNoiseHeightMap;
	
	public Terrain() {
		//Load and process the tile noise height map
		tileNoiseHeightMap = new double[Configuration.TERRAIN_TILE_SIZE][Configuration.TERRAIN_TILE_SIZE];
		try {
			//Load the noise map for terrain tiles, and store the values for height and steepness
			PNGImageLoader imageLoader = new PNGImageLoader();
			Texture noiseTexture = new Texture(GL11.GL_RGBA, 256, 256, 1);
			imageLoader.loadTexture(noiseTexture, new String[]{"/img/noisea256.png"});
			ByteBuffer data = noiseTexture.getImageData();
			data.order(ByteOrder.LITTLE_ENDIAN);
			data.rewind();
			for (int x = 0; x < Configuration.TERRAIN_TILE_SIZE; x++) {
				for (int z = 0; z < Configuration.TERRAIN_TILE_SIZE; z++) {
					short s = data.get();
					if (s < 0)
						s = (short)-s;
					tileNoiseHeightMap[x][z] = (double)s * 15.0 / 256;
					data.position(data.position() + 3);
				}
			}
		} catch (FileNotFoundException e) {
			//TODO log
			//Use 0 as noise values
		}
		
		int heightMapSize = Configuration.TERRAIN_SIZE + 1;

		//Generate tiles
		tiles = new TerrainTile[Configuration.TERRAIN_SIZE][Configuration.TERRAIN_SIZE];
		for (int x = 0; x < Configuration.TERRAIN_SIZE; x++) {
			for (int z = 0; z < Configuration.TERRAIN_SIZE; z++) {
				tiles[x][z] = new TerrainTile(x, z);
			}
		}

		//Create the height map
		createHeightMap();
		
		//Calculate and assign maximum height
		double maxTileHeight = 1.0;
		for (int x = 0; x < Configuration.TERRAIN_SIZE; x++) {
			for (int z = 0; z < Configuration.TERRAIN_SIZE; z++) {
				double value = heightMap[x][z];
				int valueCount = 1;
				if (x < Configuration.TERRAIN_SIZE - 1) {
					value += heightMap[x + 1][z];
					valueCount++;
				}
				if (z < Configuration.TERRAIN_SIZE - 1) {
					value += heightMap[x][z + 1];
					valueCount++;
				}
				if (x < Configuration.TERRAIN_SIZE - 1 && z < Configuration.TERRAIN_SIZE - 1) {
					value += heightMap[x + 1][z + 1];
					valueCount++;
				}
				tiles[x][z].setHeight(value / valueCount);
				if (tiles[x][z].getHeight() > maxTileHeight) {
					maxTileHeight = tiles[x][z].getHeight();
				}
				
			}
		}
		
		//Create steepness map, moisture map and temperature map
		temperatureMap = new double[heightMapSize][heightMapSize];
		moistureMap = new double[heightMapSize][heightMapSize];
		steepnessMap = new double[heightMapSize][heightMapSize];
		for (int x = 0; x < heightMapSize; x++) {
			for (int z = 0; z < heightMapSize; z++) {
				//Generate steepness map
				double y = heightMap[x][z];
				int yCount = 0;
				if (x > 0) {
					steepnessMap[x][z] += Math.abs(heightMap[x - 1][z] - y);
					yCount++;
				}
				if (x < Configuration.TERRAIN_SIZE) {
					steepnessMap[x][z] += Math.abs(heightMap[x + 1][z] - y);
					yCount++;
				}
				if (z > 0) {
					steepnessMap[x][z] += Math.abs(heightMap[x][z - 1] - y);
					yCount++;
				}
				if (z < Configuration.TERRAIN_SIZE) {
					steepnessMap[x][z] += Math.abs(heightMap[x][z + 1] - y);
					yCount++;
				}
				steepnessMap[x][z] = steepnessMap[x][z] / yCount;
				
				//Create temperature
				temperatureMap[x][z] = Math.max(0.0, Math.min(1.0, (1.0 - (double)z / heightMapSize) - Math.max(0, (heightMap[x][z] / maxTileHeight / 2))));

				//Create moisture
				if (heightMap[x][z] <= 0.0) {
					//Water level or below. Moisture = 1.0
					moistureMap[x][z] = 1.0;
				} else {
					//Dry land. Set moisture based on eastwards point (x - 1)
					double deltaHeight = heightMap[x][z] - heightMap[x - 1][z];
					if (deltaHeight > 0.0) {
						moistureMap[x][z] = ((moistureMap[x - 1][z - 1] + moistureMap[x - 1][z] + moistureMap[x - 1][z + 1]) / 3) - (deltaHeight / maxTileHeight);
					} else {
						moistureMap[x][z] = ((moistureMap[x - 1][z - 1] + moistureMap[x - 1][z] + moistureMap[x - 1][z + 1]) / 3) - (0.7 / Configuration.TERRAIN_SIZE);
					}
					moistureMap[x][z] = Math.max(moistureMap[x][z], 0.0);
					
					if (heightMap[x - 2][z] < 0.0 || heightMap[x + 2][z] < 0.0
							|| heightMap[x][z - 2] < 0.0 || heightMap[x][z + 2] < 0.0) {
						moistureMap[x][z] = Math.max(0.75, moistureMap[x][z]);
					}
				}
			}			
		}
		
		//Assign climate
		for (int x = 0; x < Configuration.TERRAIN_SIZE; x++) {
			for (int z = 0; z < Configuration.TERRAIN_SIZE; z++) {
				//Assign temperature
				tiles[x][z].setTemperature((temperatureMap[x][z] + temperatureMap[x + 1][z]
						+ temperatureMap[x + 1][z + 1] + temperatureMap[x][z + 1]) / 4);
				
				//Assign moisture
				tiles[x][z].setMoisture((moistureMap[x][z] + moistureMap[x + 1][z]
						+ moistureMap[x + 1][z + 1] + moistureMap[x][z + 1]) / 4);
			}
		}
	}
	
	public double[][] getHeightMap() {
		return heightMap;
	}
	
	public double[][] getTemperatureMap() {
		return temperatureMap;
	}
	
	public double[][] getMoistureMap() {
		return moistureMap;
	}
	
	public double[][] getSteepnessMap() {
		return steepnessMap;
	}
	
	public TerrainTile[][] getTiles() {
		return tiles;
	}

	public double[][] getTileNoiseHeightMap() {
		return tileNoiseHeightMap;
	}
	
	/**
	 * Get the height at a given point in the terrain, as created by the world generator (no tile height applied)
	 * @param x Worldspace X coordinate
	 * @param z Worldspace Z coordinate
	 * @return
	 */
	public double getHeightAt(int x, int z) {
		int tileX = x / Configuration.TERRAIN_TILE_SIZE;
		int tileZ = z / Configuration.TERRAIN_TILE_SIZE;

		return getHeightAt(tileX, tileZ, x, z);
	}

	private double getHeightAt(int tileX, int tileZ, int x, int z) {
		double y11 = 0.0;
		double y21 = 0.0;
		double y22 = 0.0;
		double y12 = 0.0;
		if (tileX > -1 && tileZ > -1) {
			y11 = heightMap[tileX][tileZ];
		}
		if (tileX < Configuration.TERRAIN_SIZE && tileZ > -1) {
			y21 = heightMap[tileX + 1][tileZ];
		}
		if (tileX < Configuration.TERRAIN_SIZE && tileZ < Configuration.TERRAIN_SIZE) {
			y22 = heightMap[tileX + 1][tileZ + 1];
		}
		if (tileX > -1 && tileZ < Configuration.TERRAIN_SIZE) {
			y12 = heightMap[tileX][tileZ + 1];
		}
		
		return bilinearInterpolate(y11, y12, y21, y22, x % Configuration.TERRAIN_TILE_SIZE, z % Configuration.TERRAIN_TILE_SIZE) * 150;
	}

	private void createHeightMap() {
		int heightMapSize = Configuration.TERRAIN_SIZE + 1;
		heightMap = new double[heightMapSize][heightMapSize];
	
		PerlinNoise noise = new PerlinNoise(Configuration.TERRAIN_NOISE_PERSISTENCE,
				Configuration.TERRAIN_NOISE_FREQUENCY, Configuration.TERRAIN_NOISE_AMPLITUDE,
				Configuration.TERRAIN_NOISE_OCTAVES, Configuration.TERRAIN_NOISE_RANDOM_SEED);
		
		//Generate initial terrain height map
		for (int x = 0; x < heightMapSize; x++) {
			for (int z = 0; z < heightMapSize; z++) {
				heightMap[x][z] = Math.sin(((double)x / Configuration.TERRAIN_SIZE)* Math.PI) * Math.sin(((double)z / Configuration.TERRAIN_SIZE)* Math.PI) * noise.getHeight(x, z)
						+ Math.sin(((double)x / Configuration.TERRAIN_SIZE)* Math.PI) * Math.sin(((double)z / Configuration.TERRAIN_SIZE)* Math.PI) * 40
						- 2;
			}
		}
	}
	
	private double bilinearInterpolate(double q11, double q12, double q21, double q22, double x, double y) {
		int heightMapSize = Configuration.TERRAIN_TILE_SIZE + 1;
		double r1 = ((heightMapSize - x)/(heightMapSize - 0)) * q11 + ((x - 0)/(heightMapSize - 0)) * q21;
		double r2 = ((heightMapSize - x)/(heightMapSize - 0)) * q12 + ((x - 0)/(heightMapSize - 0)) * q22;
		
		double rc = (heightMapSize - y)/(heightMapSize - 0) * r1 + (heightMapSize - y)/(heightMapSize - 0) * r2;
		
		double x1 = 0;
		double x2 = Configuration.TERRAIN_TILE_SIZE ;
		double y1 = 0;
		double y2 = Configuration.TERRAIN_TILE_SIZE ;
		
		rc = ((((x2 - x) * (y2 - y)) / ((x2 - x1) * (y2 - y1))) * q11)
				+ ((((x - x1) * (y2 - y)) / ((x2 - x1) * (y2 - y1))) * q21)
				+ ((((x2 - x) * (y - y1)) / ((x2 - x1) * (y2 - y1))) * q12)
				+ ((((x - x1) * (y - y1)) / ((x2 - x1) * (y2 - y1))) * q22);
		return rc;
	}
	
}
