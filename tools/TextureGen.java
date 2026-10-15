import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Draws the draft block textures of the mod pixel by pixel, so they can be regenerated and tweaked in code.
 * <p>
 * Run from the project root with a plain JDK 21, no dependencies:
 * <pre>java tools/TextureGen.java src/main/resources/assets/create_pocketdynamo/textures/block</pre>
 * Writes the 12 block textures (16x16). The art is original to this project. A hand-made set can replace these files
 * at any time; this generator is then no longer needed.
 */
public class TextureGen {
	// Palettes: dark, mid, base, light, highlight (0xRRGGBB)
	static final int[] COPPER = { 0x5e2d16, 0x9c5530, 0xc87340, 0xe8985f, 0xffc896 };
	static final int[] BRASS = { 0x5c4719, 0x967a28, 0xcfae44, 0xecd070, 0xfff0ae };
	static final int[] DIAMOND = { 0x0c4a56, 0x23909c, 0x3fc4cc, 0x7ae6ea, 0xd2fbff };
	static final int[] STEEL = { 0x14161a, 0x2a2d34, 0x3d414a, 0x5a5f6b, 0x8a909c };

	static BufferedImage img() {
		return new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	}

	static void px(BufferedImage b, int x, int y, int rgb) {
		if (x >= 0 && y >= 0 && x < 16 && y < 16)
			b.setRGB(x, y, 0xFF000000 | rgb);
	}

	static void rect(BufferedImage b, int x, int y, int w, int h, int rgb) {
		for (int j = y; j < y + h; j++)
			for (int i = x; i < x + w; i++)
				px(b, i, j, rgb);
	}

	/** Metal plate: dark outline, lit top-left edge, shaded bottom-right edge, rivets in the corners. */
	static void plate(BufferedImage b, int[] p) {
		rect(b, 0, 0, 16, 16, p[2]);
		rect(b, 0, 0, 16, 1, p[0]);
		rect(b, 0, 15, 16, 1, p[0]);
		rect(b, 0, 0, 1, 16, p[0]);
		rect(b, 15, 0, 1, 16, p[0]);
		rect(b, 1, 1, 14, 1, p[3]);
		rect(b, 1, 1, 1, 14, p[3]);
		rect(b, 2, 14, 13, 1, p[1]);
		rect(b, 14, 2, 1, 13, p[1]);
		for (int[] r : new int[][] { { 3, 3 }, { 12, 3 }, { 3, 12 }, { 12, 12 } }) {
			px(b, r[0], r[1], p[4]);
			px(b, r[0] + 1, r[1] + 1, p[0]);
		}
	}

	/**
	 * Body of the motor. The model reads a 12x10 region at the top-left of the texture (the long sides, rotated for
	 * the top and bottom) and its left 10x10 part for the back cap, so the plate is drawn there at 1:1 scale.
	 */
	static BufferedImage motorBody(int[] p) {
		BufferedImage b = img();
		rect(b, 0, 0, 16, 16, p[2]);
		// 12x10 plate: dark outline, lit top-left edge, shaded bottom-right edge.
		rect(b, 0, 0, 12, 1, p[0]);
		rect(b, 0, 9, 12, 1, p[0]);
		rect(b, 0, 0, 1, 10, p[0]);
		rect(b, 11, 0, 1, 10, p[0]);
		rect(b, 1, 1, 10, 1, p[3]);
		rect(b, 1, 1, 1, 8, p[3]);
		rect(b, 2, 8, 9, 1, p[1]);
		rect(b, 10, 2, 1, 7, p[1]);
		// Rivets at the corners, cooling slats in the middle.
		for (int[] r : new int[][] { { 2, 2 }, { 9, 2 }, { 2, 7 }, { 9, 7 } })
			px(b, r[0], r[1], p[4]);
		for (int y : new int[] { 3, 5 }) {
			rect(b, 3, y, 6, 1, p[0]);
			rect(b, 3, y + 1, 6, 1, p[3]);
		}
		return b;
	}

	/** Front face (the output end). The model crops its centre 10x10, so the face is drawn inside (3..12, 3..12). */
	static BufferedImage motorFront() {
		BufferedImage b = img();
		rect(b, 0, 0, 16, 16, STEEL[2]);
		rect(b, 3, 3, 10, 10, STEEL[2]);
		rect(b, 3, 3, 10, 1, STEEL[0]);
		rect(b, 3, 12, 10, 1, STEEL[0]);
		rect(b, 3, 3, 1, 10, STEEL[0]);
		rect(b, 12, 3, 1, 10, STEEL[0]);
		rect(b, 4, 4, 8, 1, STEEL[3]);
		rect(b, 4, 4, 1, 8, STEEL[3]);
		rect(b, 5, 11, 7, 1, STEEL[1]);
		rect(b, 11, 5, 1, 7, STEEL[1]);
		// Gauge: dark dial with an amber ring (the battery) and a needle.
		for (int y = 0; y < 16; y++)
			for (int x = 0; x < 16; x++) {
				double d = Math.hypot(x + 0.5 - 8.0, y + 0.5 - 8.0);
				if (d < 2.3)
					px(b, x, y, 0x0d0f12);
				else if (d < 3.4)
					px(b, x, y, 0xe8a23a);
			}
		px(b, 8, 8, 0xffd98a);
		px(b, 9, 7, 0xffd98a);
		// Rivets in the corners of the face.
		for (int[] r : new int[][] { { 4, 4 }, { 11, 4 }, { 4, 11 }, { 11, 11 } })
			px(b, r[0], r[1], STEEL[4]);
		return b;
	}

	/**
	 * The battery pack on the motor. Regions the model uses: top 6x8 at (0,0), long sides 8x2 at (6,0) and short ends
	 * 6x2 at (0,8).
	 */
	static BufferedImage motorBattery() {
		BufferedImage b = img();
		rect(b, 0, 0, 16, 16, STEEL[1]);
		// Top: three charge cells (green), the case around them.
		rect(b, 0, 0, 6, 8, STEEL[1]);
		rect(b, 0, 0, 6, 1, STEEL[0]);
		rect(b, 0, 7, 6, 1, STEEL[0]);
		rect(b, 0, 0, 1, 8, STEEL[0]);
		rect(b, 5, 0, 1, 8, STEEL[0]);
		for (int y : new int[] { 1, 3, 5 }) {
			rect(b, 1, y, 4, 2, 0x2fae58);
			rect(b, 1, y, 4, 1, 0x6be08f);
		}
		// Sides: lit top edge, dark bottom edge, an amber stripe in the middle.
		rect(b, 6, 0, 8, 1, STEEL[3]);
		rect(b, 6, 1, 8, 1, STEEL[0]);
		rect(b, 6, 0, 8, 1, STEEL[3]);
		rect(b, 8, 1, 4, 1, 0xe8a23a);
		// Ends.
		rect(b, 0, 8, 6, 1, STEEL[3]);
		rect(b, 0, 9, 6, 1, STEEL[0]);
		return b;
	}

	static BufferedImage solarTop(int[] frame, int[] cell) {
		BufferedImage b = img();
		rect(b, 0, 0, 16, 16, frame[2]);
		rect(b, 0, 0, 16, 1, frame[0]);
		rect(b, 0, 15, 16, 1, frame[0]);
		rect(b, 0, 0, 1, 16, frame[0]);
		rect(b, 15, 0, 1, 16, frame[0]);
		// Four cells, each a darker blue with a lighter diagonal glint.
		int[][] origins = { { 2, 2 }, { 9, 2 }, { 2, 9 }, { 9, 9 } };
		for (int[] o : origins) {
			rect(b, o[0], o[1], 5, 5, cell[0]);
			rect(b, o[0], o[1], 5, 1, cell[1]);
			for (int k = 0; k < 4; k++)
				px(b, o[0] + k, o[1] + 4 - k, cell[2]);
			px(b, o[0] + 1, o[1] + 1, cell[3]);
		}
		// Frame bars between the cells and a lit top-left edge.
		rect(b, 7, 1, 2, 14, frame[3]);
		rect(b, 1, 7, 14, 2, frame[3]);
		rect(b, 1, 1, 14, 1, frame[4]);
		rect(b, 1, 1, 1, 14, frame[4]);
		rect(b, 7, 7, 2, 2, frame[1]);
		return b;
	}

	/** The panel is 4 pixels tall and reads rows 12..15 of this texture, so the design lives there. */
	static BufferedImage solarSide(int[] p) {
		BufferedImage b = img();
		rect(b, 0, 0, 16, 16, p[2]);
		rect(b, 0, 12, 16, 1, p[3]);
		rect(b, 0, 15, 16, 1, p[0]);
		rect(b, 0, 14, 16, 1, p[1]);
		for (int x : new int[] { 2, 8, 13 })
			px(b, x, 13, p[4]);
		return b;
	}

	/**
	 * The cable models read a plus shape: the 6x6 core at (5..10, 5..10) and one 5x6 strip per arm (east x 11..15,
	 * west x 0..4, up y 0..4, down y 11..15). Each strip has a dark ring where it meets the core and at its tip.
	 */
	static BufferedImage cable() {
		BufferedImage b = img();
		int[] p = COPPER;
		int ring = 0x7a3e24;
		rect(b, 0, 0, 16, 16, p[2]);
		// Core: copper with a lit top edge, a shaded bottom edge and a bright centre.
		rect(b, 5, 5, 6, 1, p[3]);
		rect(b, 5, 10, 6, 1, p[1]);
		rect(b, 7, 7, 2, 2, p[3]);
		// East and west strips (5 wide, 6 tall).
		for (int[] s : new int[][] { { 11, 15 }, { 4, 0 } }) {
			rect(b, Math.min(s[0], s[1]), 5, 5, 1, p[3]);
			rect(b, Math.min(s[0], s[1]), 10, 5, 1, p[1]);
			rect(b, s[0], 5, 1, 6, ring);
			rect(b, s[1], 5, 1, 6, p[0]);
		}
		// Up and down strips (6 wide, 5 tall).
		for (int[] s : new int[][] { { 4, 0 }, { 11, 15 } }) {
			rect(b, 5, Math.min(s[0], s[1]), 1, 5, p[3]);
			rect(b, 10, Math.min(s[0], s[1]), 1, 5, p[1]);
			rect(b, 5, s[0], 6, 1, ring);
			rect(b, 5, s[1], 6, 1, p[0]);
		}
		return b;
	}

	static void save(BufferedImage b, File dir, String name) throws Exception {
		ImageIO.write(b, "png", new File(dir, name + ".png"));
	}

	public static void main(String[] a) throws Exception {
		File out = new File(a[0]);
		out.mkdirs();
		save(motorBody(COPPER), out, "portable_motor_copper");
		save(motorBody(BRASS), out, "portable_motor_brass");
		save(motorBody(DIAMOND), out, "portable_motor_diamond");
		save(motorFront(), out, "portable_motor_front");
		save(motorBattery(), out, "portable_motor_battery");
		int[] copperCell = { 0x14307a, 0x2447a6, 0x4a78d8, 0x9cc0ff };
		int[] brassCell = { 0x14307a, 0x2447a6, 0x4a78d8, 0x9cc0ff };
		int[] diamondCell = { 0x1f55c4, 0x3a7aee, 0x7db0ff, 0xdff0ff };
		save(solarTop(COPPER, copperCell), out, "solar_panel_copper_top");
		save(solarTop(BRASS, brassCell), out, "solar_panel_brass_top");
		save(solarTop(DIAMOND, diamondCell), out, "solar_panel_diamond_top");
		save(solarSide(COPPER), out, "solar_panel_copper_side");
		save(solarSide(BRASS), out, "solar_panel_brass_side");
		save(solarSide(DIAMOND), out, "solar_panel_diamond_side");
		save(cable(), out, "energy_cable");
		System.out.println("wrote 12 textures to " + out);
	}
}
