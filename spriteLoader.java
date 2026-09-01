public class spriteLoader {
	
	private static final byte[][][] PALLETTABLE = new byte[16, 3, 16] {
	// Palette 0
	{
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
    // Palette 1
    {
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
	// Palette 2
	{
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
    // Palette 3
    {
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
	// Palette 4
	{
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
    // Palette 5
    {
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
	// Palette 6
	{
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
    // Palette 7
    {
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
	// Palette 8
	{
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
    // Palette 9
    {
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
	// Palette 10
	{
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
    // Palette 11
    {
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
	// Palette 12
	{
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
    // Palette 13
    {
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
	// Palette 14
	{
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    },
    // Palette 15
    {
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // R
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }, // G
        { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }  // B
    }	
	};
	
	public static final byte[][] ATLAS = Textures.TEXTURES;
	
	private byte pallet, palletSet, palleteanddata;
	private Texture2D texture;
	private Color32[] pixels;
	
	
	public static void load() {
        for (int i = 0; i < Textures.TEXTURES.length; i++) {
            System.arraycopy(
                Textures.TEXTURES[i],
                0,
                atlas[i],
                0,
                128
            );
        }
    }
	}
	
	private bool getmetadata(BinaryReader reader) {
		palleteanddata = reader.ReadByte();
		byte temp = palleteanddata;
		palletSet = (byte)(temp >> 4);

		return true;
	}
	
	private void drawimage(BinaryReader reader) {
		int totalPixels = 256, pixel = 1;
		
		palleteanddata = (byte)(palleteanddata & 0x0F);
		
		pixels[0] = new Color32(
			PALLETTABLE[palletSet, 0, palleteanddata], 
			PALLETTABLE[palletSet, 1, palleteanddata],
			PALLETTABLE[palletSet, 2, palleteanddata],
			255
		);
		
		while(pixel < totalPixels) {
			byte packedByte = reader.ReadByte();

			// 1st Pixel: High Nibble (Upper 4 bits)
			byte highNibble = (byte)(packedByte >> 4);
			pixels[pixel] = new Color32(
				PALLETTABLE[palletSet, 0, highNibble],
				PALLETTABLE[palletSet, 1, highNibble],
				PALLETTABLE[palletSet, 2, highNibble],
				255
			);
			pixel++;
		
			// 2nd Pixel: Low Nibble (Lower 4 bits)
			if (pixel < totalPixels) {
				byte lowNibble = (byte)(packedByte & 0x0F);
				pixels[pixel] = new Color32(
					PALLETTABLE[palletSet, 0, lowNibble],
					PALLETTABLE[palletSet, 1, lowNibble],
					PALLETTABLE[palletSet, 2, lowNibble],
					255
				);
				pixel++;
			}
		}
	}
}
