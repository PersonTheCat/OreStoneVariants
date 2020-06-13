package com.personthecat.orestonevariants.util;

import java.util.List;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class Reference {
    public static final List<String> SUPPORTED_MODS = list(
        "minecraft", "quark", "iceandfire",
        "simpleores", "basemetals", "biomesoplenty",
        "glasshearts", "thermalfoundation", "embers",
        "undergroundbiomes", "mineralogy", "thaumcraft",
        "immersiveengineering", "earthworks", "modernmetals",
        "geolosys", "rustic"
    );

    public static final String CLIENT = "com.personthecat.orestonevariants.proxy.ClientProxy";
    public static final String COMMON = "com.personthecat.orestonevariants.proxy.CommonProxy";

//    private static String generateDependencies() {
//        final StringBuilder sb = new StringBuilder();
//        SUPPORTED_MODS.forEach(s -> {
//            sb.append("after:");
//            sb.append(s);
//            sb.append("; ");
//        });
//        return sb.toString();
//    }
}
