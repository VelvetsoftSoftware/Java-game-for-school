public class cutScene1 {
	public static final int[][] Background_BUFFER_1 = new int[15][20];

	static {
		int tx = 0;
		for (int y = 0; y < 15; y++) {
			for (int x = 0; x < 20; x++) {
				Background_BUFFER_1[y][x] = tx;
				tx++;
			}
		}
	}
	
	public static final byte[] FIRSTTEXT = {
		// The church has always found witchcraft as an abomination and hunts us
		19, 33, 30, 72, 
		28, 33, 46, 43, 28, 33, 72, 
		33, 26, 44, 72, 
		26, 37, 48, 26, 50, 44, 72, 
		31, 40, 46, 39, 29, 72, 
		48, 34, 45, 28, 33, 28, 43, 26, 32, 45, 72, 
		26, 44, 72, 
		26, 39, 72, 
		26, 27, 40, 39, 34, 39, 26, 45, 34, 40, 39, 72, 
		26, 39, 29, 72, 
		33, 46, 39, 45, 44, 72, 
		47, 44, 66
	};
	
	public static final byte[] SECONDTEXT = {
		// The church caught our coven and killed all of my sisters. Im all thats left.
		19, 33, 30, 72,
		28, 33, 46, 43, 28, 33, 72,
		28, 26, 46, 32, 33, 45, 72,
		40, 46, 43, 72,
		28, 40, 47, 30, 39, 72,
		26, 39, 29, 72,
		36, 34, 37, 37, 30, 29, 72,
		26, 37, 37, 72,
		40, 31, 72,
		38, 50, 72,
		44, 34, 44, 45, 30, 43, 44, 66, 72,
		8, 63, 38, 72,
		26, 37, 37, 72,
		45, 33, 26, 45, 63, 44, 72,
		37, 30, 31, 45, 66
	};
	
	public static final byte[] THIRDTEXT = {
		13,40,22,72,
		26,72,
		43,34,47,26,37,72,
		28,40,47,30,39,72,
		29,30,31,34,37,30,44,72,
		45,33,30,72,
		37,34,32,33,45,72,
		1,26,26,37,72,
		32,43,26,39,45,30,29,72,
		46,44,71,72,
		27,34,43,45,33,34,39,32,72,
		26,27,40,38,34,39,26,45,34,40,39,44,72,
		26,32,26,34,39,44,45,72,
		33,34,44,72,
		39,26,38,30,66
	};
	
	public static final byte[] FOURTHTEXT = {
		19, 33, 30, 50, 72,
		38, 46, 44, 45, 72,
		29, 34, 30
	};

	private static boolean startingFadeOut = false;
	private static int runner = 0, finished = 0, cutsceneState = 0;

	public static void drawcutscene1(Window window) {
		// --- STATE LOGIC ---
		
		// State 0: Fade IN Church
		if (cutsceneState == 0) {
			drawBackground(window, cutSceneTextures1.FIRST_SLIDE_TEXTURE_background);
			window.compose();
			finished = window.fadeInOBJ(window);
			if (finished == 1) {
				runner = 0;
				cutsceneState++;
			}
			return; // Handled fade, skip bottom compose/repaint
		}
		// State 1: Hold Church
		else if (cutsceneState == 1) {
			drawBackground(window, cutSceneTextures1.FIRST_SLIDE_TEXTURE_background);
			runner++;
			if (runner >= 300) {
				runner = 0;
				cutsceneState++;
				return;
			}
		} 
		// State 2: Fade OUT Church
		else if (cutsceneState == 2) {
			drawBackground(window, cutSceneTextures1.FIRST_SLIDE_TEXTURE_background);
			window.compose();
			finished = window.fadeOutOBJ(window);
			if (finished == 1) {
				runner = 0;
				cutsceneState++;
			}
			return; // Handled fade, skip bottom compose/repaint
		} 
		// State 3: The church has always found witchcraft as an abomination and hunts us
		else if (cutsceneState == 3) {
			runner++;
			if (runner >= 660) {
				runner = 0;
				cutsceneState++;
				return;
			}
			utility.drawText(FIRSTTEXT, window);
		} 
		// State 4: Fade IN Woman Burning
		else if (cutsceneState == 4) {
			drawBackground(window, cutSceneTextures1.BURNING_SLIDE_TEXTURE_background);
			window.compose();
			finished = window.fadeInOBJ(window);
			if (finished == 1) {
				runner = 0;
				cutsceneState++;
			}
			return;
		} 
		// State 5: Hold Woman Burning
		else if (cutsceneState == 5) {
			drawBackground(window, cutSceneTextures1.BURNING_SLIDE_TEXTURE_background);
			runner++;
			if (runner >= 300) {
				runner = 0;
				cutsceneState++;
				return;
			}
		} 
		// State 6: Fade OUT Woman Burning
		else if (cutsceneState == 6) {
			drawBackground(window, cutSceneTextures1.BURNING_SLIDE_TEXTURE_background);
			window.compose();
			finished = window.fadeOutOBJ(window);
			if (finished == 1) {
				runner = 0;
				cutsceneState++;
			}
			return;
		} 
		// State 7: text: The church caught our coven and killed all of my sisters. Im all thats left
		else if (cutsceneState == 7) {
			runner++;
			if (runner >= 750) {
				runner = 0;
				cutsceneState++;
				return;
			}
			utility.drawText(SECONDTEXT, window);
		} 
		// State 8: Fade IN Abomination
		else if (cutsceneState == 8) {
			drawBackground(window, cutSceneTextures1.OBOMINATION_SLIDE_TEXTURE_background);
			window.compose();
			finished = window.fadeInOBJ(window);
			if (finished == 1) {
				runner = 0;
				cutsceneState++;
			}
			return;
		} 
		// State 9: Hold Abomination
		else if (cutsceneState == 9) {
			drawBackground(window, cutSceneTextures1.OBOMINATION_SLIDE_TEXTURE_background);
			runner++;
			if (runner >= 300) {
				runner = 0;
				cutsceneState++;
				return;
			}
		} 
		// State 10: Fade OUT Abomination
		else if (cutsceneState == 10) {
			drawBackground(window, cutSceneTextures1.OBOMINATION_SLIDE_TEXTURE_background);
			window.compose();
			finished = window.fadeOutOBJ(window);
			if (finished == 1) {
				runner = 0;
				cutsceneState++;
			}
			return;
		} 
		// State 11: text: Now, a rival coven defiles the light Baal granted us, birthing abominations against his name.
		else if (cutsceneState == 11) {
			runner++;
			if (runner >= 750) {
				runner = 0;
				cutsceneState++;
				return;
			}
			utility.drawText(THIRDTEXT, window);
		} 
		// State 12: Text 4 ("They must die")
		else if (cutsceneState == 12) {
			runner++;
			if (runner >= 180) {
				startingFadeOut = true;
			}
			utility.drawText(FOURTHTEXT, window);
		}

		// --- RENDERING PIPELINE FOR NON-FADE STATES ---
		if (startingFadeOut) {
			window.fadeOUT(window);
		} else {
			window.compose();
			window.repaint();
		}
	}

	private static void drawBackground(Window window, byte[][] texture) {
		for (int y = 0; y < 15; y++) {
			for (int x = 0; x < 20; x++) {
				spriteLoader.drawtexture(window, 0, texture, Background_BUFFER_1[y][x], x * 16, y * 16, 100, cutSceneTextures1.BACKGROUND_PALETTES);
			}
		}
	}
}