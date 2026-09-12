import java.awt.event.*;

public class input implements KeyListener {

    private static final boolean[] keys = new boolean[256];
	public static final boolean[] pressed = new boolean[256];

    public static boolean down(int key) {
        return key < 256 && keys[key];
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k < 256) keys[k] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k < 256) keys[k] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}