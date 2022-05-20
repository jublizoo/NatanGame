import java.util.ArrayList;

public class SoftMesh {
	
	ArrayList<Edge> edges = new ArrayList<Edge>();
	ArrayList<Vertex> vertices = new ArrayList<Vertex>();
	ArrayList<Vertex> outline = new ArrayList<Vertex>();
	
	//Number of (theoretical) milliseconds between each call of moveVertices
	int subTick;
	final static double dampingFactor = 0.9;
	final static double collisionDampingFactor = 0.7;
	
	public SoftMesh(int subTick) {
		this.subTick = subTick;
		
		vertices.add(new Vertex(200, 200, false));
		vertices.add(new Vertex(400, 0, false));
		vertices.add(new Vertex(300, 300, false));
		vertices.add(new Vertex(100, 300, false));
		/*
		vertices.add(new Vertex(300, 250, false));
		vertices.add(new Vertex(300, 200, false));
		edges.add(new Edge(0, 1, 50, 10));
		edges.add(new Edge(1, 2, 50, 10));
		edges.add(new Edge(2, 3, 50, 10));
		edges.add(new Edge(3, 4, 50, 10));
		edges.add(new Edge(4, 5, 50, 10));
		edges.add(new Edge(5, 1, 50, 10));
		edges.add(new Edge(0, 4, 50, 10));
		edges.add(new Edge(1, 2, 50, 10));
		*/
		edges.add(new Edge(0, 1, 100, 10));
		edges.add(new Edge(1, 2, 100, 10));
		edges.add(new Edge(2, 3, 100, 10));
		edges.add(new Edge(3, 0, 100, 10));
		edges.add(new Edge(0, 2, 100 * Math.sqrt(2), 5));
		edges.add(new Edge(1, 3, 100 * Math.sqrt(2), 5));
		//edges.add(new Edge(2, 0, (int) (100 * Math.sqrt(2)), 5));
		//edges.add(new Edge(3, 1, (int) (100 * Math.sqrt(2)), 5));
		
	}
	
	public void onTick(int millis) {
		for(int i = 0; i <= millis; i += subTick) {
			moveVertices(subTick);
		}
		moveVertices(millis % subTick);
		
	}
	
	private void moveVertices(int millis) {
		Edge edge;
		Vertex v1 = null;
		Vertex v2 = null;
		//Length of each edge
		double length;
		//Force applied by each edge
		double force;
		
		//Moving vertices
		for(Vertex v : vertices) {
			v.x += v.velX * (millis / 1000.0);
			v.y += v.velY * (millis / 1000.0);
		}
		
		//Updating lengths
		for(Edge e : edges) {
			length = 0;
			v1 = vertices.get(e.v1);
			v2 = vertices.get(e.v2);
			length += Math.pow(v1.x - v2.x, 2);
			length += Math.pow(v1.y - v2.y, 2);
			length = Math.sqrt(length);
			e.length = length;
		}
		
		//Summing forces
		for(int a = 0; a < vertices.size(); a++) {
			v1 = vertices.get(a);
			
			v1.velX *= 1 - dampingFactor * (millis / 1000.0);
			v1.velY *= 1 - dampingFactor * (millis / 1000.0);	
			
			for(int b = 0; b < edges.size(); b++) {
				edge = edges.get(b);
				
				if(edge.v1 == a || edge.v2 == a) {
					if(edge.v1 == a){
						v2 = vertices.get(edge.v2);
					}else if(edge.v2 == a) {
						v2 = vertices.get(edge.v1);
					}
					
					force = edge.getForce();					
					v1.forceX = force * (v1.x - v2.x) / edge.length;
					v1.forceY = force * (v1.y - v2.y) / edge.length;
					v1.velX += -v1.forceX * (millis / 1000.0) / v1.mass;
					v1.velY += -v1.forceY * (millis / 1000.0) / v1.mass;	
				}
			}
		}
		
		//Detecting collision
		
		//Vertices on intersecting edges
		Vertex[] intersectingVertices = new Vertex[4];
		//Distance from intersecting vertices to the corresponding edge
		Double[] distances = new Double[4];
		int minDistanceIndex;
		Double[] intersection = null;
		boolean intersecting = false;
		boolean intersected = false;
		//The edge and vertex whose velocity will be affected by the collision. v1 and v2 will be set to the cEdge vertices
		Edge cEdge;
		Vertex cVertex;
		
		for(int a = 0; a < vertices.size() - 2 && !intersected; a++) {
			intersectingVertices[0] = vertices.get(a);
			intersectingVertices[1] = vertices.get(a + 1);
			
			for(int b = a + 2; b < vertices.size() && !intersected; b++) {
				intersectingVertices[2] = vertices.get(b);
				if(b + 1 == vertices.size()) {
					//We do not want to check intersections between adjacent edges
					if(a == 0) {
						break;
					}
					intersectingVertices[3] = vertices.get(0);
				}else {
					intersectingVertices[3] = vertices.get(b + 1);
				}
				
				intersection = findIntersection(intersectingVertices[0], intersectingVertices[1], intersectingVertices[2], intersectingVertices[3]);
				if(intersection != null) {
					intersecting = true;
					intersected = true;					

					distances[0] = distanceToLine(intersectingVertices[0], new Vertex[] {intersectingVertices[2], intersectingVertices[3]});
					distances[1] = distanceToLine(intersectingVertices[1], new Vertex[] {intersectingVertices[2], intersectingVertices[3]});
					distances[2] = distanceToLine(intersectingVertices[2], new Vertex[] {intersectingVertices[0], intersectingVertices[1]});
					distances[3] = distanceToLine(intersectingVertices[3], new Vertex[] {intersectingVertices[0], intersectingVertices[1]});
				}
			}
		}
		
		//Reversing collision		
		while(intersecting) {				
			for(Vertex v : vertices) {
				v.x -= v.velX * (millis / 1000.0) / 10;
				v.y -= v.velY * (millis / 1000.0) / 10;
			}
			
			intersecting = intersecting();
		}
		
		//Causing "bounce"
		double angle;
		
		//The x and y components (as ratios of edge length) of the edge perpendicular (rotated 90 degrees counterclockwise) to the colliding edge
		Double[] perpRatio = new Double[2];
		
		double cEdgeMass;
		//Distance from collision point on edge to v1 (first vertex on edge)
		double distance;
		//Ratio across the edge of the collision point.
		double cEdgeRatio;
		//Information about the collision point on the colliding edge
		Double[] cEdgeVector = new Double[2];
		double cEdgeVelocity;
		double perpEdgeVelocity;
		Double[] perpEdgeVector = new Double[2];
		double finalEdgeVelocity;
		Double[] finalEdgeVector = new Double[2];
		
		//The element of the vertex velocity which is perpendicular to the edge
		double perpVertexVelocity;
		Double[] perpVertexVector = new Double[2];
		double finalVertexVelocity;
		Double[] finalVertexVector = new Double[2];
		
		double centerRatio;
		
		if(intersected) {
			System.out.println("bounce!");
			minDistanceIndex = 0;
			for(int i = 1; i < distances.length; i++) {
				if(distances[i] / intersectingVertices[i].getVelocity() < distances[minDistanceIndex] / intersectingVertices[minDistanceIndex].getVelocity()) {
					minDistanceIndex = i;
				}
			}
			
			cVertex = intersectingVertices[minDistanceIndex];
			if(minDistanceIndex < 2) {
				v1 = intersectingVertices[2];
				v2 = intersectingVertices[3];
			}else {
				v1 = intersectingVertices[0];
				v2 = intersectingVertices[1];
			}
			
			cEdge = findEdge(v1, v2);
			cEdgeMass = v1.mass + v2.mass;
			//cEdge.length = Math.sqrt(Math.pow(v2.x - v1.x, 2) + Math.pow(v2.y - v1.y, 2));
			
			//X and Y components of edge rotates 90 degrees counterclockwise
			perpRatio[0] = -(v2.y - v1.y) / cEdge.length;
			perpRatio[1] = (v2.x - v1.x) / cEdge.length;
			
			distance = Math.pow(v1.x - intersection[0], 2);
			distance += Math.pow(v1.y - intersection[1], 2);
			distance = Math.sqrt(distance);
			cEdgeRatio = distance / cEdge.length;
			cEdgeVector[0] = v1.velX * (1 - cEdgeRatio) + v2.velX * cEdgeRatio;
			cEdgeVector[1] = v1.velY * (1 - cEdgeRatio) + v2.velY * cEdgeRatio;
			cEdgeVelocity = Math.pow(cEdgeVector[0], 2);
			cEdgeVelocity += Math.pow(cEdgeVector[1], 2);
			cEdgeVelocity = Math.sqrt(cEdgeVelocity);
			angle = getAngle(cEdgeVector[0], cEdgeVector[1]) - getAngle(v2.x - v1.x, v2.y - v1.y);
			perpEdgeVelocity = cEdgeVelocity * Math.sin(angle);	
			perpEdgeVector[0] = perpEdgeVelocity * perpRatio[0];
			perpEdgeVector[1] = perpEdgeVelocity * perpRatio[1];
						
			angle = getAngle(cVertex.velX, cVertex.velY) - getAngle(v2.x - v1.x, v2.y - v1.y);
			perpVertexVelocity = cVertex.getVelocity() * Math.sin(angle);
			perpVertexVector[0] = perpVertexVelocity * perpRatio[0];
			perpVertexVector[1] = perpVertexVelocity * perpRatio[1];
			
			finalEdgeVelocity = collisionDampingFactor * ((cEdgeMass - cVertex.mass) * perpEdgeVelocity + 2 * cVertex.mass * perpVertexVelocity) / (cEdgeMass + cVertex.mass);
			finalVertexVelocity = collisionDampingFactor * ((cVertex.mass - cEdgeMass) * perpVertexVelocity + 2 * cEdgeMass * perpEdgeVelocity) / (cVertex.mass + cEdgeMass);
			
			finalVertexVector[0] = finalVertexVelocity * perpRatio[0];
			finalVertexVector[1] = finalVertexVelocity * perpRatio[1];
			finalEdgeVector[0] = finalEdgeVelocity * perpRatio[0];
			finalEdgeVector[1] = finalEdgeVelocity * perpRatio[1];
			
			centerRatio = (0.5 * cEdge.length - distance) / cEdge.length;
			
			cVertex.velX += finalVertexVector[0] - perpVertexVector[0];
			cVertex.velY += finalVertexVector[1] - perpVertexVector[1];
			v1.velX += finalEdgeVector[0] - perpEdgeVector[0];
			v1.velY += finalEdgeVector[1] - perpEdgeVector[1];
			v2.velX += finalEdgeVector[0] - perpEdgeVector[0];
			v2.velY += finalEdgeVector[1] - perpEdgeVector[1];
			
//			try {
//				Thread.sleep(1000000);
//			} catch (InterruptedException e1) {}
			
			
			/*
			parallelVelocity = cVertex.getVelocity() * Math.cos(angle);
			parallelVector[0] = parallelVelocity * (v2.x - v1.x) / cEdge.length;
			parallelVector[1] = parallelVelocity * (v2.y - v1.y) / cEdge.length;
			//Subtracting 2 times the perpendicular vector, to reverse the perpendicular vector.
			cVertex.velX -= 2 * (cVertex.velX - parallelVector[0]);
			cVertex.velY -= 2 * (cVertex.velY - parallelVector[1]);
			*/
			
		}
		
	}
	
	public double getAngle(double x, double y) {
		double angle = Math.atan(y / x);
		if(x < 0) {
			angle += Math.PI;
		}
		
		return angle;
		
	}
	
	public Edge findEdge(Vertex v1, Vertex v2) {
		for(Edge e : edges) {
			if(vertices.get(e.v1) == v1 && vertices.get(e.v1) == v1) {
				return e;
			}
			if(vertices.get(e.v2) == v1 && vertices.get(e.v1) == v2) {
				return e;
			}
		}
		
		return null;
		
	}
	
	public boolean intersecting() {
		Vertex v1 = null;
		Vertex v2 = null;
		Vertex v3 = null;
		Vertex v4 = null;
		Double[] intersection;
		boolean intersecting = false;
		//Checks 2 segments in front (the segment after the next segment), so you only have to go to the third to last segment in order to compare to the last segment
		for(int a = 0; a < vertices.size() - 2; a++) {
			v1 = vertices.get(a);
			v2 = vertices.get(a + 1);
			
			for(int b = a + 2; b < vertices.size(); b++) {
				v3 = vertices.get(b);
				if(b + 1 == vertices.size()) {
					//We do not want to check intersections for adjacent edges
					if(a == 0) {
						break;
					}
					v4 = vertices.get(0);
				}else {
					v4 = vertices.get(b + 1);
				}
				
				intersection = findIntersection(v1, v2, v3, v4);
				if(intersection != null) {
					intersecting = true;
				}
			}
		}
		
		return intersecting;
		
	}
	
	public double distanceToLine(Vertex v, Vertex[] edge){
		//ax + by + c = 0
		double a;
		double b;
		double c;
		double distance;
		
		if(edge[0].x == edge[1].x) {
			a = -1;
			b = 0;
			c = edge[0].x;	
		}else {
			a = (edge[1].y - edge[0].y) / (edge[1].x - edge[0].x);
			b = -1;
			c = edge[0].y - a * edge[0].x;
		}
		
		distance = Math.abs(a * v.x + b * v.y + c) / Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		return distance;
		
	}
	
	public Double[] findIntersection(Vertex a1, Vertex a2, Vertex b1, Vertex b2) {
		Double[] maxA;
		Double[] minA;
		Double[] maxB;
		Double[] minB;
		//These variables must be initialized so we can used them later
		double slopeA = 0;
		double slopeB = 0;
		double interceptA = 0;
		double interceptB = 0;
		//Are lines a and b vertical (infinite slopes)
		boolean infA = false;
		boolean infB = false;
		Double[] intersection = new Double[2];

		if (a2.x - a1.x != 0) {
			slopeA = (a2.y - a1.y) / (a2.x - a1.x);
			interceptA = a1.y - (a1.x * slopeA);
		} else {
			infA = true;
			intersection[0] = a1.x;
		}

		if (b2.x - b1.x != 0) {
			slopeB = (b2.y - b1.y) / (b2.x - b1.x);
			interceptB = b1.y - (b1.x * slopeB);
		} else {
			infB = true;
			intersection[0] = b1.x;
		}

		/*
		 * If the infA and infB are false, we set the value manually, because the slope
		 * was infinite. We cannot find the intersection using the same formula if the
		 * slope is infinite, so we must check if this value has been initialized, so we
		 * can decide which formula to use.
		 */
		if (!infA && !infB) {
			if (slopeA != slopeB) {
				intersection[0] = (interceptB - interceptA) / (slopeA - slopeB);
				intersection[1] = intersection[0] * slopeA + interceptA;
			} else {
				return null;
			}
		} else if (infA && infB) {
			return null;
		} else if (infA) {
			intersection[1] = intersection[0] * slopeB + interceptB;
		} else {
			intersection[1] = intersection[0] * slopeA + interceptA;
		}

		maxA = new Double[] { Math.max(a1.x, a2.x), Math.max(a1.y, a2.y) };
		minA = new Double[] { Math.min(a1.x, a2.x), Math.min(a1.y, a2.y) };
		maxB = new Double[] { Math.max(b1.x, b2.x), Math.max(b1.y, b2.y) };
		minB = new Double[] { Math.min(b1.x, b2.x), Math.min(b1.y, b2.y) };
		//Checking if the point is within both of the segments
		if (intersection[0] < maxA[0] && intersection[0] > minA[0] && intersection[1] < maxA[1]
				&& intersection[1] > minA[1]) {
			if (intersection[0] < maxB[0] && intersection[0] > minB[0] && intersection[1] < maxB[1]
					&& intersection[1] > minB[1]) {
				return intersection;
			}
		}

		return null;

	}

}
