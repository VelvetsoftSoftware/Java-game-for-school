public class mainmenu {
	//the plan is for a paralax image in the center
	//this will take up 1 and 2
	// 3 will be an ornate scrollwork
	// 4 will be name of game, buttons im using 2 colors on name, i will have 8 for buttons 4 unhighlighted and 4 highlighted
	// giving me 6 more to play with
	// 5 will be any extra art
    public static final int[][] BACKGROUND_BUFFER = new int[17][22];

    static {
        for (int y = 0; y < 17; y++) {
            for (int x = 0; x < 22; x++) {
                BACKGROUND_BUFFER[y][x] = mainMenuTextures.BACKGROUND_TILES[(y & 1) * 2 + (x & 1)];
            }
        }
    }
	
	public static final int[][] BUFFER3 = new int[15][20];

	static {
		for (int y = 0; y < 15; y++) {
			for (int x = 0; x < 20; x++) {

				if (y < 4) {
					int tx = x & 1;
					int ty = y & 1;

					BUFFER3[y][x] = mainMenuTextures.SCROLLWORK_TILES[ty * 2 + tx];

				} else if (y == 4) {
					BUFFER3[y][x] = 28;
				// Fix the bottom border initialization in static {}
				} else if(y == 12) {
					BUFFER3[y][x] = 29;
				} else if (y >= 13) {
					int tx = x & 1;
					// Align parity with ty = (y - 13) & 1 so row 13 gets ty=0 and row 14 gets ty=1
					int ty = (y - 13) & 1;

					BUFFER3[y][x] = mainMenuTextures.SCROLLWORK_TILES[ty * 2 + tx];
				} else {
					BUFFER3[y][x] = 27;
				}
			}
		}
	}

	static int camX = 0, camY = 0;
    private static boolean startingFadeOut = false;

    public static void drawmainmenu(Window window) {
		//Synth.play(Synth.LEVEL2);
		
        // Check input
        if ((Input.pressed(java.awt.event.KeyEvent.VK_SPACE) || Input.pressed(java.awt.event.KeyEvent.VK_ENTER))) {
            startingFadeOut = true;
        }

        camX++;
        camY++;
        if (camX > 32) camX = 0;
        if (camY > 32) camY = 0;

        // Render scene
        utility.scrollmap(BACKGROUND_BUFFER, camX, camY, 100, mainMenuTextures.BACKGROUNDPALETTEMAINMENU, 0, window);
        spriteLoader.drawtexture(window, 1, mainMenuTextures.MAINMENUTEXTURES, 4, 144, 120, 100, mainMenuTextures.BUTTONUNSELECTEDPALETTEMAINMENU);
		spriteLoader.drawtexture(window, 1, mainMenuTextures.MAINMENUTEXTURES, 5, 160, 120, 100, mainMenuTextures.BUTTONUNSELECTEDPALETTEMAINMENU);
		
		//title
		int temp = 0;
		for(int x = 16; temp < mainMenuTextures.MAIN_TITLE_TILES.length; x += 16) {
			spriteLoader.drawtexture(window, /*layer*/4, mainMenuTextures.MAINMENUTEXTURES, /*texure*/mainMenuTextures.MAIN_TITLE_TILES[temp], x, 3, 100, mainMenuTextures.TITLECARDAOFGAME);
			temp++;
		}
		//sub title
		temp = 0;
		int y = 19;
		for(int x = 16; temp < mainMenuTextures.SUB_TITLE_TILES.length; x += 16) {
			if(temp == 9) {
				x = 16;
				y = 35;
			}
			spriteLoader.drawtexture(window, /*layer*/4, mainMenuTextures.MAINMENUTEXTURES, /*texure*/mainMenuTextures.SUB_TITLE_TILES[temp], x, y, 100, mainMenuTextures.TITLECARDAOFGAME);
			temp++;
		}
		//is the scroll work
		for (int ty = 0; ty < BUFFER3.length; ty++) {
			for (int tx = 0; tx < BUFFER3[ty].length; tx++) {
				spriteLoader.drawtexture(window, 3, mainMenuTextures.MAINMENUTEXTURES, BUFFER3[ty][tx], tx * 16, ty * 16, 100, mainMenuTextures.SCROLLWORK);
			}
		}
		

        // State rendering
        if (startingFadeOut) {
			//Synth.stop();
            window.fadeOUT(window);
        } else if (main.firstrun != 1) {
            window.fadeIN(window);
        } else {
            window.compose();
            window.repaint();
        }
    }
}