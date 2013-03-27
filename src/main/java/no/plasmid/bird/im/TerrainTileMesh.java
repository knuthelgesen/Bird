package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;

public class TerrainTileMesh {

	private Vertex3d[][] strips;	//Triangle strips vertices
	
	public TerrainTileMesh() {
	}
		
	public void generateMeshFromHeightMap(double[][] heightMap, int divisionSize, int xOffsetStart, int zOffsetStart) {
		int detail = Configuration.TERRAIN_TILE_SIZE / divisionSize;
		
		//Generate triangle strips
		strips = new Vertex3d[detail][];
		for (int x = 0; x < detail; x++) {
			strips[x] = new Vertex3d[(detail + 1) * 2];
			int vertexCount = 0;
			strips[x][vertexCount++] = new Vertex3d(new double[]{x * divisionSize + xOffsetStart, (heightMap[x][0]), 0 * divisionSize + zOffsetStart});

			for (int z = 0; z < detail; z++) {
				strips[x][vertexCount++] = new Vertex3d(new double[]{(x + 1) * divisionSize + xOffsetStart, (heightMap[x + 1][z]), z * divisionSize + zOffsetStart});
				strips[x][vertexCount++] = new Vertex3d(new double[]{x * divisionSize + xOffsetStart, (heightMap[x][z + 1]), (z + 1) * divisionSize + zOffsetStart});
			}
			
			strips[x][vertexCount++] = new Vertex3d(new double[]{(x + 1) * divisionSize + xOffsetStart, (heightMap[x + 1][detail]), divisionSize * detail + zOffsetStart});
		}
	}
	
	public Vertex3d[][] getStrips() {
		return strips;
	}
	
}
