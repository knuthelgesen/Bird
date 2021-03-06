package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;
import no.plasmid.bird.util.PerlinNoise;

public class TerrainTileMesh {

	private static PerlinNoise noise = new PerlinNoise(Configuration.TERRAIN_TILE_NOISE_PERSISTENCE,
			Configuration.TERRAIN_TILE_NOISE_FREQUENCY, Configuration.TERRAIN_TILE_NOISE_AMPLITUDE,
			Configuration.TERRAIN_TILE_NOISE_OCTAVES, Configuration.TERRAIN_TILE_NOISE_RANDOM_SEED);

	private Terrain terrain;
	
	private int tileX;
	private int tileZ;
	
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
	private Vertex3d[][] textureCoords;	//Texture coords strips vertices
	private int[] vertexCounts;
	
	/**
	 * Generate a mesh that can be rendered.
	 * @param terrain
	 * @param tileX
	 * @param tileZ
	 * @param divisionSize
	 */
	public void generateMeshFromHeightMap(Terrain terrain, int tileX, int tileZ, int divisionSize) {
		this.terrain = terrain;
		
		this.tileX = tileX;
		this.tileZ = tileZ;
		
		int detail = Configuration.TERRAIN_TILE_SIZE / divisionSize;

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
				heightMap[x][z] = generateHeightForPoint(x + xOffsetStart, z + zOffsetStart);
			}
		}
		for (int x = 0; x < heightMapSize; x += heightMapDivisionSize) {
			for (int z = 0; z < heightMapSize; z += heightMapDivisionSize) {
				createNormalForPoint(x, z, xOffsetStart, zOffsetStart, heightMapDivisionSize);
			}
		}
		
		//Generate triangle strips
		strips = new Vertex3d[detail][];
		normals = new Vertex3d[detail][];
		textureCoords = new Vertex3d[detail][];
		vertexCounts = new int[detail];
		for (int x = 0; x < detail; x++) {
			int vertexCount = 0;

			if (stitchNegX && x == 0) {
				//Perform stitching in the negative X direction
				strips[x] = new Vertex3d[(detail + 1) * 7];
				normals[x] = new Vertex3d[(detail + 1) * 7];
				textureCoords[x] = new Vertex3d[(detail + 1) * 7];
				
				for (int z = detail - 1; z > -1; z--) {
					if (stitchNegZ && z == 0) {
						//Do stitch in negative Z direction
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (int)((x + 0.5) * divisionSize), z * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, x * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
					} else if (stitchPosZ && z == detail - 1) {
						//Do stitch in positive Z direction
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, x * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, (int)((x + 0.5) * divisionSize), (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
					} else {
						//Do normal stitching
						createDataForPoint(x, vertexCount++, x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, x * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
					}
				}
			} else if (stitchPosX && x == detail - 1) {
				//Perform stitching in the positive X direction
				strips[x] = new Vertex3d[(detail + 1) * 6];
				normals[x] = new Vertex3d[(detail + 1) * 6];
				textureCoords[x] = new Vertex3d[(detail + 1) * 6];
				
				for (int z = 0; z < detail; z++) {
					if (stitchNegZ && z == 0) {
						//Do stitch in negative Z direction
						createDataForPoint(x, vertexCount++, x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (int)((x + 0.5) * divisionSize), z * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
					} else if (stitchPosZ && z == detail - 1) {
						//Do stitch in positive Z direction
						createDataForPoint(x, vertexCount++, x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, (int)((x + 0.5) * divisionSize), (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);

						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
					} else {
						//Do normal stitching
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
	
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (int)((z + 0.5) * divisionSize), xOffsetStart, zOffsetStart);
	
						createDataForPoint(x, vertexCount++, x * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
						
						createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (z + 1) * divisionSize, xOffsetStart, zOffsetStart);
					}
				}
			} else {
				strips[x] = new Vertex3d[(detail + 1) * 2 + 9];
				normals[x] = new Vertex3d[(detail + 1) * 2 + 9];
				textureCoords[x] = new Vertex3d[(detail + 1) * 2 + 9];

				//Z = 0 line
				if (stitchNegZ) {
					//Do stitch in negative Z direction
					createDataForPoint(x, vertexCount++, x * divisionSize, 0, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, x * divisionSize, divisionSize, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, (int)((x + 0.5) * divisionSize), 0, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, divisionSize, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, 0, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, divisionSize, xOffsetStart, zOffsetStart);
				} else {
					//No stitching needed
					createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, 0, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, x * divisionSize, 0, xOffsetStart, zOffsetStart);
				}
	
				//Center
				for (int z = 1; z < detail; z++) {
					createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
	
					createDataForPoint(x, vertexCount++, x * divisionSize, z * divisionSize, xOffsetStart, zOffsetStart);
				}
	
				//Z = detail line
				if (stitchPosZ) {
					//Do stitch in positive Z direction
					createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (detail - 1) * divisionSize, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (detail - 1) * divisionSize, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, detail * divisionSize, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, (detail - 1) * divisionSize, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, (int)((x + 0.5) * divisionSize), detail * divisionSize, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, x * divisionSize, (detail - 1) * divisionSize, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, x * divisionSize, detail * divisionSize, xOffsetStart, zOffsetStart);
				} else {
					//No stitching needed
					createDataForPoint(x, vertexCount++, (x + 1) * divisionSize, detail * divisionSize, xOffsetStart, zOffsetStart);
		
					createDataForPoint(x, vertexCount++, x * divisionSize, detail * divisionSize, xOffsetStart, zOffsetStart);
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
	
	public Vertex3d[][] getTextureCoords() {
		return textureCoords;
	}
	
	public int[] getVertexCounts() {
		return vertexCounts;
	}
	
	private void createDataForPoint(int stripCount, int vertexCount, int x, int z, int xOffsetStart, int zOffsetStart) {
		normals[stripCount][vertexCount] = new Vertex3d(normalMap[x][z]);
		textureCoords[stripCount][vertexCount] = createTextureCoordsForPoint(x, z);
		strips[stripCount][vertexCount++] = createVertexForPoint(x, z, xOffsetStart, zOffsetStart);
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
	private void createNormalForPoint(int x, int z, int xOffsetStart, int zOffsetStart, int divisionSize) {
		int worldX = x + xOffsetStart;
		int worldZ = z + zOffsetStart;
		
		double y1 = heightMap[x][z];

		Vertex3d v1;
		Vertex3d v2;
		Vertex3d v3;
		Vertex3d v4;
		
		if (x > divisionSize / 2) {
			v1 = new Vertex3d(new double[]{-divisionSize, y1 - heightMap[x - divisionSize][z], 0.0});
		} else {
			v1 = new Vertex3d(new double[]{-divisionSize, y1 - generateHeightForPoint(worldX - divisionSize, worldZ), 0.0});
		}
		if (x < Configuration.TERRAIN_TILE_SIZE - divisionSize / 2) {
			v2 = new Vertex3d(new double[]{divisionSize, y1 - heightMap[x + divisionSize][z], 0.0});
		} else {
			v2 = new Vertex3d(new double[]{divisionSize, y1 - generateHeightForPoint(worldX + divisionSize, worldZ), 0.0});
		}
		if (z > divisionSize / 2) {
			v3 = new Vertex3d(new double[]{0.0, y1 - heightMap[x][z - divisionSize],  -divisionSize});
		} else {
			v3 = new Vertex3d(new double[]{0.0, y1 - generateHeightForPoint(worldX, worldZ - divisionSize),  -divisionSize});
		}
		if (z < Configuration.TERRAIN_TILE_SIZE - divisionSize / 2) {
			v4 = new Vertex3d(new double[]{0.0, y1 - heightMap[x][z + divisionSize],  divisionSize});
		} else {
			v4 = new Vertex3d(new double[]{0.0, y1 - generateHeightForPoint(worldX, worldZ + divisionSize),  divisionSize});
		}
		
		Vertex3d v5 = Vertex3d.crossProduct(v1, v4);
		Vertex3d v6 = Vertex3d.crossProduct(v2, v3);
		
		normalMap[x][z].values[0] = v5.values[0] + v6.values[0];
		normalMap[x][z].values[1] = v5.values[1] + v6.values[1];
		normalMap[x][z].values[2] = v5.values[2] + v6.values[2];
		
		normalMap[x][z].normalize();
	}
	
	private Vertex3d createTextureCoordsForPoint(int x, int z) {
		return new Vertex3d(new double[]{x, z, 1.0});
	}
	
	/**
	 * Generate a height value for a point using the world heightmap and the tile noise
	 * @param x Worldspace X coordinate
	 * @param z Worldspace Z coordinate
	 * @return
	 */
	private double generateHeightForPoint(int x, int z) {
		return terrain.getHeightAt(x, z) + noise.getHeight(x,z) * 15;
	}

}
