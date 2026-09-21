public class mainmenu {

    public static final int[] BACKGROUNDPALETTEMAINMENU = {
		0xFF4FA3,   // Pink
        0xF28C28  // Orange
    };

    public static final int[] BUTTONUNSELECTEDPALETTEMAINMENU = {
        0x0B0B10,
        0x4A0E17,
        0x8B1E2F,
        0xD4A359,
        0xF4F4F9
    };

    public static final int[] BACKGROUND_TILES = {
        24, 25, 26, 27
    };

    public static final int[][] BACKGROUND_BUFFER = new int[17][22];

    static {
        for (int y = 0; y < 17; y++) {
            for (int x = 0; x < 22; x++) {
                BACKGROUND_BUFFER[y][x] = BACKGROUND_TILES[(y & 1) * 2 + (x & 1)];
            }
        }
    }

	static int camX = 0, camY = 0;
    private static boolean startingFadeOut = false;

    public static void drawmainmenu(Window window) {
		Synth.play(Synth.LEVEL2);
		
        // Check input
        if ((Input.pressed(java.awt.event.KeyEvent.VK_SPACE) || Input.pressed(java.awt.event.KeyEvent.VK_ENTER))) {
            startingFadeOut = true;
        }

        camX++;
        camY++;
        if (camX > 32) camX = 0;
        if (camY > 32) camY = 0;

        // Render scene
        utility.scrollmap(BACKGROUND_BUFFER, camX, camY, 100, BACKGROUNDPALETTEMAINMENU, 0, window);
        spriteLoader.drawtexture(window, 1, 28, 144, 120, 100, BUTTONUNSELECTEDPALETTEMAINMENU);
        spriteLoader.drawtexture(window, 1, 29, 160, 120, 100, BUTTONUNSELECTEDPALETTEMAINMENU);

        // State rendering
        if (startingFadeOut) {
			Synth.stop();
            window.fadeOUT(window);
        } else if (main.firstrun != 1) {
            window.fadeIN(window);
        } else {
            window.compose();
            window.repaint();
        }
    }
}
