import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.Raster;
import java.awt.image.ColorModel;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Window extends JPanel {

    int[] background1;
    int[] background2;
    int[] playground;
    int[] foreground1;
    int[] foreground2;

    int[] screen;
    BufferedImage screenImage;

    int scale = 2;

    private JFrame frame;
    private JComboBox<String> resolutionBox;

    public Window() {

        background1 = new int[320 * 240];
        background2 = new int[320 * 240];
        playground = new int[320 * 240];
        foreground1 = new int[320 * 240];
        foreground2 = new int[320 * 240];

        screen = new int[320 * 240];
		java.util.Arrays.fill(screen, 0xFF000000);

        // Make BufferedImage use screen[] directly
        DataBufferInt dataBuffer = new DataBufferInt(screen, screen.length);

		int[] masks = {
			0x00FF0000,
			0x0000FF00,
			0x000000FF,
			0xFF000000
		};

		SinglePixelPackedSampleModel sampleModel = new SinglePixelPackedSampleModel( DataBuffer.TYPE_INT, 320, 240, masks);
		WritableRaster raster = Raster.createWritableRaster( sampleModel, dataBuffer, null);
		
		screenImage = new BufferedImage(ColorModel.getRGBdefault(), raster, false, null);
        screenImage = new BufferedImage( ColorModel.getRGBdefault(), raster, false, null);

        frame = new JFrame("Test Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        resolutionBox = new JComboBox<>(new String[] {
            "320 x 240 1x",
            "640 x 480 2x",
            "960 x 720 3x",
            "1280 x 960 4x",
            "1600 x 1200 5x"
        });

        resolutionBox.setFocusable(false);

        resolutionBox.addActionListener(e -> {

            int selected = resolutionBox.getSelectedIndex();
            scale = selected + 1;

            frame.pack();
            frame.setLocationRelativeTo(null);

            repaint();

            SwingUtilities.invokeLater(() -> {
                this.requestFocusInWindow();
            });
        });

        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocusInWindow();
            }
        });

        JPanel menu = new JPanel();
        menu.add(new JLabel("Resolution:"));
        menu.add(resolutionBox);

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

        this.setFocusable(true);
		this.setBackground(Color.BLACK);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        this.requestFocusInWindow();
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

        g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(screenImage, 0, 0, 320 * scale, 240 * scale, null );
    }

	public void setPixel(int buffer, int x, int y, int color) {
		color |= 0xFF000000;
		int index = y * 320 + x;

		switch (buffer) {
			case 0:
				background1[index] = color;
				break;
			case 1:
				background2[index] = color;
				break;
			case 2:
				playground[index] = color;
				break;
			case 3:
				foreground1[index] = color;
				break;
			case 4:
				foreground2[index] = color;
				break;
		}
	}

    public void compose() {
        int pixels = 320 * 240;

        for (int i = 0; i < pixels; i++) {
            int color = background1[i];

            if (background2[i] != 0)
                color = background2[i];

            if (playground[i] != 0)
                color = playground[i];

            if (foreground1[i] != 0)
                color = foreground1[i];

            if (foreground2[i] != 0)
                color = foreground2[i];

            screen[i] = color;
        }
    }

    public void clearBuffers() {
        java.util.Arrays.fill(background1, 0);
        java.util.Arrays.fill(background2, 0);
        java.util.Arrays.fill(playground, 0);
        java.util.Arrays.fill(foreground1, 0);
        java.util.Arrays.fill(foreground2, 0);
    }

    private void fade(int alpha) {
		int amount = 255 - alpha;

		for (int i = 0; i < screen.length; i++) {
			int pixel = screen[i];
			int r = ((pixel >> 16) & 0xFF) * amount / 255;
			int g = ((pixel >> 8) & 0xFF) * amount / 255;
			int b = (pixel & 0xFF) * amount / 255;
			screen[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
		}
	}

    static int alpha = 0;
    public void fadeOUT(Window window) {
        if (alpha <= 255) {
            fade(alpha);
            window.repaint();
            alpha += 5;
        }
        if (alpha == 255) {
            alpha = 0;
			main.firstrun = 0;
            main.gameState++;
        }
    }

    static int alpha1 = 255;
    public void fadeIN(Window window) {
        if (alpha1 >= 0) {
            compose();
            fade(alpha1);
            window.repaint();
            alpha1 -= 5;
        }
        if (alpha1 == 0) {
            alpha1 = 255;
            main.firstrun = 1;
        }
    }
	
	static int alpha2 = 0;
	public int fadeOutOBJ(Window window) {
        if (alpha2 <= 255) {
			fade(alpha2);
			window.repaint();
	
            alpha2 += 5;
        }
        if (alpha2 >= 255) {
            alpha2 = 0;
			return 1;
        }
		return 0;
    }

    static int alpha3 = 255;
    public int fadeInOBJ(Window window) {
        if (alpha3 >= 0) {
            compose();
            fade(alpha3);
            window.repaint();
            alpha3 -= 5;
        }
        if (alpha3 == 0) {
            alpha3 = 255;
			return 1;
        }
		return 0;
    }
}