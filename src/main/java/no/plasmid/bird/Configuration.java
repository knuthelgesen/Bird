package no.plasmid.bird;

public class Configuration {
	public static final String WINDOW_TITLE	= "Bird";
	public static final int WINDOW_WIDTH		= 500;
	public static final int WINDOW_HEIGTH		= 500;

	public static final int TERRAIN_SIZE		= 256;	//Unit is terrain tiles
	public static final int TERRAIN_TILE_SIZE	= 2;	//Unit i meters
	public static final int VERTICAL_SCALE = 256;
	public static final int HOROZONTAL_SCALE = VERTICAL_SCALE / TERRAIN_TILE_SIZE;
	
	public static final double TERRAIN_NOISE_PERSISTENCE	= 0.42;
	public static final double TERRAIN_NOISE_FREQUENCY	= 0.05;
	public static final double TERRAIN_NOISE_AMPLITUDE	= 100.0;
	public static final int TERRAIN_NOISE_OCTAVES			= 15;
	public static final int TERRAIN_NOISE_RANDOM_SEED		= 1;

	public static final double TERRAIN_TILE_NOISE_PERSISTENCE	= 0.32;
	public static final double TERRAIN_TILE_NOISE_FREQUENCY	= 0.37;
	public static final double TERRAIN_TILE_NOISE_AMPLITUDE	= 100.0;
	public static final int TERRAIN_TILE_NOISE_OCTAVES			= 2;
	public static final int TERRAIN_TILE_NOISE_RANDOM_SEED		= 1;
}
