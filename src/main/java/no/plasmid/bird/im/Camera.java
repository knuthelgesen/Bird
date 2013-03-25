package no.plasmid.bird.im;

public class Camera {

	private Vertex3d position;
	private Vertex3d rotation;	//In degrees
	
	public Camera() {
		position = new Vertex3d();
		rotation = new Vertex3d();
	}
	
	public Vertex3d getPosition() {
		return position;
	}

	public void setPosition(Vertex3d position) {
		this.position = position;
	}

	public Vertex3d getRotation() {
		return rotation;
	}

	public void setRotation(Vertex3d rotation) {
		this.rotation = rotation;
	}
	
	public void moveCamera(float deltaX, float deltaY, float deltaZ) {
		position.values[0] += deltaX * Math.sin((Math.toRadians(rotation.values[1] + 90)));
		position.values[2] += deltaX * Math.cos((Math.toRadians(rotation.values[1] - 90)));
		
		position.values[1] += deltaY;

		position.values[0] += deltaZ * Math.sin((Math.toRadians(rotation.values[1] - 180)));
		position.values[2] += deltaZ * Math.cos((Math.toRadians(rotation.values[1])));
		position.values[1] += deltaZ * Math.sin((Math.toRadians(rotation.values[0])));
	}
	
	public void rotateCamera(float deltaXAngle, float deltaYAngle, float deltaZAngle) {
		this.rotation.values[0] += deltaXAngle;
		this.rotation.values[1] += deltaYAngle;
		this.rotation.values[2] += deltaZAngle;
	}
	
}
