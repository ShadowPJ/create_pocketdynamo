import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Draws the mod logo (a copper gear with a lightning bolt) on a 32x32 pixel grid and scales it up. The default scale
 * of 4 gives the 128x128 logo of the mod; any other integer gives a larger icon without losing sharpness.
 * <p>
 * Run from the project root with a plain JDK 21, no dependencies:
 * <pre>java tools/LogoGen.java src/main/resources/logo.png
 * java tools/LogoGen.java docs/store/icon-512.png 16</pre>
 * The art is original to this project. A hand-made logo can replace the PNG at any time; this generator is then no
 * longer needed, and it must not be run on top of hand edits.
 */
public class LogoGen {
	static final int G = 32;
	static final int[] COPPER = { 0x4a2210, 0x8a4a28, 0xc87340, 0xe8985f, 0xffc896 };

	static int shade(int[] p, double lit) { // lit in [-1, 1]
		if (lit > 0.55) return p[4];
		if (lit > 0.15) return p[3];
		if (lit > -0.3) return p[2];
		if (lit > -0.65) return p[1];
		return p[0];
	}

	static boolean inBolt(double x, double y) {
		// Lightning bolt as a polygon in grid coordinates.
		double[][] poly = { { 17.5, 7.5 }, { 11.0, 17.5 }, { 15.0, 17.5 }, { 13.5, 25.0 }, { 21.0, 14.5 }, { 16.8, 14.5 }, { 19.5, 7.5 } };
		boolean in = false;
		for (int i = 0, j = poly.length - 1; i < poly.length; j = i++)
			if ((poly[i][1] > y) != (poly[j][1] > y)
				&& x < (poly[j][0] - poly[i][0]) * (y - poly[i][1]) / (poly[j][1] - poly[i][1]) + poly[i][0])
				in = !in;
		return in;
	}

	public static void main(String[] a) throws Exception {
		int SCALE = a.length > 1 ? Integer.parseInt(a[1]) : 4;
		int[][] grid = new int[G][G]; // 0 = transparent, else 0xRRGGBB (alpha implied)
		double cx = 15.5, cy = 15.5;
		int teeth = 8;
		for (int y = 0; y < G; y++)
			for (int x = 0; x < G; x++) {
				double dx = x + 0.5 - 16, dy = y + 0.5 - 16;
				double r = Math.hypot(dx, dy);
				// Background: a dark steel rounded square with a soft vertical gradient.
				double rx = Math.abs(dx) - 14.5, ry = Math.abs(dy) - 14.5;
				boolean inSquare = Math.max(rx, ry) < 1.5 && !(rx > -2.5 && ry > -2.5 && Math.hypot(rx + 2.5, ry + 2.5) > 3.2);
				if (inSquare) {
					int g = 0x20 + (int) (y * 0.9);
					grid[y][x] = (g << 16) | ((g + 4) << 8) | (g + 12);
					if (Math.max(Math.abs(dx), Math.abs(dy)) > 14.2)
						grid[y][x] = 0x14161a;
				}
				// Gear: teeth around a body ring.
				double ang = Math.atan2(dy, dx);
				double tooth = Math.cos(ang * teeth);
				double outer = 10.2 + (tooth > 0.2 ? 2.6 : 0.0);
				boolean gear = r < outer && r > 6.4;
				if (gear) {
					double lit = (-dx - dy) / Math.max(r, 0.001) / Math.sqrt(2);
					int c = shade(COPPER, lit);
					if (r > outer - 1.0 || r < 7.4)
						c = (r < 7.4) ? COPPER[0] : shade(COPPER, lit - 0.6);
					grid[y][x] = c;
				}
				// Dark dial inside the ring.
				if (r <= 6.4 && inSquare)
					grid[y][x] = 0x0d0f12;
			}
		// Bolt: bright amber with a lit upper-left edge.
		for (int y = 0; y < G; y++)
			for (int x = 0; x < G; x++)
				if (inBolt(x + 0.5, y + 0.5)) {
					boolean edge = !inBolt(x - 0.5, y + 0.5) || !inBolt(x + 0.5, y - 0.5);
					grid[y][x] = edge ? 0xffe9a8 : 0xf2a62e;
					if (!inBolt(x + 1.5, y + 0.5) || !inBolt(x + 0.5, y + 1.5))
						grid[y][x] = 0xc9791a;
				}
		BufferedImage out = new BufferedImage(G * SCALE, G * SCALE, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < G * SCALE; y++)
			for (int x = 0; x < G * SCALE; x++) {
				int c = grid[y / SCALE][x / SCALE];
				out.setRGB(x, y, c == 0 ? 0 : 0xFF000000 | c);
			}
		ImageIO.write(out, "png", new File(a[0]));
		System.out.println("wrote " + a[0]);
	}
}
