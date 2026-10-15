import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Generates the Ponder scene schematics (vanilla structure-template NBT) for the addon.
 * <p>
 * Run from the project root with a plain JDK 21, no dependencies:
 * <pre>java tools/PonderStructureGen.java src/main/resources/assets/create_pocketdynamo/ponder</pre>
 * Output files are the ones the scenes reference: {@code <path>.nbt} next to the scene id.
 * Layer y=0 is the Ponder base plate; scene content starts at y=1.
 */
public class PonderStructureGen {
	static final int DATA_VERSION = 3955; // Minecraft 1.21.1
	static final String PLATE = "minecraft:smooth_stone";
	static final String MOD = "create_pocketdynamo:";

	// --- Minimal structure model ---

	record State(String name, Map<String, String> props) {
	}

	static class Structure {
		final int sx, sy, sz;
		final List<State> palette = new ArrayList<>();
		final List<int[]> blocks = new ArrayList<>(); // x, y, z, paletteIndex

		Structure(int sx, int sy, int sz) {
			this.sx = sx;
			this.sy = sy;
			this.sz = sz;
		}

		Structure plate() {
			for (int x = 0; x < sx; x++)
				for (int z = 0; z < sz; z++)
					set(x, 0, z, PLATE);
			return this;
		}

		Structure set(int x, int y, int z, String block, String... kv) {
			Map<String, String> props = new LinkedHashMap<>();
			for (int i = 0; i < kv.length; i += 2)
				props.put(kv[i], kv[i + 1]);
			State state = new State(block, props);
			int index = palette.indexOf(state);
			if (index < 0) {
				palette.add(state);
				index = palette.size() - 1;
			}
			blocks.add(new int[] { x, y, z, index });
			return this;
		}

		/** Energy cable with explicit connection flags (a Ponder world does not run neighbour updates). */
		Structure cable(int x, int y, int z, boolean north, boolean east, boolean south, boolean west) {
			return set(x, y, z, MOD + "energy_cable",
				"down", "false", "east", String.valueOf(east), "north", String.valueOf(north),
				"south", String.valueOf(south), "up", "false", "west", String.valueOf(west));
		}
	}

	// --- Scenes ---

	static Structure motorBasics() {
		// 5x5 plate. Motor at (3,1,2) pointing WEST, two shafts leading away from it (same layout as Create's own motor scene).
		return new Structure(5, 2, 5).plate()
			.set(1, 1, 2, "create:shaft", "axis", "x")
			.set(2, 1, 2, "create:shaft", "axis", "x")
			.set(3, 1, 2, MOD + "brass_portable_motor", "facing", "west");
	}

	static Structure motorCharging() {
		// Solar panel directly west of the motor; motor points EAST with shafts leading away.
		return new Structure(5, 2, 5).plate()
			.set(1, 1, 2, MOD + "brass_solar_panel")
			.set(2, 1, 2, MOD + "brass_portable_motor", "facing", "east")
			.set(3, 1, 2, "create:shaft", "axis", "x")
			.set(4, 1, 2, "create:shaft", "axis", "x");
	}

	static Structure solarSunlight() {
		// Three panel tiers in a row (z=1), a cable row below them (z=2) feeding a motor at (4,1,2) that points SOUTH.
		return new Structure(5, 2, 5).plate()
			.set(1, 1, 1, MOD + "copper_solar_panel")
			.set(2, 1, 1, MOD + "brass_solar_panel")
			.set(3, 1, 1, MOD + "diamond_solar_panel")
			.cable(1, 1, 2, true, true, false, false)
			.cable(2, 1, 2, true, true, false, true)
			.cable(3, 1, 2, true, true, false, true)
			.set(4, 1, 2, MOD + "brass_portable_motor", "facing", "south")
			.set(4, 1, 3, "create:shaft", "axis", "z")
			.set(4, 1, 4, "create:shaft", "axis", "z");
	}

	// --- Minimal NBT writer (uncompressed tag layout, gzip-wrapped like vanilla .nbt files) ---

	static final int T_END = 0, T_INT = 3, T_STRING = 8, T_LIST = 9, T_COMPOUND = 10;

	static void name(DataOutputStream out, int type, String name) throws IOException {
		out.writeByte(type);
		out.writeUTF(name);
	}

	static byte[] encode(Structure s) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(bytes))) {
			name(out, T_COMPOUND, ""); // root

			name(out, T_LIST, "size");
			out.writeByte(T_INT);
			out.writeInt(3);
			out.writeInt(s.sx);
			out.writeInt(s.sy);
			out.writeInt(s.sz);

			name(out, T_LIST, "entities");
			out.writeByte(T_END);
			out.writeInt(0);

			name(out, T_LIST, "blocks");
			out.writeByte(T_COMPOUND);
			out.writeInt(s.blocks.size());
			for (int[] b : s.blocks) {
				name(out, T_LIST, "pos");
				out.writeByte(T_INT);
				out.writeInt(3);
				out.writeInt(b[0]);
				out.writeInt(b[1]);
				out.writeInt(b[2]);
				name(out, T_INT, "state");
				out.writeInt(b[3]);
				out.writeByte(T_END);
			}

			name(out, T_LIST, "palette");
			out.writeByte(T_COMPOUND);
			out.writeInt(s.palette.size());
			for (State st : s.palette) {
				name(out, T_STRING, "Name");
				out.writeUTF(st.name());
				if (!st.props().isEmpty()) {
					name(out, T_COMPOUND, "Properties");
					for (Map.Entry<String, String> e : st.props().entrySet()) {
						name(out, T_STRING, e.getKey());
						out.writeUTF(e.getValue());
					}
					out.writeByte(T_END);
				}
				out.writeByte(T_END);
			}

			name(out, T_INT, "DataVersion");
			out.writeInt(DATA_VERSION);

			out.writeByte(T_END); // root
		}
		return bytes.toByteArray();
	}

	static void write(Path root, String path, Structure s) throws IOException {
		Path file = root.resolve(path + ".nbt");
		Files.createDirectories(file.getParent());
		Files.write(file, encode(s));
		System.out.println("wrote " + file + " (" + s.blocks.size() + " blocks, " + s.palette.size() + " palette entries)");
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("usage: java tools/PonderStructureGen.java <output ponder dir>");
			System.exit(2);
		}
		Path root = Path.of(args[0]);
		write(root, "portable_motor/basics", motorBasics());
		write(root, "portable_motor/charging", motorCharging());
		write(root, "solar_panel/sunlight", solarSunlight());
	}
}
