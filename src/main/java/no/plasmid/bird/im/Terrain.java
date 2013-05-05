package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;
import no.plasmid.bird.util.PerlinNoise;

public class Terrain {

	private double[][] heightMap;
	private TerrainTile[][] tiles;
	
	public Terrain() {
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
		double maxHeight = 1.0;
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
				if (tiles[x][z].getHeight() > maxHeight) {
					maxHeight = tiles[x][z].getHeight();
				}
				
			}
		}
		
		//Assign climate
		for (int x = 0; x < Configuration.TERRAIN_SIZE; x++) {
			for (int z = 0; z < Configuration.TERRAIN_SIZE; z++) {
				//Assign temperature
				tiles[x][z].setTemperature(Math.max(0.0, Math.min(1.0, (1.0 - (double)z / Configuration.TERRAIN_SIZE) - Math.max(0, (tiles[x][z].getHeight() / maxHeight / 2)))));
				
				//Assign moisture
				if (tiles[x][z].getHeight() < 0.0) {
					//Water. Always has moisture 1.0
					tiles[x][z].setMoisture(1.0);
				} else {
					//Set moisture based on eastwards tile (x - 1)
					double deltaMoisture = tiles[x][z].getHeight() / maxHeight;
					tiles[x][z].setMoisture(Math.min(tiles[x - 1][z].getMoisture() - (1.0 / Configuration.TERRAIN_SIZE), 1.0 - deltaMoisture));
					tiles[x][z].setMoisture(Math.min(1.0, tiles[x][z].getMoisture()
							+ tiles[x - 1][z - 1].getMoisture() / Configuration.TERRAIN_SIZE
							+ tiles[x - 1][z + 1].getMoisture() / Configuration.TERRAIN_SIZE
							));
					tiles[x][z].setMoisture(Math.max(tiles[x][z].getMoisture(), 0.0));
				}
			}
		}
	}
	
	public double[][] getHeightMap() {
		return heightMap;
	}
	
	public TerrainTile[][] getTiles() {
		return tiles;
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

	public double getHeightAt(int tileX, int tileZ, int x, int z) {
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
