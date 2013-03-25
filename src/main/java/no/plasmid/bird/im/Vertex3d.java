package no.plasmid.bird.im;

public class Vertex3d {

	double[] values;
	
	public Vertex3d() {
		this(new double[]{0.0, 0.0, 0.0});
	}
	
	public Vertex3d(double[] values) {
		if (values.length != 3) {
			throw new IllegalArgumentException("Number of values must be three");
		}
		this.values = values;
	}
	
	public double[] getValues() {
		return values;
	}
	
}