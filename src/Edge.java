
public class Edge {
	int v1;
	int v2;
	
	double defaultLength;
	double length;
	//Spring constant
	double k;
	
	public Edge(int v1, int v2, double length, double k) {
		this.v1 = v1;
		this.v2 = v2;
		this.defaultLength = length;
		this.length = length;
		this.k = k;
		
	}
	
	public double getForce() {
		return (length - defaultLength) * k;
				
	}
	
}
