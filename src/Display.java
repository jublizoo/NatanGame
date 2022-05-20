import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class Display extends JPanel{
	
	Graphics2D g2d;
	SoftMesh mesh;
	
	public Display(SoftMesh mesh) {
		this.mesh = mesh;
		
	}

	protected void paintComponent(Graphics g) {
		g2d = (Graphics2D) g;
		paintSoftMesh(mesh);
		
	}
	
	public void paintSoftMesh(SoftMesh mesh){
		Vertex v1;
		Vertex v2;
		
		for(Vertex v : mesh.vertices) {
			g2d.fillOval((int) v.x - 2, (int) v.y - 2, 4, 4);
		}
		
		for(int i = 0; i < mesh.vertices.size(); i++) {
			v1 = mesh.vertices.get(i);
			if(i + 1 == mesh.vertices.size()) {
				v2 = mesh.vertices.get(0);
			}else {
				v2 = mesh.vertices.get(i + 1);
			}			
			g2d.drawLine((int) Math.round(v1.x), (int) Math.round(v1.y), (int) Math.round(v2.x), (int) Math.round(v2.y));
		}
		
	}
	
}
