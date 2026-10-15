import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/**
 * Generates the empty arena the GameTests run in (a vanilla structure-template NBT).
 * <p>
 * Run from the project root with a plain JDK 21, no dependencies:
 * <pre>java tools/GameTestStructureGen.java src/gametest/resources/data/create_pocketdynamo/structure</pre>
 * The arena is a smooth stone floor (y=0) with air above it, so that a test starts from a known, open-sky box.
 */
public class GameTestStructureGen {
	static final int DATA_VERSION = 3955; // Minecraft 1.21.1
	static final int SIZE_XZ = 9;
	static final int SIZE_Y = 6;

	static final int T_END = 0, T_INT = 3, T_STRING = 8, T_LIST = 9, T_COMPOUND = 10;

	static void name(DataOutputStream out, int type, String name) throws IOException {
		out.writeByte(type);
		out.writeUTF(name);
	}

	static byte[] encode() throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(bytes))) {
			name(out, T_COMPOUND, ""); // root

			name(out, T_LIST, "size");
			out.writeByte(T_INT);
			out.writeInt(3);
			out.writeInt(SIZE_XZ);
			out.writeInt(SIZE_Y);
			out.writeInt(SIZE_XZ);

			name(out, T_LIST, "entities");
			out.writeByte(T_END);
			out.writeInt(0);

			// Every position is listed, air included, so the arena also clears whatever the world had there.
			name(out, T_LIST, "blocks");
			out.writeByte(T_COMPOUND);
			out.writeInt(SIZE_XZ * SIZE_Y * SIZE_XZ);
			for (int y = 0; y < SIZE_Y; y++) {
				for (int z = 0; z < SIZE_XZ; z++) {
					for (int x = 0; x < SIZE_XZ; x++) {
						name(out, T_LIST, "pos");
						out.writeByte(T_INT);
						out.writeInt(3);
						out.writeInt(x);
						out.writeInt(y);
						out.writeInt(z);
						name(out, T_INT, "state");
						out.writeInt(y == 0 ? 0 : 1);
						out.writeByte(T_END);
					}
				}
			}

			name(out, T_LIST, "palette");
			out.writeByte(T_COMPOUND);
			out.writeInt(2);
			name(out, T_STRING, "Name");
			out.writeUTF("minecraft:smooth_stone");
			out.writeByte(T_END);
			name(out, T_STRING, "Name");
			out.writeUTF("minecraft:air");
			out.writeByte(T_END);

			name(out, T_INT, "DataVersion");
			out.writeInt(DATA_VERSION);

			out.writeByte(T_END); // root
		}
		return bytes.toByteArray();
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("usage: java tools/GameTestStructureGen.java <output structure dir>");
			System.exit(2);
		}
		Path file = Path.of(args[0]).resolve("arena.nbt");
		Files.createDirectories(file.getParent());
		Files.write(file, encode());
		System.out.println("wrote " + file);
	}
}
