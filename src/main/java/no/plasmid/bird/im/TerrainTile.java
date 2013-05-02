package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;

public class TerrainTile {

	public final String SYNCH_PARAM = "Hei";
	
	private int tileX;	//Relative to other tiles on the whole terrain
	private int tileZ;	//Relative to other tiles on the whole terrain
	
	private float[] idColor;
	
	//Climate values
	private double temperature;
	private double moisture;
	
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
		
		//Create random ID color, for rendering in "clown mode"
		idColor = new float[]{(float)Math.random(), (float)Math.random(), (float)Math.random(), 1.0f};
	}

	public int getTileX() {
		return tileX;
	}

	public int getTileZ() {
		return tileZ;
	}

	public float[] getIdColor() {
		return idColor;
	}
	
	public double getTemperature() {
		return temperature;
	}
	
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}
	
	public double getMoisture() {
		return moisture;
	}
	
	public void setMoisture(double moisture) {
		this.moisture = moisture;
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
