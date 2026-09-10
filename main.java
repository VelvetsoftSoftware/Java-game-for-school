public class main {
	public static void main(String[] args) {
		Window window = new Window();

		intro.loadintro(window);
		
		while(true) {
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				break;
			}	
		}
	}
}
