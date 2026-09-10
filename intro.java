public class intro {
    public static final int[] LETTERPALETTE = {
        0x0B0B10, // 0 - background
        0x4A0E17, // 1
        0x8B1E2F, // 2
        0xD4A359, // 3
        0xF4F4F9  // 4
    };
	
	public static final int[] PALETTE_MASCCOT = {
    0x0B0B10, // 0: Transparent/Background
    0xF4F4F9, // 1: Pure White (Hair Highlights & Collars)
    0xD4A359, // 2: Bright Gold (Coat Trim & Accents)
    0x101018, // 3: Jet Black (Coat & Skirt Base)
    0x252535, // 4: Dark Slate (Coat Folds & Shading)
    0xFF2A4B, // 5: Crimson Red (Eyes & Highlights)
    0x9E1B32, // 6: Deep Crimson (Eye Shading)
    0xFFE0BD, // 7: Pale Skin
    0xF5C29B, // 8: Skin Shadow / Blush
    0x9A9AB0, // 9: Mid-Tone Hair Shadow
    0x333348, // A: Secondary Lineart
    0x000000, // B: Pure Black Outline
    0xB8860B, // C: Dark Gold / Antique Brass (Reserve Slot 1 - Trim Shadow)
    0xDCDCE5, // D: Soft White / Hair Midtone (Reserve Slot 2 - Hair Smooth Transition)
    0x6E6E85, // E: Deep Hair Shadow (Reserve Slot 3 - Hair Depth)
    0x1E1E2C  // F: Very Dark Slate (Reserve Slot 4 - Inner Fabric Shadows)
};
	
    public static final int WIDTH_TILES = 20;
	public static final int HEIGHT_TILES = 15;
	public static final int[] LETTER_BUFFER = new int[WIDTH_TILES * HEIGHT_TILES];
	public static final int[] CHARACTER_BUFFER = new int[WIDTH_TILES * HEIGHT_TILES];

	static {
		// --- 1. TEXT BUFFER ---
        int textStart = (7 * WIDTH_TILES) + 2; 

        LETTER_BUFFER[textStart]     = 1;
        LETTER_BUFFER[textStart + 1] = 2;
        LETTER_BUFFER[textStart + 2] = 3;
        LETTER_BUFFER[textStart + 3] = 1;
        LETTER_BUFFER[textStart + 4] = 2;
        LETTER_BUFFER[textStart + 5] = 4;
        LETTER_BUFFER[textStart + 6] = 5;
        LETTER_BUFFER[textStart + 7] = 6;
        LETTER_BUFFER[textStart + 8] = 7;
        LETTER_BUFFER[textStart + 9] = 4;     

        // --- 2. 4x4 CHARACTER BUFFER ---
        int charStartCol = 13; 
        int charStartRow = 4; 

        // Populate 4x4 grid using tile IDs 10 through 25
        int tileID = 8;
        for (int row = 0; row < 4; row++) {
            int rowOffset = ((charStartRow + row) * WIDTH_TILES) + charStartCol;
            for (int col = 0; col < 4; col++) {
                CHARACTER_BUFFER[rowOffset + col] = tileID++;
            }
        } 
	}	
 
    public static void loadintro(Window window) { // Added window parameter
        int xPOS = 0, yPOS = 0, scale = 1, x = 0;
        
        while(scale != 100) {
			utility.sleep60fps();
            x = 0; // Reset buffer index for each frame/scale loop
            yPOS = 0;
            while (yPOS < 240) {
                xPOS = 0;
                while(xPOS < 320) {
					if(LETTER_BUFFER[x] != 0) 
						spriteLoader.drawtexture(window, 2, LETTER_BUFFER[x], xPOS, yPOS, scale, LETTERPALETTE);//logo
					if (CHARACTER_BUFFER[x] != 0)
						spriteLoader.drawtexture(window, 3, CHARACTER_BUFFER[x], xPOS, yPOS, scale, PALETTE_MASCCOT);//background
					spriteLoader.drawtexture(window, 0, 0, xPOS, yPOS, scale, LETTERPALETTE);
                    x++;
                    xPOS += 16;
                }
                yPOS += 16;
            }
            
            // Build the screen from the layers
            window.compose();

            // Display it
            window.repaint();
            
            scale++;
        }
    }
}