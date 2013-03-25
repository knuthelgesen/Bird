package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;

public class TerrainTileMesh {

	private Vertex3d[][] strips;	//Triangle strips vertices
	
	public TerrainTileMesh() {
	}
	
	public void generateMeshFromHeightMap(double[][] heightMap, int xOffsetStart, int zOffsetStart) {
		//Generate triangle strips
		strips = new Vertex3d[Configuration.TERRAIN_TILE_SIZE][];
		for (int x = 0; x < Configuration.TERRAIN_TILE_SIZE; x++) {
			strips[x] = new Vertex3d[(Configuration.TERRAIN_TILE_SIZE + 1) * 2];
			int vertexCount = 0;
			strips[x][vertexCount++] = new Vertex3d(new double[]{(x + xOffsetStart) * Configuration.HOROZONTAL_SCALE, (heightMap[x][0]) * Configuration.VERTICAL_SCALE, (0 + zOffsetStart) * Configuration.HOROZONTAL_SCALE});
			for (int z = 0; z < Configuration.TERRAIN_TILE_SIZE; z++) {
				strips[x][vertexCount++] = new Vertex3d(new double[]{(x + 1 + xOffsetStart) * Configuration.HOROZONTAL_SCALE, (heightMap[x + 1][z]) * Configuration.VERTICAL_SCALE, (z + zOffsetStart) * Configuration.HOROZONTAL_SCALE});
				strips[x][vertexCount++] = new Vertex3d(new double[]{(x + xOffsetStart) * Configuration.HOROZONTAL_SCALE, (heightMap[x][z + 1]) * Configuration.VERTICAL_SCALE, (z + 1 + zOffsetStart) * Configuration.HOROZONTAL_SCALE});
			}
			strips[x][vertexCount++] = new Vertex3d(new double[]{(x + 1 + xOffsetStart) * Configuration.HOROZONTAL_SCALE, (heightMap[x + 1][Configuration.TERRAIN_TILE_SIZE]) * Configuration.VERTICAL_SCALE, (Configuration.TERRAIN_TILE_SIZE + zOffsetStart) * Configuration.HOROZONTAL_SCALE});
		}
	}
	
	public Vertex3d[][] getStrips() {
		return strips;
	}
	
}
