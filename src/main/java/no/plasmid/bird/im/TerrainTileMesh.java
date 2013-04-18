package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;
import no.plasmid.bird.util.PerlinNoise;

public class TerrainTileMesh {

	private static PerlinNoise noise = new PerlinNoise(Configuration.TERRAIN_TILE_NOISE_PERSISTENCE,
			Configuration.TERRAIN_TILE_NOISE_FREQUENCY, Configuration.TERRAIN_TILE_NOISE_AMPLITUDE,
			Configuration.TERRAIN_TILE_NOISE_OCTAVES, Configuration.TERRAIN_TILE_NOISE_RANDOM_SEED);

	private static double[][] heightMap = new double[Configuration.TERRAIN_TILE_SIZE + 1][Configuration.TERRAIN_TILE_SIZE + 1];
	private static Vertex3d[][] normalMap = new Vertex3d[Configuration.TERRAIN_TILE_SIZE + 1][Configuration.TERRAIN_TILE_SIZE + 1];

	static {
		int heightMapSize = Configuration.TERRAIN_TILE_SIZE + 1;
		for (int x = 0; x < heightMapSize; x++) {
			for (int z = 0; z < heightMapSize; z++) {
				normalMap[x][z] = new Vertex3d();
			}
		}
	}
	
	private static boolean stitchNegX = false;
	private static boolean stitchPosX = false;
	private static boolean stitchNegZ = false;
	private static boolean stitchPosZ = false;
	
	private Vertex3d[][] strips;	//Triangle strips vertices
	private Vertex3d[][] normals;	//Triangle strips normals
	private int[] vertexCounts;
	
	/**
	 * Generate a mesh that can be rendered.
	 * @param terrain
	 * @param tileX
	 * @param tileZ
	 * @param divisionSize
	 */
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

		TerrainTile[][] tiles = terrain.getTiles();
		if (tileX > 0 && tiles[tileX - 1][tileZ] != null && tiles[tileX - 1][tileZ].isReadyForDrawing() && tiles[tileX - 1][tileZ].getDivisionSize() < divisionSize) {
			stitchNegX = true;
		} else {
			stitchNegX = false;
		}
		if (tileX < Configuration.TERRAIN_SIZE - 1 && tiles[tileX + 1][tileZ] != null && tiles[tileX + 1][tileZ].isReadyForDrawing() && tiles[tileX + 1][tileZ].getDivisionSize() < divisionSize) {
			stitchPosX = true;
		} else {
			stitchPosX = false;
		}
		if (tileZ > 0 && tiles[tileX][tileZ - 1] != null && tiles[tileX][tileZ - 1].isReadyForDrawing() && tiles[tileX][tileZ - 1].getDivisionSize() < divisionSize) {
			stitchNegZ = true;
		} else {
			stitchNegZ = false;
		}
		if (tileZ < Configuration.TERRAIN_SIZE - 1 && tiles[tileX][tileZ + 1] != null && tiles[tileX][tileZ + 1].isReadyForDrawing() && tiles[tileX][tileZ + 1].getDivisionSize() < divisionSize) {
			stitchPosZ = true;
		} else {
			stitchPosZ = false;
		}

		int heightMapSize = Configuration.TERRAIN_TILE_SIZE + 1;
		int heightMapDivisionSize = divisionSize / 2;
		//Generate heightmap
		if (divisionSize == 1) {
			heightMapDivisionSize = 1;
		}
		for (int x = 0; x < heightMapSize; x += heightMapDivisionSize) {
			for (int z = 0; z < heightMapSize; z += heightMapDivisionSize) {
				heightMap[x][z] = bilinearInterpolate(y11, y12, y21, y22, x, z, heightMapDivisionSize) * 150 + noise.getHeight(xOffsetStart + x, zOffsetStart + z) * 15;
			}
		}

		//Generate normals
		for (int x = 0; x < heightMapSize; x += heightMapDivisionSize) {
			for (int z = 0; z < heightMapSize; z += heightMapDivisionSize) {
				createNormalForPoint(x, z, heightMapDivisionSize);
			}
		}
		
		//Generate triangle strips
		strips = new Vertex3d[detail][];
		normals = new Vertex3d[detail][];
		vertexCounts = new int[detail];
		for (int x = 0; x < detail; x++) {
			int vertexCount = 0;

			if (stitchNegX && x == 0) {
				//Perform stitching in the negative X direction
				strips[x] = new Vertex3d[(detail + 1) * 7];
				normals[x] = new Vertex3d[(detail + 1) * 7];
				
				for (int z = detail - 1; z > -1; z--) {
					if (stitchNegZ && z == 0) {
						//Do stitch in negative Z direction
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching
						
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching

						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching

						normals[x][vertexCount] = new Vertex3d(normalMap[(int)((x + 0.5) * divisionSize)][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((int)((x + 0.5) * divisionSize), z * divisionSize, xOffsetStart, zOffsetStart);

						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][(int)((z + 0.5) * divisionSize)]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching
					} else if (stitchPosZ && z == detail - 1) {
						//Do stitch in positive Z direction
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching

						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching

						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching

						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][(int)((z + 0.5) * divisionSize)]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[(int)((x + 0.5) * divisionSize)][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((int)((x + 0.5) * divisionSize), (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching

						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching
					} else {
						//Do normal stitching
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);	//For correct stitching
						
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][(int)((z + 0.5) * divisionSize)]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);

						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
					}
				}
			} else if (stitchPosX && x == detail - 1) {
				//Perform stitching in the positive X direction
				normals[x] = new Vertex3d[(detail + 1) * 6];
				strips[x] = new Vertex3d[(detail + 1) * 6];
				
				for (int z = 0; z < detail; z++) {
					if (stitchNegZ && z == 0) {
						//Do stitch in negative Z direction
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);

						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);

						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(int)((z + 0.5) * divisionSize)]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);

						normals[x][vertexCount] = new Vertex3d(normalMap[(int)((x + 0.5) * divisionSize)][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((int)((x + 0.5) * divisionSize), z * divisionSize, xOffsetStart, zOffsetStart);

						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);

						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
					} else if (stitchPosZ && z == detail - 1) {
						//Do stitch in positive Z direction
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);

						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);

						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[(int)((x + 0.5) * divisionSize)][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((int)((x + 0.5) * divisionSize), (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(int)((z + 0.5) * divisionSize)]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);

						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
					} else {
						//Do normal stitching
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][z * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
	
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(int)((z + 0.5) * divisionSize)]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);
	
						normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(z + 1) * divisionSize]);
						strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
					}
				}
			} else {
				strips[x] = new Vertex3d[(detail + 1) * 2 + 9];
				normals[x] = new Vertex3d[(detail + 1) * 2 + 9];

				//Z = 0 line
				if (stitchNegZ) {
					//Do stitch in negative Z direction
					normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][0]);
					strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, 0, xOffsetStart, zOffsetStart);

					normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, divisionSize, xOffsetStart, zOffsetStart);

					normals[x][vertexCount] = new Vertex3d(normalMap[(int)((x + 0.5) * divisionSize)][0]);
					strips[x][vertexCount++] = createVertexForPoint((int)((x + 0.5) * divisionSize), 0, xOffsetStart, zOffsetStart);
				
					normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, divisionSize, xOffsetStart, zOffsetStart);

					normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][0]);
					strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, 0, xOffsetStart, zOffsetStart);

					normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, divisionSize, xOffsetStart, zOffsetStart);	//To set up for the main strips
				} else {
					//No stitching needed
					normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][0]);
					strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, 0, xOffsetStart, zOffsetStart);
					normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][0]);
					strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, 0, xOffsetStart, zOffsetStart);
				}

				//Center
				for (int z = 1; z < detail; z++) {
					normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][z * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
					normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][z * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
				}
				
				//Z = detail line
				if (stitchPosZ) {
					//Do stitch in positive Z direction
					normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(detail - 1) * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (detail - 1) * divisionSize, xOffsetStart, zOffsetStart);	//To find the starting point from the main strips
					normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(detail - 1) * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (detail - 1) * divisionSize, xOffsetStart, zOffsetStart);
					normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][detail * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, detail * divisionSize, xOffsetStart, zOffsetStart);
					normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][(detail - 1) * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, (detail - 1) * divisionSize, xOffsetStart, zOffsetStart);

					normals[x][vertexCount] = new Vertex3d(normalMap[(int)((x + 0.5) * divisionSize)][detail * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint((int)((x + 0.5) * divisionSize), detail * divisionSize, xOffsetStart, zOffsetStart);

					normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][(detail - 1) * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, (detail - 1) * divisionSize, xOffsetStart, zOffsetStart);
					
					normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][detail * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, detail * divisionSize, xOffsetStart, zOffsetStart);
				} else {
					//No stitching needed
					normals[x][vertexCount] = new Vertex3d(normalMap[(x + 1) * divisionSize][detail * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint((x + 1) * divisionSize, detail * divisionSize, xOffsetStart, zOffsetStart);
					normals[x][vertexCount] = new Vertex3d(normalMap[x * divisionSize][detail * divisionSize]);
					strips[x][vertexCount++] = createVertexForPoint(x * divisionSize, detail * divisionSize, xOffsetStart, zOffsetStart);
				}
			}
			
			vertexCounts[x] = vertexCount;
		}
	}
	
	public Vertex3d[][] getStrips() {
		return strips;
	}
	
	public Vertex3d[][] getNormals() {
		return normals;
	}
	
	public int[] getVertexCounts() {
		return vertexCounts;
	}
	
	/**
	 * Create a vertex for a point on the map for the current tile
	 * @param x The x position on the current tile (0 - heightmap size)
	 * @param z The z position on the current tile (0 - heightmap size)
	 * @param xOffsetStart The offset between the tile space coordinates and the world space coordinates
	 * @param zOffsetStart The offset between the tile space coordinates and the world space coordinates
	 * @return
	 */
	private Vertex3d createVertexForPoint(int x, int z, int xOffsetStart, int zOffsetStart) {
		return new Vertex3d(new double[]{x + xOffsetStart, heightMap[x][z], z + zOffsetStart});
	}
	
	/**
	 * Create a normal for a point. Will base the normal on the delta-y value compared to 2-4 neighbouring points
	 * @param x
	 * @param z
	 */
	private void createNormalForPoint(int x, int z, int divisionSize) {
		double y1 = heightMap[x][z];

		int deltaXPos = divisionSize;
		int deltaZPos = divisionSize;
		
		Vertex3d v1;
		Vertex3d v2;
		Vertex3d v3;
		Vertex3d v4;
		
		if (x != 0) {
			//Create normal with x - deltaXPos
			v1 = new Vertex3d(new double[]{-deltaXPos, y1 - heightMap[x - deltaXPos][z], 0.0});
		} else {
			//Use placeholder value
			v1 = new Vertex3d(new double[]{-deltaXPos, heightMap[x + deltaXPos][z] - y1, 0.0});
		}
		if (x < Configuration.TERRAIN_TILE_SIZE) {
			//Create normal with x + deltaXPos
			v2 = new Vertex3d(new double[]{deltaXPos, y1 - heightMap[x + deltaXPos][z], 0.0});
		} else {
			//Use placeholder value
			v2 = new Vertex3d(new double[]{deltaXPos, heightMap[x - deltaXPos][z] - y1, 0.0});
		}
		if (z != 0) {
			//Create normal with z - deltaZPos
			v3 = new Vertex3d(new double[]{0.0, y1 - heightMap[x][z - deltaZPos], -deltaZPos});
		} else {
			//Use placeholder value
			v3 = new Vertex3d(new double[]{0.0, heightMap[x][z + deltaZPos] - y1, -deltaZPos});
		}
		if (z < Configuration.TERRAIN_TILE_SIZE) {
			//Create normal with z + deltaZPos
			v4 = new Vertex3d(new double[]{0.0, y1 - heightMap[x][z + deltaZPos], deltaZPos});
		} else {
			//Use placeholder value
			v4 = new Vertex3d(new double[]{0.0,heightMap[x][z - deltaZPos] - y1, deltaZPos});
		}
		
		Vertex3d v5 = Vertex3d.crossProduct(v1, v4);
		Vertex3d v6 = Vertex3d.crossProduct(v2, v3);
		
		normalMap[x][z].values[0] = v5.values[0] + v6.values[0];
		normalMap[x][z].values[1] = v5.values[1] + v6.values[1];
		normalMap[x][z].values[2] = v5.values[2] + v6.values[2];
		
		normalMap[x][z].normalize();
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
