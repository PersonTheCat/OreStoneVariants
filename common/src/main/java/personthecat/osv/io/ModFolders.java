package personthecat.osv.io;

import personthecat.catlib.util.McUtils;
import personthecat.osv.util.Reference;

import java.io.File;

public class ModFolders {
    public static final File OSV_DIR = new File(McUtils.getConfigDir(), Reference.MOD_ID);
    public static final File ORE_DIR = new File(OSV_DIR, "ores");
    public static final File STONE_DIR = new File(OSV_DIR, "stone");
    public static final File RESOURCE_DIR = new File(OSV_DIR, "resources");
}
