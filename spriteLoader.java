public class spriteLoader {	
	
	/////////////////////
	// buffer
	//0: backround 1
	//1: background 2
	//2: playfield 
	//3: forground 1
	//4: forground 2
	////////////////////
	//scale
	//100 = 1.00x
	//50 = 0.50x
	//200 = 2.00x
	//10 ≈ 0.10x
	/////////////////////
	
	public static void drawtexture( Window window, int buffer, int textureid, int x, int y, int scale, int[] PALETTE) {

		byte[] texture = Textures.TEXTURES[textureid];

		int width = mathlib.Divider(16 * scale, 100);
		int height = mathlib.Divider(16 * scale, 100);

		if (width < 1 || height < 1)
			return;

		for (int py = 0; py < height; py++) {
			int srcY = mathlib.Divider(py * 100, scale);
			for (int px = 0; px < width; px++) {
				int srcX = mathlib.Divider(px * 100, scale);

				if (srcX >= 16 || srcY >= 16)
					continue;

				int pixel = srcY * 16 + srcX;
				int data = texture[pixel >> 1] & 0xFF;
				int colorIndex;

				if ((pixel & 1) == 0)
					colorIndex = data >> 4;
				else
					colorIndex = data & 0x0F;

				int color = PALETTE[colorIndex];
				int destX = x + px;
				int destY = y + py;

				if (destX >= 0 && destX < 320 && destY >= 0 && destY < 240) {
					window.setPixel(buffer, destX, destY, color);
				}
			}
		}
	}
}

