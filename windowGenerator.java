import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Window extends JPanel {

    BufferedImage background1;
	BufferedImage background2;
	BufferedImage playground;
	BufferedImage foreground1;
	BufferedImage foreground2;

	BufferedImage screen;
	
    int scale = 1;

    private JFrame frame;
    private JComboBox<String> resolutionBox;

    public Window() {

        background1 = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
		background2 = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
		playground = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
		foreground1 = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);
		foreground2 = new BufferedImage(320, 240, BufferedImage.TYPE_INT_ARGB);

		screen = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);

        frame = new JFrame("Test Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Resolution dropdown text
        resolutionBox = new JComboBox<>(new String[] {
            "320 x 240 1x",
            "640 x 480 2x",
            "960 x 720 3x",
            "1280 x 960 4x",
            "1600 x 1200 5x"
        });

        resolutionBox.addActionListener(e -> {
            int selected = resolutionBox.getSelectedIndex();
            scale = selected + 1;

            frame.pack();
            frame.setLocationRelativeTo(null);

            repaint();
        });

        // Top menu
        JPanel menu = new JPanel();
        menu.add(new JLabel("Resolution:"));
        menu.add(resolutionBox);

        // Put menu above game
        JPanel container = new JPanel(new BorderLayout());
        container.add(menu, BorderLayout.NORTH);
        container.add(this, BorderLayout.CENTER);

        frame.setContentPane(container);

        try {
            Image icon = ImageIO.read(new File("icon16.png"));
            frame.setIconImage(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(
            320 * scale,
            240 * scale
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        g2.drawImage(
            screen,
            0,
            0,
            320 * scale,
            240 * scale,
            null
        );
    }

    public void setPixel(int buffer, int x, int y, int color) {
		color |= 0xFF000000;
		
		switch (buffer) {
			case 0:
				background1.setRGB(x, y, color);
				break;
			case 1:
				background2.setRGB(x, y, color);
				break;
			case 2:
				playground.setRGB(x, y, color);
				break;
			case 3:
				foreground1.setRGB(x, y, color);
				break;
			case 4:
				foreground2.setRGB(x, y, color);
				break;
		}
	}
	
	public void compose() {
		Graphics2D g = screen.createGraphics();
		
		g.drawImage(background1, 0, 0, null);
		g.drawImage(background2, 0, 0, null);
		g.drawImage(playground, 0, 0, null);
		g.drawImage(foreground1, 0, 0, null);
		g.drawImage(foreground2, 0, 0, null);

		g.dispose();
	}
}
