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
	private boolean recreateMeshRequested;
	
	/**
	 * @param tileX X position relative to other tiles
	 * @param tileZ Z position relative to other tiles
	 */
	public TerrainTile(int tileX, int tileZ) {
		this.tileX = tileX;
		this.tileZ = tileZ;
		
		readyForDrawing = false;
		recreateMeshRequested = false;
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

		if (this.detail != detail) {
			//Alert nearby tiles that they need to create new mesh, so they can stitch properly
			TerrainTile[][] tiles = terrain.getTiles();
			if (tileX != 0) {
				tiles[tileX - 1][tileZ].requestMeshRecreation();
			}
			if (tileX != Configuration.TERRAIN_SIZE - 1) {
				tiles[tileX + 1][tileZ].requestMeshRecreation();
			}
			if (tileZ != 0) {
				tiles[tileX][tileZ - 1].requestMeshRecreation();
			}
			if (tileZ != Configuration.TERRAIN_SIZE - 1) {
				tiles[tileX][tileZ + 1].requestMeshRecreation();
			}
			
			//Save the new detail and division size
			this.detail = detail;
			this.divisionSize = divisionSize;
		}
		
		//Generate mesh
		mesh.generateMeshFromHeightMap(terrain, tileX, tileZ, divisionSize);

		this.mesh = mesh;
		recreateMeshRequested = false;
		readyForDrawing = true;
	}
		
	public TerrainTileMesh dropMesh() {
		readyForDrawing = false;
		recreateMeshRequested = false;
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
		if (this.detail != detail) {
			//Alert nearby tiles that they need to create new mesh, so they can stitch properly
			TerrainTile[][] tiles = terrain.getTiles();
			if (tileX != 0) {
				tiles[tileX - 1][tileZ].requestMeshRecreation();
			}
			if (tileX != Configuration.TERRAIN_SIZE - 1) {
				tiles[tileX + 1][tileZ].requestMeshRecreation();
			}
			if (tileZ != 0) {
				tiles[tileX][tileZ - 1].requestMeshRecreation();
			}
			if (tileZ != Configuration.TERRAIN_SIZE - 1) {
				tiles[tileX][tileZ + 1].requestMeshRecreation();
			}
			
			//Save the new detail and division size
			this.detail = detail;
			this.divisionSize = divisionSize;
		}
		readyForDrawing = true;
		recreateMeshRequested = false;
		return rc;
	}
	
	public boolean isRecreateMeshRequested() {
		return recreateMeshRequested;
	}
	
	public void requestMeshRecreation() {
		recreateMeshRequested = true;
	}
	
}
