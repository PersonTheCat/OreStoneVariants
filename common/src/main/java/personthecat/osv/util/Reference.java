package personthecat.osv.util;

import personthecat.catlib.data.ModDescriptor;
import personthecat.catlib.versioning.ConfigTracker;
import personthecat.catlib.versioning.ConfigTracker.PersistOption;
import personthecat.catlib.versioning.Version;
import personthecat.catlib.versioning.VersionTracker;
import personthecat.osv.config.ModelCache;
import personthecat.osv.io.ModFolders;

import java.util.Arrays;
import java.util.List;

public class Reference {

    public static final String MOD_ID = "@MOD_ID@";
    public static final String MOD_NAME = "@MOD_NAME@";
    public static final Version MOD_VERSION = Version.parse("@MOD_VERSION@");

    public static final ModDescriptor MOD_DESCRIPTOR =
        ModDescriptor.builder().modId(MOD_ID).name(MOD_NAME).version(MOD_VERSION)
            .preferredDirectory(ModFolders.ORE_DIR).build();

    public static final List<String> SUPPORTED_MODS =
        Arrays.asList("byg", "create", "druidcraft", "iceandfire", "minecraft", "osv", "simpleores", "quark");

    public static final List<String> VALID_EXTENSIONS = Arrays.asList("json", "hjson");
}
