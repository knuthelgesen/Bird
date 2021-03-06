package no.plasmid.bird;

import java.util.LinkedList;
import java.util.List;

import no.plasmid.bird.im.Camera;
import no.plasmid.bird.im.Terrain;
import no.plasmid.bird.im.TerrainTile;
import no.plasmid.bird.im.TerrainTileMesh;

public class TerrainTileManager {

	private TerrainTileUpdateThread terrainTileUpdateThread;
	
	private List<TerrainTile> tileList;

	public TerrainTileManager() {
		tileList = new LinkedList<TerrainTile>();
	}

	public List<TerrainTile> getTileList() {
		return tileList;
	}
	
	public void startTerainTileUpdateThread(Terrain terrain, Camera camera) {
		//Create tiles
		for (int x = 0; x < Configuration.TERRAIN_SIZE; x++) {
			for (int z = 0; z < Configuration.TERRAIN_SIZE; z++) {
				TerrainTile tile = new TerrainTile(x, z);
				tileList.add(tile);
				terrain.getTiles()[x][z] = tile;
			}
		}

		//Create and start update thread
		terrainTileUpdateThread = new TerrainTileUpdateThread(camera, terrain);
		terrainTileUpdateThread.start();
	}

	public void stopTerrainTileUpdateThread() {
		if (terrainTileUpdateThread != null) {
			terrainTileUpdateThread.setFinished();
		}
	}
	
	private class TerrainTileUpdateThread extends Thread {
		
		private String lock = "";
		
		private boolean finished = false;
		
		private Camera camera;
		private Terrain terrain;
		
		private List<TerrainTileMesh> unusedMeshes;
		
		private TerrainTileUpdateThread(Camera camera, Terrain terrain) {
			this.camera = camera;
			this.terrain = terrain;
			
			unusedMeshes = new LinkedList<TerrainTileMesh>();
		}
		
		public void setFinished() {
			finished = true;
		}
		
		public void run() {
			while (!finished) {
				int cameraTileX = (int)-camera.getPosition().getValues()[0] / Configuration.TERRAIN_TILE_SIZE;
				int cameraTileZ = (int)-camera.getPosition().getValues()[2] / Configuration.TERRAIN_TILE_SIZE;
				for (TerrainTile tile : tileList) {
					int range = calculateRange(cameraTileX, tile.getTileX(), cameraTileZ, tile.getTileZ());
					if (range > 150) {
						if (tile.isReadyForDrawing()) {
							unusedMeshes.add(tile.dropMesh());
						}
					} else {
						int divisionsSize = nextPow2(range * 4);
						
						divisionsSize = Math.max(1, divisionsSize);
						divisionsSize = Math.min(Configuration.TERRAIN_TILE_SIZE, divisionsSize);
						if (!tile.isReadyForDrawing()) {
							if (unusedMeshes.size() < 10) {
								add100UnusedMeshes();
							}
							tile.generateMesh(terrain, divisionsSize, unusedMeshes.remove(0));
						} else {
							if (tile.getDivisionSize() != divisionsSize || tile.isRecreateMeshRequested()) {
								if (unusedMeshes.size() < 10) {
									add100UnusedMeshes();
								}
								unusedMeshes.add(tile.replaceMesh(terrain, divisionsSize, unusedMeshes.remove(0)));
							}
						}
					}
				}
				
				synchronized (lock) {
					try {
						lock.wait(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		/**
		 * Calculates range in "Manhattan units"
		 * @return
		 */
		private int calculateRange(int x1, int x2, int z1, int z2) {
			return (int)(Math.sqrt((x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1))); 
		}
		
		private int nextPow2(int value) {
			int rc = 1;
			while (rc < value) {
				rc = rc << 1;
			}
			return rc;
		}
		
		private void add100UnusedMeshes() {
			for (int i = 0; i < 100; i++) {
				unusedMeshes.add(new TerrainTileMesh());
			}
		}
	}
	
}
