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
        "geolosys", "rustic", "metallurgy"
    );

    public static final String CLIENT = "com.personthecat.orestonevariants.proxy.ClientProxy";
    public static final String COMMON = "com.personthecat.orestonevariants.proxy.CommonProxy";
}
