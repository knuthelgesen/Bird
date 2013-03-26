package no.plasmid.bird;

import java.util.LinkedList;
import java.util.List;

import no.plasmid.bird.im.Camera;
import no.plasmid.bird.im.Terrain;
import no.plasmid.bird.im.TerrainTile;

public class TerrainTileManager {

	private TerrainTileUpdateThread terrainTileUpdateThread;
	
	private List<TerrainTile> tileList;

	public TerrainTileManager() {
		tileList = new LinkedList<TerrainTile>();
		for (int x = 0; x < Configuration.TERRAIN_SIZE; x++) {
			for (int z = 0; z < Configuration.TERRAIN_SIZE; z++) {
				tileList.add(new TerrainTile(x, z));
			}
		}
	}

	public List<TerrainTile> getTileList() {
		return tileList;
	}
	
	public void startTerainTileUpdateThread(Terrain terrain, Camera camera) {
		terrainTileUpdateThread = new TerrainTileUpdateThread(camera, terrain);
		terrainTileUpdateThread.start();
	}

	public void stopTerrainTileUpdateThread() {
		if (terrainTileUpdateThread != null) {
			terrainTileUpdateThread.setFinished();
		}
	}
	
	private class TerrainTileUpdateThread extends Thread {
		
		private boolean finished = false;
		
		private Camera camera;
		private Terrain terrain;
		
		private TerrainTileUpdateThread(Camera camera, Terrain terrain) {
			this.camera = camera;
			this.terrain = terrain;
		}
		
		public void setFinished() {
			finished = true;
		}
		
		public void run() {
			while (!finished) {
				int cameraTileX = (int)-camera.getPosition().getValues()[0] / 256;
				int cameraTileZ = (int)-camera.getPosition().getValues()[2] / 256;
				for (TerrainTile tile : tileList) {
					if (calculateRange(cameraTileX, tile.getTileX(), cameraTileZ, tile.getTileZ()) > 55) {
						tile.dropMesh();
					} else {
						if (!tile.isReadyForDrawing()) {
							tile.generateMesh(terrain);
						}
					}
				}
			}
		}
		
		/**
		 * Calculates range in "Manhattan units"
		 * @return
		 */
		private int calculateRange(int x1, int x2, int z1, int z2) {
			return Math.abs(x2 - x1) + Math.abs(z2 - z1);
		}
	}
	
}
