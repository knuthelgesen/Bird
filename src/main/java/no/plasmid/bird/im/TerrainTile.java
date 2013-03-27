package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;

public class TerrainTile {

	public final String SYNCH_PARAM = "Hei";
	
	private int tileX;	//Relative to other tiles on the whole terrain
	private int tileZ;	//Relative to other tiles on the whole terrain
	
//	private static PerlinNoise noise = new PerlinNoise(Configuration.TERRAIN_TILE_NOISE_PERSISTENCE,
//			Configuration.TERRAIN_TILE_NOISE_FREQUENCY, Configuration.TERRAIN_TILE_NOISE_AMPLITUDE,
//			Configuration.TERRAIN_TILE_NOISE_OCTAVES, Configuration.TERRAIN_TILE_NOISE_RANDOM_SEED);
//	
	private boolean readyForDrawing;
	private int detail = 1;
	private int divisionSize = Configuration.TERRAIN_TILE_SIZE;
	private TerrainTileMesh mesh;
	
	/**
	 * @param tileX X position relative to other tiles
	 * @param tileZ Z position relative to other tiles
	 */
	public TerrainTile(int tileX, int tileZ) {
		this.tileX = tileX;
		this.tileZ = tileZ;
		
		readyForDrawing = false;
	}

	public int getTileX() {
		return tileX;
	}

	public int getTileZ() {
		return tileZ;
	}

	public boolean isReadyForDrawing() {
		return readyForDrawing;
	}
	
	public int getDetail() {
		return detail;
	}
	
	public int getDivisionSize() {
		return divisionSize;
	}
	
	public TerrainTileMesh getMesh() {
		return mesh;
	}
	
	/**
	 * @param terrain The terrain
	 * @param divisionSize Indicates the level of detail. 1 is highest detail, 256 is lowest.
	 */
	public void generateMesh(Terrain terrain, int divisionSize, TerrainTileMesh mesh) {
		int detail = Configuration.TERRAIN_TILE_SIZE / divisionSize;

		this.detail = detail;
		this.divisionSize = divisionSize;
		
		//Generate mesh
		mesh.generateMeshFromHeightMap(terrain, tileX, tileZ, divisionSize);

		this.mesh = mesh;
		readyForDrawing = true;
	}
		
	public TerrainTileMesh dropMesh() {
		readyForDrawing = false;
		TerrainTileMesh rc = mesh;
		mesh = null;
		return rc;
	}
	
	public TerrainTileMesh replaceMesh(Terrain terrain, int divisionSize, TerrainTileMesh mesh) {
		int detail = Configuration.TERRAIN_TILE_SIZE / divisionSize;

		//Generate mesh
		mesh.generateMeshFromHeightMap(terrain, tileX, tileZ, divisionSize);

		readyForDrawing = false;
		TerrainTileMesh rc = this.mesh;
		this.mesh = mesh;
		this.detail = detail;
		this.divisionSize = divisionSize;
		readyForDrawing = true;
		return rc;
	}
	
}
