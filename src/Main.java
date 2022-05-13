import javax.swing.JFrame;

public class Main {
	
	SoftMesh hair = new SoftMesh(5);
	JFrame frame = new JFrame();
	Display display = new Display(hair);
	
	public static void main(String[] args) {
		new Main();
		
	}
	
	public Main() {
		initFrame();
		while(true) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {}
			hair.onTick(20);
			frame.repaint();
		}
		
	}
	
	private void initFrame(){
		frame.setSize(1000, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(display);
		
	}
	
}
