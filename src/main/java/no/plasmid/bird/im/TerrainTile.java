package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;

public class TerrainTile {

	private TerrainTileMesh mesh;
	
	/**
	 * 
	 * @param tileX X position relative to other tiles
	 * @param tileZ Z position relative to other tiles
	 * @param y1 height of corner
	 * @param y2 height of corner
	 * @param y3 height of corner
	 * @param y4 height of corner
	 */
	public TerrainTile(int tileX, int tileZ, double y11, double y21, double y22, double y12) {
		//Generate heightmap
		int heightMapSize = Configuration.TERRAIN_TILE_SIZE + 1;
		double[][] heightMap = new double[heightMapSize][heightMapSize];
		for (int x = 0; x < heightMapSize; x++) {
			for (int z = 0; z < heightMapSize; z++) {
				heightMap[x][z] = bilinearInterpolate(y11, y12, y21, y22, x, z);
			}
		}
		
		//Calculate offsets
		int xOffsetStart = tileX * Configuration.TERRAIN_TILE_SIZE;
		int zOffsetStart = tileZ * Configuration.TERRAIN_TILE_SIZE;

		//Generate mesh
		mesh = new TerrainTileMesh();
		mesh.generateMeshFromHeightMap(heightMap, xOffsetStart, zOffsetStart);
	}

	public TerrainTileMesh getMesh() {
		return mesh;
	}

	private double bilinearInterpolate(double q11, double q12, double q21, double q22, double x, double y) {
		int heightMapSize = Configuration.TERRAIN_TILE_SIZE;
		double r1 = ((heightMapSize - x)/(heightMapSize - 0)) * q11 + ((x - 0)/(heightMapSize - 0)) * q21;
		double r2 = ((heightMapSize - x)/(heightMapSize - 0)) * q12 + ((x - 0)/(heightMapSize - 0)) * q22;
		
		double rc = (heightMapSize - y)/(heightMapSize - 0) * r1 + (heightMapSize - y)/(heightMapSize - 0) * r2;
		
		double x1 = 0;
		double x2 = Configuration.TERRAIN_TILE_SIZE;
		double y1 = 0;
		double y2 = Configuration.TERRAIN_TILE_SIZE;
		
		rc = ((((x2 - x) * (y2 - y)) / ((x2 - x1) * (y2 - y1))) * q11)
				+ ((((x - x1) * (y2 - y)) / ((x2 - x1) * (y2 - y1))) * q21)
				+ ((((x2 - x) * (y - y1)) / ((x2 - x1) * (y2 - y1))) * q12)
				+ ((((x - x1) * (y - y1)) / ((x2 - x1) * (y2 - y1))) * q22);
		return rc;
	}
	
}
