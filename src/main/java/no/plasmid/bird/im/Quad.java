package no.plasmid.bird.im;

public class Quad {

	private Vertex3d[] corners;
	
	public Quad(Vertex3d[] corners) {
		if (corners.length != 4) {
			throw new IllegalArgumentException("Number of corners must be four");
		}
		
		this.corners = corners;
	}

	public Vertex3d[] getCorners() {
		return corners;
	}
	
}
