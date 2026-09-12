public class mainmenu {
	
	public static final int[] BACKGROUNDPALETTEMAINMENU = {
        0xF28C28,  // Orange
		0xFF4FA3  // Pink
    };
	
	public static final int[] BUTTONUNSELECTEDPALETTEMAINMENU = {
        0x0B0B10,
		0x4A0E17,
		0x8B1E2F,
		0xD4A359,
		0xF4F4F9
    };
	
	//336x256 or 21 tiles by 16 tiles
	public static final int[] BACKGROUND_TILES = {
		23, 24, 25, 26
	};

	public static final int[][] BACKGROUND_BUFFER = new int[17][22];

	static {
		for(int y = 0; y < 17; y++) {
			for(int x = 0; x < 22; x++) {
				BACKGROUND_BUFFER[y][x] =
					BACKGROUND_TILES[(y & 1) * 2 + (x & 1)];
			}
		}
	}
	
	static int camX = 0, camY = 0;
	
	public static void drawmainmenu(Window window) {
		camX++;
		camY++;
			
		if(camX > 32)
			camX = 0;
			
		if(camY > 32)
			camY = 0;
			
		if(main.firstrun != 1) {
			utility.scrollmap( BACKGROUND_BUFFER, camX, camY, 100, BACKGROUNDPALETTEMAINMENU, 0, window);
			window.fadeIN(window);
		}
	
		if(main.firstrun == 1) {
			utility.scrollmap(BACKGROUND_BUFFER, camX, camY, 100, BACKGROUNDPALETTEMAINMENU, 0, window);
		
		spriteLoader.drawtexture(window, 1, 27, 144, 120, 100, BUTTONUNSELECTEDPALETTEMAINMENU);
		spriteLoader.drawtexture(window, 1, 28, 160, 120, 100, BUTTONUNSELECTEDPALETTEMAINMENU);
			
		window.compose();
        window.repaint();
		}
	}
}