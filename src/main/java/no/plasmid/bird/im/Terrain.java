package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;
import no.plasmid.bird.util.PerlinNoise;

public class Terrain {

	private double[][] heightMap;
	private TerrainTile[][] tiles;
	
	public Terrain() {
		PerlinNoise noise = new PerlinNoise(Configuration.TERRAIN_NOISE_PERSISTENCE,
				Configuration.TERRAIN_NOISE_FREQUENCY, Configuration.TERRAIN_NOISE_AMPLITUDE,
				Configuration.TERRAIN_NOISE_OCTAVES, Configuration.TERRAIN_NOISE_RANDOM_SEED);
		
		//Generate terrain heightmap
		int heightMapSize = Configuration.TERRAIN_SIZE + 1;
		heightMap = new double[heightMapSize][heightMapSize];
		for (int x = 0; x < heightMapSize; x++) {
			for (int z = 0; z < heightMapSize; z++) {
				heightMap[x][z] = Math.sin(((double)x / Configuration.TERRAIN_SIZE)* Math.PI) * Math.sin(((double)z / Configuration.TERRAIN_SIZE)* Math.PI) * noise.getHeight(x, z)
						+ Math.sin(((double)x / Configuration.TERRAIN_SIZE)* Math.PI) * Math.sin(((double)z / Configuration.TERRAIN_SIZE)* Math.PI) * 40;
			}
		}
		
		//Generate tiles
		tiles = new TerrainTile[Configuration.TERRAIN_SIZE][Configuration.TERRAIN_SIZE];
		for (int x = 0; x < Configuration.TERRAIN_SIZE; x++) {
			for (int z = 0; z < Configuration.TERRAIN_SIZE; z++) {
				tiles[x][z] = new TerrainTile(x, z, heightMap[x][z], heightMap[x + 1][z], heightMap[x + 1][z + 1], heightMap[x][z + 1]);
			}
		}
	}
	
	public TerrainTile[][] getTiles() {
		return tiles;
	}
	
}
