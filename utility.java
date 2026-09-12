import java.awt.event.KeyEvent;

public class utility {
	
	public static void sleep60fps() {
		try {
			Thread.sleep(16);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}	
	}
	
	public static void button(int maxbuttons) {
		int buttonpossition = 0;
		
		if (input.down(KeyEvent.VK_UP))
			buttonpossition++;
		if (input.down(KeyEvent.VK_DOWN))
			buttonpossition--;
		if (input.down(KeyEvent.VK_W))
			buttonpossition++;
		if (input.down(KeyEvent.VK_S))
			buttonpossition--;
		
		if(buttonpossition > maxbuttons)
			buttonpossition = 0;
		if(buttonpossition < 0)
			buttonpossition = maxbuttons;
	}
	
	public static void scrollmap(int map[][], int cameraX, int cameraY, int scale, int[] pallete, int layer, Window window) {
		for (int ty = 0; ty < map.length; ty++) {
			for (int tx = 0; tx < map[ty].length; tx++) {
				int x = tx * 16 - cameraX;
				int y = ty * 16 - cameraY;
				spriteLoader.drawtexture(window, layer, map[ty][tx], x, y, scale, pallete);
			}
		}
	}
}
