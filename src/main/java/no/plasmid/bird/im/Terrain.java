package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;
import no.plasmid.bird.util.PerlinNoise;

public class Terrain {

	private double[][] heightMap;
	private TerrainTile[][] tiles;
	
	public Terrain() {
		tiles = new TerrainTile[Configuration.TERRAIN_SIZE][Configuration.TERRAIN_SIZE];
		
		PerlinNoise noise = new PerlinNoise(Configuration.TERRAIN_NOISE_PERSISTENCE,
				Configuration.TERRAIN_NOISE_FREQUENCY, Configuration.TERRAIN_NOISE_AMPLITUDE,
				Configuration.TERRAIN_NOISE_OCTAVES, Configuration.TERRAIN_NOISE_RANDOM_SEED);
		
		//Generate terrain heightmap
		int heightMapSize = Configuration.TERRAIN_SIZE + 1;
		heightMap = new double[heightMapSize][heightMapSize];
		for (int x = 0; x < heightMapSize; x++) {
			for (int z = 0; z < heightMapSize; z++) {
				heightMap[x][z] = Math.sin(((double)x / Configuration.TERRAIN_SIZE)* Math.PI) * Math.sin(((double)z / Configuration.TERRAIN_SIZE)* Math.PI) * noise.getHeight(x, z)
						+ Math.sin(((double)x / Configuration.TERRAIN_SIZE)* Math.PI) * Math.sin(((double)z / Configuration.TERRAIN_SIZE)* Math.PI) * 30;
			}
		}
	}
	
	public double[][] getHeightMap() {
		return heightMap;
	}
	
	public TerrainTile[][] getTiles() {
		return tiles;
	}
	
}
