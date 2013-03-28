package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;
import no.plasmid.bird.util.PerlinNoise;

public class TerrainTileMesh {

	private static PerlinNoise noise = new PerlinNoise(Configuration.TERRAIN_TILE_NOISE_PERSISTENCE,
			Configuration.TERRAIN_TILE_NOISE_FREQUENCY, Configuration.TERRAIN_TILE_NOISE_AMPLITUDE,
			Configuration.TERRAIN_TILE_NOISE_OCTAVES, Configuration.TERRAIN_TILE_NOISE_RANDOM_SEED);

	private static double[][] heightMap = new double[Configuration.TERRAIN_TILE_SIZE + 1][Configuration.TERRAIN_TILE_SIZE + 1];

	private Vertex3d[][] strips;	//Triangle strips vertices
	private int[] vertexCounts;
	
	public TerrainTileMesh() {
	}
		
	public void generateMeshFromHeightMap(Terrain terrain, int tileX, int tileZ, int divisionSize) {
		int detail = Configuration.TERRAIN_TILE_SIZE / divisionSize;

		//Find the height of the corners
		double[][] terrainHeightMap = terrain.getHeightMap();
		double y11 = terrainHeightMap[tileX][tileZ];
		double y21 = terrainHeightMap[tileX + 1][tileZ];
		double y22 = terrainHeightMap[tileX + 1][tileZ + 1];
		double y12 = terrainHeightMap[tileX][tileZ + 1];
		
		//Calculate offsets
		int xOffsetStart = tileX * Configuration.TERRAIN_TILE_SIZE;
		int zOffsetStart = tileZ * Configuration.TERRAIN_TILE_SIZE;

		//Generate heightmap
		int heightMapSize = Configuration.TERRAIN_TILE_SIZE + 1;
		for (int x = 0; x < heightMapSize; x += divisionSize) {
			for (int z = 0; z < heightMapSize; z += divisionSize) {
				heightMap[x][z] = bilinearInterpolate(y11, y12, y21, y22, x, z, divisionSize) * 150 + noise.getHeight(xOffsetStart + x, zOffsetStart + z) * 15;
			}
		}
		
		//Generate triangle strips
		strips = new Vertex3d[detail][];
		vertexCounts = new int[detail];
		for (int x = 0; x < detail; x++) {
			strips[x] = new Vertex3d[(detail + 1) * 2];
			vertexCounts[x] = (detail + 1) * 2;
			int vertexCount = 0;
			strips[x][vertexCount++] = new Vertex3d(new double[]{x * divisionSize + xOffsetStart, (heightMap[x * divisionSize][0]), 0 * divisionSize + zOffsetStart});

			for (int z = 0; z < detail; z++) {
				strips[x][vertexCount++] = new Vertex3d(new double[]{(x + 1) * divisionSize + xOffsetStart, (heightMap[(x + 1)* divisionSize][z * divisionSize]), z * divisionSize + zOffsetStart});
				strips[x][vertexCount++] = new Vertex3d(new double[]{x * divisionSize + xOffsetStart, (heightMap[x* divisionSize][(z + 1) * divisionSize]), (z + 1) * divisionSize + zOffsetStart});
			}
			
			strips[x][vertexCount++] = new Vertex3d(new double[]{(x + 1) * divisionSize + xOffsetStart, (heightMap[(x + 1) * divisionSize][detail * divisionSize]), divisionSize * detail + zOffsetStart});
		}
	}
	
	public Vertex3d[][] getStrips() {
		return strips;
	}
	
	public int[] getVertexCounts() {
		return vertexCounts;
	}
	
	private double bilinearInterpolate(double q11, double q12, double q21, double q22, double x, double y, int divisionSize) {
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
