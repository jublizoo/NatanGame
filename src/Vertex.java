
public class Vertex {
	double x;
	double y;
	double forceX;
	double forceY;
	double velX;
	double velY;
	double mass;
	
	public Vertex(double x, double y, double mass) {
		this.x = x;
		this.y = y;
		forceX = 0;
		forceY = 0;
		velX = 0;
		velY = 0;
		this.mass = mass;
		
	}
	
	public double getVelocity() {
		double velocity = 0;
		velocity += Math.pow(velX, 2);
		velocity += Math.pow(velY, 2);
		velocity = Math.sqrt(velocity);
		
		return velocity;
		
	}
	
}
