package no.plasmid.bird.im;

public class Vertex3d {

	double[] values;
	
	public static Vertex3d crossProduct(Vertex3d v1, Vertex3d v2) {
		double x = (v1.values[1] * v2.values[2]) - (v1.values[2] * v2.values[1]);
		double y = (v1.values[2] * v2.values[0]) - (v1.values[0] * v2.values[2]);
		double z = (v1.values[0] * v2.values[1]) - (v1.values[1] * v2.values[0]);
		
		return new Vertex3d(new double[]{x, y, z});
	}
	
	public Vertex3d() {
		this(new double[]{0.0, 0.0, 0.0});
	}
	
	public Vertex3d(double[] values) {
		if (values.length != 3) {
			throw new IllegalArgumentException("Number of values must be three");
		}
		this.values = values;
	}
	
	/**
	 * Copy constructor
	 * @param original
	 */
	public Vertex3d(Vertex3d original) {
		this(new double[]{original.values[0], original.values[1], original.values[2]});
	}
	
	public double[] getValues() {
		return values;
	}
	
	public void normalize() {
		double length = Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
		
		values[0] /= length;
		values[1] /= length;
		values[2] /= length;
	}
}