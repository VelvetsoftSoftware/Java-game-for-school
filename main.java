public class main {

    public static int gameState = 0, firstrun = 0;

    public static void main(String[] args) {
        Window window = new Window();

        while (true) {
            // 1. Process all queued keyboard inputs for this frame
            Input.update();

            // 2. Clear draw buffers
            window.clearBuffers();

            // 3. Update active screen logic
            switch (gameState) {
                case 0:
                    intro.loadintro(window);
                    break;
                case 1:
                    mainmenu.drawmainmenu(window);
                    break;
                case 2:
					cutScene1.drawcutscene1(window);
                    break;
            }

            // 4. Sleep to lock frame rate
            utility.sleep60fps();
        }
    }
}