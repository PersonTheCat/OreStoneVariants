package personthecat.osv.util;

import personthecat.catlib.data.ModDescriptor;
import personthecat.osv.io.ModFolders;

import java.util.Arrays;
import java.util.List;

public class Reference {
    public static final String MOD_ID = "osv";
    public static final String MOD_NAME = "Ore Stone Variants";

    public static final ModDescriptor MOD_DESCRIPTOR = ModDescriptor.builder()
        .modId(MOD_ID)
        .name(MOD_NAME)
        .preferredDirectory(ModFolders.ORE_DIR)
        .build();

    public static final List<String> SUPPORTED_MODS = Arrays
        .asList("byg", "create", "druidcraft", "iceandfire", "minecraft", "osv", "simpleores", "quark");

    public static final List<String> VALID_EXTENSIONS = Arrays.asList("json", "hjson");
}
