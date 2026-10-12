import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Input {

    private static final boolean[] keys = new boolean[65536];
    public static final boolean[] pressed = new boolean[65536];
    
    // Thread-safe queue to capture key presses across threads
    private static final ConcurrentLinkedQueue<Integer> pressQueue = new ConcurrentLinkedQueue<>();

    static {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                int k = e.getKeyCode();
                if (k >= 0 && k < keys.length) {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        if (!keys[k]) {
                            pressQueue.add(k); // Store press event safely
                        }
                        keys[k] = true;
                    } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                        keys[k] = false;
                    }
                }
                return false;
            }
        });
    }

    public static boolean down(int key) {
        return key >= 0 && key < keys.length && keys[key];
    }

    public static boolean pressed(int key) {
        return key >= 0 && key < pressed.length && pressed[key];
    }

    // Call this ONCE at the start of every frame in your game loop
    public static void update() {
        // Reset old single-frame states
        for (int i = 0; i < pressed.length; i++) {
            pressed[i] = false;
        }

        // Drain queued presses recorded since last frame
        Integer k;
        while ((k = pressQueue.poll()) != null) {
            pressed[k] = true;
        }
    }
}