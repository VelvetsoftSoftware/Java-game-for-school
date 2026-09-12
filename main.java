public class main {
	//dont forget to set firstrun to 0 before the tranistion of the level
	public static int gameState = 0, firstrun = 0;
	
	public static void main(String[] args) {
		Window window = new Window();
		
		while(true) {
			switch(gameState) {
				case 0:
					intro.loadintro(window);
					break;
				case 1:
					mainmenu.drawmainmenu(window);
					break;
			}
					
			window.clearBuffers();
			utility.sleep60fps();
			for (int i = 0; i < 256; i++)
				input.pressed[i] = false;	
		}
	}
}
