package no.plasmid.bird.im;

import no.plasmid.bird.Configuration;
import no.plasmid.bird.util.PerlinNoise;

public class TerrainTileMesh {

	private static PerlinNoise noise = new PerlinNoise(Configuration.TERRAIN_TILE_NOISE_PERSISTENCE,
			Configuration.TERRAIN_TILE_NOISE_FREQUENCY, Configuration.TERRAIN_TILE_NOISE_AMPLITUDE,
			Configuration.TERRAIN_TILE_NOISE_OCTAVES, Configuration.TERRAIN_TILE_NOISE_RANDOM_SEED);

	private Terrain terrain;
	
	private static double[][] heightMap = new double[Configuration.TERRAIN_TILE_SIZE + 1][Configuration.TERRAIN_TILE_SIZE + 1];
	private static Vertex3d[][] normalMap = new Vertex3d[Configuration.TERRAIN_TILE_SIZE + 1][Configuration.TERRAIN_TILE_SIZE + 1];
	private static double[][] moistureMap = new double[Configuration.TERRAIN_TILE_SIZE + 1][Configuration.TERRAIN_TILE_SIZE + 1];
	private static double[][] temperatureMap = new double[Configuration.TERRAIN_TILE_SIZE + 1][Configuration.TERRAIN_TILE_SIZE + 1];
	private static double[][] steepnessMap = new double[Configuration.TERRAIN_TILE_SIZE + 1][Configuration.TERRAIN_TILE_SIZE + 1];

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
	private Vertex3d[][] colors;	//Colors (alpha will always be 1.0)
	private int[] vertexCounts;
	
	/**
	 * Generate a mesh that can be rendered.
	 * @param terrain
	 * @param tileX
	 * @param tileZ
	 * @param divisionSize
	 */
	public void generateMeshFromHeightMap(Terrain terrain, TerrainTile tile) {
		this.terrain = terrain;
		
		int tileX = tile.getTileX();
		int tileZ = tile.getTileZ();
		int divisionSize = tile.getDivisionSize();
		
		int detail = Configuration.TERRAIN_TILE_SIZE / divisionSize;
		int heightMapSize = Configuration.TERRAIN_TILE_SIZE + 1;

		//Calculate offsets
		int xOffsetStart = tileX * Configuration.TERRAIN_TILE_SIZE;
		int zOffsetStart = tileZ * Configuration.TERRAIN_TILE_SIZE;

		//Generate height map
		double[][] terrainHeightMap = terrain.getHeightMap();
		interpolateTileMap(terrainHeightMap[tileX][tileZ], terrainHeightMap[tileX + 1][tileZ], terrainHeightMap[tileX][tileZ + 1], terrainHeightMap[tileX + 1][tileZ + 1], heightMap, 150);

		//Generate temperature map
		double[][] terrainTemperatureMap = terrain.getTemperatureMap();
		interpolateTileMap(terrainTemperatureMap[tileX][tileZ], terrainTemperatureMap[tileX + 1][tileZ], terrainTemperatureMap[tileX][tileZ + 1], terrainTemperatureMap[tileX + 1][tileZ + 1], temperatureMap, 1);
		
		//Generate moisture map
		double[][] terrainMoistureMap = terrain.getMoistureMap();
		interpolateTileMap(terrainMoistureMap[tileX][tileZ], terrainMoistureMap[tileX + 1][tileZ], terrainMoistureMap[tileX][tileZ + 1], terrainMoistureMap[tileX + 1][tileZ + 1], moistureMap, 1);
		
		//Generate steepness map
		double[][] terrainSteepnessMap = terrain.getSteepnessMap();
		interpolateTileMap(terrainSteepnessMap[tileX][tileZ], terrainSteepnessMap[tileX + 1][tileZ], terrainSteepnessMap[tileX][tileZ + 1], terrainSteepnessMap[tileX + 1][tileZ + 1], steepnessMap, 1);
		
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

		int heightMapDivisionSize = divisionSize / 2;
		
		if (divisionSize == 1) {
			heightMapDivisionSize = 1;
		}
		for (int x = 0; x < heightMapSize; x += heightMapDivisionSize) {
			for (int z = 0; z < heightMapSize; z += heightMapDivisionSize) {
//				heightMap[x][z] = generateHeightForPoint(x + xOffsetStart, z + zOffsetStart);
//				heightMap[x][z] += noise.getHeight(x + xOffsetStart,z + zOffsetStart);
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
		colors = new Vertex3d[detail][];
		vertexCounts = new int[detail];
		for (int x = 0; x < detail; x++) {
			int vertexCount = 0;

			if (stitchNegX && x == 0) {
				//Perform stitching in the negative X direction
				strips[x] = new Vertex3d[(detail + 1) * 7];
				normals[x] = new Vertex3d[(detail + 1) * 7];
				textureCoords[x] = new Vertex3d[(detail + 1) * 7];
				colors[x] = new Vertex3d[(detail + 1) * 7];
				
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
				colors[x] = new Vertex3d[(detail + 1) * 6];
				
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
				colors[x] = new Vertex3d[(detail + 1) * 2 + 9];

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
	
	public Vertex3d[][] getColors() {
		return colors;
	}
	
	public int[] getVertexCounts() {
		return vertexCounts;
	}
	
	private void createDataForPoint(int stripCount, int vertexCount, int x, int z, int xOffsetStart, int zOffsetStart) {
		Vertex3d vertex = createVertexForPoint(x, z, xOffsetStart, zOffsetStart);
		double temperature = temperatureMap[x][z];
		double moisture = moistureMap[x][z];
		//Get the normal
		normals[stripCount][vertexCount] = new Vertex3d(normalMap[x][z]);
		
		//Calculate which texture and color to use
		double textureP = 0.875;	//Set initially to grass
		colors[stripCount][vertexCount] = calculateGrassColors(temperature, moisture);
		if (vertex.values[1] < 5) {
			//Steep enough to be sand
			textureP = 0.125;
			colors[stripCount][vertexCount] = new Vertex3d(new double[]{1.0, 1.0, 1.0});
		}
//		if (vertex.values[1] > 5000 || normals[stripCount][vertexCount].values[1] < 0.8) {
		if (vertex.values[1] > 5000 || steepnessMap[x][z] > 0.8) {
			//Steep enough to be rock
			textureP = 0.375;
			colors[stripCount][vertexCount] = new Vertex3d(new double[]{0.75, 0.75, 0.75});
		}
		textureCoords[stripCount][vertexCount] = createTextureCoordsForPoint(x, z, textureP);
		
		//Generate the vertex
		strips[stripCount][vertexCount++] = vertex;
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
	
	private Vertex3d createTextureCoordsForPoint(int x, int z, double p) {
		return new Vertex3d(new double[]{x, z, p});
	}
	
	/**
	 * Generate a height value for a point using the world heightmap and the tile noise
	 * @param x Worldspace X coordinate
	 * @param z Worldspace Z coordinate
	 * @return
	 */
	private double generateHeightForPoint(int x, int z) {
//		return terrain.getHeightAt(x, z) + noise.getHeight(x,z);
		return terrain.getHeightAt(x, z);
	}
	
	/**
	 * Will interpolate four values over a 2D map
	 * @param v00 Corner value at 0, 0
	 * @param v10 Corner value at 0, 0
	 * @param v01 Corner value at 0, 0
	 * @param v11 Corner value at 0, 0
	 * @param target Where to store the calculated values
	 * @param factor Value factor
	 */
	private void interpolateTileMap(double v00, double v10, double v01, double v11, double[][] target, int factor) {
		int tileMapSize = Configuration.TERRAIN_TILE_SIZE + 1;
		//Start values for the lines at z = 0 and z = TERRAIN_TILE_SIZE
		double z0 = v00;
		double z1 = v01;
		
		//Delta values for the  lines at z = 0 and z = TERRAIN_TILE_SIZE
		double dz0 = (v10 - v00) / Configuration.TERRAIN_TILE_SIZE;
		double dz1 = (v11 - v01) / Configuration.TERRAIN_TILE_SIZE;
		
		for (int x = 0; x < tileMapSize; x++) {
			//Calculate the values along the z = 0 and z = TERRAIN_TILE_SIZE lines
			target[x][0] = z0 * factor;
			target[x][Configuration.TERRAIN_TILE_SIZE] = z1 * factor;
			
			//Delta value along the z line
			double v = z0;
			double dv = (z1 - z0) / (Configuration.TERRAIN_TILE_SIZE - 1);
			//Fill inn the middle (the line between z = 1 and z = TERRAIN_TILE_SIZE - 1
			for (int z = 1; z < Configuration.TERRAIN_TILE_SIZE; z++) {
				target[x][z] = v * factor;
				v = v + dv;
			}
			z0 = z0 + dz0;
			z1 = z1 + dz1;
		}
	}
	
	private Vertex3d calculateGrassColors(double temperature, double moisture) {
		//Create color based on moisture
		float[] moistureColors = new float[3];
		moistureColors[0] = (float)(1.0 * (1.0 - moisture));
		moistureColors[1] = (float)Math.min(1.0f, 1.7 - moisture);
		moistureColors[2] = 0.5f;

		//Create color based on temperature
		float[] temperatureColors = new float[3];
		temperatureColors[0] = 0.5f;
		temperatureColors[1] = (float)Math.min(1.0f, 1.7 - temperature);
		temperatureColors[2] = (float)((1.0 * (1.0 - temperature)) / 2);
		
		//Combine colors
		return new Vertex3d(new double[]{moistureColors[0], (moistureColors[1] + temperatureColors[1]) / 2, temperatureColors[2]});
	}


}
