package no.plasmid.bird;

public class Configuration {
	public static final String WINDOW_TITLE	= "Bird";
	public static final int WINDOW_WIDTH		= 900;
	public static final int WINDOW_HEIGTH		= 900;

	public static final float CAMERA_MOVEMENT_SPEED	= 400.42f;
	
	public static final int TERRAIN_SIZE		= 256;	//Unit is terrain tiles
	public static final int TERRAIN_TILE_SIZE	= 256;	//Unit is meters
	
	public static final double TERRAIN_NOISE_PERSISTENCE	= 0.42;
	public static final double TERRAIN_NOISE_FREQUENCY	= 0.05;
	public static final double TERRAIN_NOISE_AMPLITUDE	= 70.0;
	public static final int TERRAIN_NOISE_OCTAVES			= 5;
//	public static final int TERRAIN_NOISE_RANDOM_SEED		= (int)(System.currentTimeMillis() % 46340);
	public static final int TERRAIN_NOISE_RANDOM_SEED		= 1;

	public static final double TERRAIN_TILE_NOISE_PERSISTENCE	= 0.22;
	public static final double TERRAIN_TILE_NOISE_FREQUENCY	= 0.01;
	public static final double TERRAIN_TILE_NOISE_AMPLITUDE	= 7.0;
	public static final int TERRAIN_TILE_NOISE_OCTAVES			= 5;
	public static final int TERRAIN_TILE_NOISE_RANDOM_SEED		= 1;
}
