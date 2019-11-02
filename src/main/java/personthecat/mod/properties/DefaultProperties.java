package personthecat.mod.properties;

import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.Loader;
import personthecat.mod.properties.OreProperties.DropProperties;

/**
 * Default stone types are listed under 
 * BlockGroup.Builder.DEFAULT_GROUP_INFO
 * 
 * For anyone searching for a list of default properties, open this as a raw file on GitHub,
 * and then paste it into another program such as notepad++ for correct tab spacing.
 * 
 * Substitutions for reading this as a block list:
 * 
 *  * mod() = the mod's name
 *  * ore() = block_ore
 *  * ero() = ore_block
 *    * e.g. mod() + "." ore() = "basemetals.copper_ore"
 *  * fullName() = modname_block_ore
 *  * SAME = repeat what came before it
 *  * rng(numbers) = a range of the numbers in parentheses.
 *  * Domains are automatically added to drops 
 *    * e.g. a drop of ore() = "simpleores:copper_ore"
 */
public class DefaultProperties
{
    /*
     * Field shorteners
     */
    private static final String 
        
        SAME = "thesameasthelastvalue",    
        GUESS_TEXTURE = "usethenormaltexturepathformat",
        GUESS_REVERSE = "samebuttheorenameisreversed",
        GUESS_THERMAL = "thermalfoundationsformat",
        GUESS_EMBERS = "embersformat",
        BUILTIN = "alreadyhavethese";
    
    public static class DefaultOreProperties
    {
        public static void init()
        {
//            modname_x_ore            languageKey,                hardness, level, drop,     dropSilkTouch,    dropRange,     xpRange,    originalTexture, blend
            
        group("minecraft");
            
            ore("coal");             c("oreCoal",                 3.0F, 0, "coal",        ore(),        rng(1),     rng(0, 2),    GUESS_TEXTURE,    false);    
            ore("diamond");            c("oreDiamond",             3.0F, 2, "diamond",         ore(),        rng(1),     rng(3, 7),    GUESS_TEXTURE,    false);
            ore("emerald");            c("oreEmerald",             3.0F, 2, "emerald",      ore(),        rng(1),     rng(3, 7),    BUILTIN,        true);
            ore("gold");            c("oreGold",                 3.0F, 2, ore(),         SAME,        rng(1),     rng(0),        GUESS_TEXTURE,    false);
            ore("iron");            c("oreIron",                 3.0F, 1, ore(),         SAME,        rng(1),     rng(0),        GUESS_TEXTURE,    false);
            ore("lapis");            c("oreLapis",                 3.0F, 2, "dye:4",         ore(),        rng(4, 8),     rng(2, 5),    GUESS_TEXTURE,    false);
            ore("redstone");        c("oreRedstone",             3.0F, 2, "redstone",     ore(),        rng(4, 5),     rng(1, 5),    GUESS_TEXTURE,    false);
            ore("lit_redstone");    c("oreRedstone",             3.0F, 2, "redstone","redstone_ore",    rng(4, 5),     rng(1, 5),    GUESS_TEXTURE,    false);
            ore("quartz");            c("netherquartz",            3.0F, 1, "quartz",         ore(),        rng(1),        rng(2, 5),    BUILTIN,        true);
    
        group("quark");
                
            ore("biotite");            c(mod() + ":" + ore(),        3.0F, 1, "biotite",        ore(),        rng(1),        rng(1, 3),    GUESS_TEXTURE,    true);
        
        group("iceandfire");

            ore("sapphire");        c(mod() + ".sapphireOre",    3.0F, 2,"sapphire_gem",    ore(),        rng(1),        rng(0),        BUILTIN,        false);
            ore("silver");            c(mod() + ".silverOre",        3.0F, 2, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
     
        group("simpleores");
     
            ore("adamantium");        c(ore(),                    5.0F, 2, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("copper");            c(ore(),                    1.7F, 1, ore(),            SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("mythril");            c(ore(),                    4.0F, 2, ore(),            SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("tin");                c(ore(),                    3.0F, 1, ore(),             SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("onyx");            c(ore(),                    7.0F, 3, "onyx_gem",    ore(),        rng(1),        rng(0),        BUILTIN,        false);
     
        group("basemetals");
     
            ore("antimony");         c(mod() + "." + ore(),        1.0F, 0, ore(),             SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("bismuth");         c(mod() + "." + ore(),        1.0F, 0, ore(),             SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("copper");             c(mod() + "." + ore(),        4.0F, 1, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("lead");             c(mod() + "." + ore(),        1.0F, 0, ore(),            SAME,        rng(1),        rng(0),        BUILTIN,        false);
            ore("mercury");         c(mod() + "." + ore(),        1.0F, 0, ore(),            SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("nickel");             c(mod() + "." + ore(),        4.0F, 1, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("pewter");             c(mod() + "." + ore(),        1.0F, 0, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("platinum");         c(mod() + "." + ore(),        3.0F, 1, ore(),            SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("silver");             c(mod() + "." + ore(),        5.0F, 1, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("tin");             c(mod() + "." + ore(),         1.0F, 1, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("zinc");             c(mod() + "." + ore(),        1.0F, 0, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("adamantine");         c(mod() + "." + ore(),        12.0F,4, ore(),         SAME,        rng(1),        rng(0),        BUILTIN,        false);
            ore("coldiron");        c(mod() + "." + ore(),        7.0F, 2, ore(),             SAME,        rng(1),        rng(0),        BUILTIN,        false);
            ore("cupronickel");        c(mod() + "." + ore(),        6.0F, 2, ore(),            SAME,        rng(1),        rng(0),        BUILTIN,        false);
            ore("starsteel");        c(mod() + "." + ore(),         10.0F,3, ore(),            SAME,        rng(1),        rng(0),        BUILTIN,        false);
     
        group("biomesoplenty");
     
            ore("amber");            c("gem_ore." + ore(),        3.0F, 2, "gem:7",      "gem_ore:7",    rng(1),        rng(3, 7),    GUESS_TEXTURE,    true);
            ore("malachite");        c("gem_ore." + ore(),        3.0F, 2, "gem:5",     "gem_ore:5",    rng(1),     rng(3, 7),    GUESS_TEXTURE,    true);
            ore("peridot");            c("gem_ore." + ore(),        3.0F, 2, "gem:2",      "gem_ore:2",    rng(1),     rng(3, 7),    GUESS_TEXTURE,    true);
            ore("ruby");            c("gem_ore." + ore(),        3.0F, 2, "gem:1",     "gem_ore:1",    rng(1),     rng(3, 7),    GUESS_TEXTURE,    true);
            ore("sapphire");        c("gem_ore." + ore(),        3.0F, 2, "gem:6",      "gem_ore:6",    rng(1),     rng(3, 7),    GUESS_TEXTURE,    true);
            ore("tanzanite");        c("gem_ore." + ore(),        3.0F, 2, "gem:4",     "gem_ore:4",    rng(1),     rng(3, 7),    GUESS_TEXTURE,    true);
            ore("topaz");            c("gem_ore." + ore(),        3.0F, 2, "gem:3",    "gem_ore:3",    rng(1),     rng(3, 7),    GUESS_TEXTURE,    true);
            ore("amethyst");        c("gem_ore." + ore(),        3.0F, 3, "gem",      "gem_ore",         rng(1),     rng(3, 7),    GUESS_TEXTURE,    true);
     
        group("glasshearts");
     
            ore("agate");            c(mod() + ".ore.agate",        3.0F, 2, "gem:7",          "ore:7",     rng(1),     rng(0),        GUESS_TEXTURE,    true);
            ore("amethyst");        c(mod() + ".ore.amethyst",    3.0F, 2, "gem",          "ore",         rng(1),     rng(0),        GUESS_TEXTURE,    true);
            ore("onyx");            c(mod() + ".ore.onyx",        3.0F, 2, "gem:5",          "ore:5",     rng(1),     rng(0),        GUESS_TEXTURE,    true);
            ore("opal");            c(mod() + ".ore.opal",        3.0F, 2, "gem:4",         "ore:4",     rng(1),     rng(0),        GUESS_TEXTURE,    true);
            ore("ruby");            c(mod() + ".ore.ruby",        3.0F, 2, "gem:1",          "ore:1",     rng(1),     rng(0),        GUESS_TEXTURE,    true);
            ore("sapphire");        c(mod() + ".ore.sapphire",    3.0F, 2, "gem:3",          "ore:3",     rng(1),     rng(0),        GUESS_TEXTURE,    true);
            ore("topaz");            c(mod() + ".ore.topaz",        3.0F, 2, "gem:2",          "ore:2",     rng(1),     rng(0),        GUESS_TEXTURE,    true);
         
        group("thermalfoundation");
     
            ore("aluminum");        c(mod() + ".ore.aluminum",    3.0F, 1, "ore:4",         SAME,        rng(1),        rng(0),        GUESS_THERMAL,    false);
            ore("copper");            c(mod() + ".ore.copper",    3.0F, 1, "ore",           SAME,        rng(1),        rng(0),        GUESS_THERMAL,    false);
            ore("iridium");            c(mod() + ".ore.iridium",    3.0F, 1, "ore:7",         SAME,        rng(1),        rng(0),        GUESS_THERMAL,    false);
            ore("lead");            c(mod() + ".ore.lead",        3.0F, 1, "ore:3",         SAME,        rng(1),        rng(0),        GUESS_THERMAL,    false);
            ore("mithril");            c(mod() + ".ore.mithril",    3.0F, 1, "ore:8",         SAME,        rng(1),        rng(0),        GUESS_THERMAL,    false);
            ore("nickel");            c(mod() + ".ore.nickel",    3.0F, 1, "ore:5",         SAME,        rng(1),        rng(0),        GUESS_THERMAL,    false);
            ore("platinum");        c(mod() + ".ore.platinum",    3.0F, 1, "ore:6",         SAME,        rng(1),        rng(0),        GUESS_THERMAL,    false);
            ore("silver");            c(mod() + ".ore.silver",    3.0F, 1, "ore:2",         SAME,        rng(1),        rng(0),        GUESS_THERMAL,    false);
            ore("tin");                c(mod() + ".ore.tin",        3.0F, 1, "ore:1",        SAME,        rng(1),        rng(0),        GUESS_THERMAL,    false);
    
        group("immersiveengineering");
    
            ore("aluminum");        c(mod() + ".ore.aluminum",    3.0F, 1, "ore:1",         SAME,        rng(1),        rng(0),        BUILTIN,        true);
            ore("copper");            c(mod() + ".ore.copper",    3.0F, 1, "ore",         SAME,        rng(1),        rng(0),        BUILTIN,        true);
            ore("lead");            c(mod() + ".ore.lead",        3.0F, 1, "ore:2",         SAME,        rng(1),        rng(0),        BUILTIN,        true);
            ore("nickel");            c(mod() + ".ore.nickel",    3.0F, 2, "ore:4",         SAME,        rng(1),        rng(0),        BUILTIN,        true);
            ore("silver");            c(mod() + ".ore.silver",    3.0F, 2, "ore:3",         SAME,        rng(1),        rng(0),        BUILTIN,        true);
            ore("uranium");            c(mod() + ".ore.uranium",    3.0F, 2, "ore:5",         SAME,        rng(1),        rng(0),        BUILTIN,        true);
    
        group("embers");
    
            ore("aluminum");        c(ero(),                    1.6F, 1, ero(),            SAME,        rng(1),        rng(0),        GUESS_EMBERS,    false);
            ore("copper");            c(ero(),                    1.8F, 1, ero(),            SAME,        rng(1),        rng(0),        GUESS_EMBERS,    false);
            ore("lead");            c(ero(),                    2.5F, 2, ero(),            SAME,        rng(1),        rng(0),        GUESS_EMBERS,    false);
            ore("nickel");            c(ero(),                    2.2F, 1, ero(),            SAME,        rng(1),        rng(0),        GUESS_EMBERS,    false);
            ore("silver");            c(ero(),                    2.5F, 2, ero(),            SAME,        rng(1),        rng(0),        GUESS_EMBERS,    false);
            ore("tin");                c(ero(),                    1.3F, 1, ero(),            SAME,        rng(1),        rng(0),        GUESS_EMBERS,    false);

        group("thaumcraft"); //Can't verify these xp values.
     
            ore("amber");            c(ero(),                    2.0F, 1, "amber",        ero(),        rng(1),        rng(1),        BUILTIN,        true);
            ore("cinnabar");        c(ero(),                    2.0F, 2, ero(),            SAME,        rng(1),        rng(0),        BUILTIN,        true);
            
        group("mineralogy");
     
            ore("phosphorous");        c(mod() + "." + ore(),        1.5F, 0, "phosphorous_dust",ore(),    rng(1, 4),    rng(0),        GUESS_TEXTURE,    true);
            ore("sulfur");            c(mod() + "." + ore(),        1.5F, 0, "sulfur_dust",    ore(),        rng(1, 4),    rng(0),        GUESS_TEXTURE,    false);
     
        group("modernmetals");
     
            ore("aluminum");        c(mod() + "." + ore(),        3.75F, 1, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
//           ore("aluminumbrass");    c(mod() + "." + ore(),        7.5F,  2, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("beryllium");        c(mod() + "." + ore(),        2.5F,  0, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("boron");            c(mod() + "." + ore(),        8.0F,  2, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("cadmium");            c(mod() + "." + ore(),        1.0F,  0, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("chromium");        c(mod() + "." + ore(),        3.0F,  1, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
//           ore("galvanizedsteel");    c(mod() + "." + ore(),        15.25F,5, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("iridium");            c(mod() + "." + ore(),        3.0F,  1, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("magnesium");        c(mod() + "." + ore(),        3.5F,  1, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("manganese");        c(mod() + "." + ore(),        2.75F, 0, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
//           ore("nichrome");        c(mod() + "." + ore(),        15.5F, 5, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("osmium");            c(mod() + "." + ore(),        2.75F, 0, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("plutonium");        c(mod() + "." + ore(),        7.5F,  2, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("rutile");            c(mod() + "." + ore(),        0.25F, 0, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
//           ore("stainlesssteel");    c(mod() + "." + ore(),        16.0F, 5, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("tantalum");        c(mod() + "." + ore(),        17.5F, 5, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("thorium");            c(mod() + "." + ore(),        1.0F,  0, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
//           ore("titanium");        c(mod() + "." + ore(),        16.25F,5, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("tungsten");        c(mod() + "." + ore(),        12.5F, 4, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("uranium");            c(mod() + "." + ore(),        16.25F,5, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
            ore("zirconium");         c(mod() + "." + ore(),        3.5F,  1, ore(),         SAME,        rng(1),        rng(0),        GUESS_TEXTURE,    false);
             
             loadAdditionalSettings();
        }
        
        private static String currentMod;
        private static String nameStorage;
        
        private static void c(String languageKey, float hardness, int level, String drop, String dropAlt, int[] dropRange, int[] xpRange, String originalTexture, boolean blendOverlay)
        {
            dropAlt = dropAlt.equals(SAME) ? drop : dropAlt;

            DropProperties newDroperties = new DropProperties(drop(drop), drop(dropAlt), dropRange, xpRange);
            OreProperties newProperties = new OreProperties(fullName(), languageKey, hardness, level, newDroperties);
            
            newProperties.setModName(currentMod);
            setOriginalTexture(newProperties, originalTexture);
            if (blendOverlay) newProperties.setUseBlendedTextures();
        }
        
        private static void group(String modName)
        {
            currentMod = modName;
        }
        
        private static void ore(String name)
        {
            nameStorage = name;
        }
        
        /*
         * Property setters
         */
        
        private static void setOriginalTexture(OreProperties props, String originalTexture)
        {
            if (!originalTexture.equals(BUILTIN))
            {
                props.setOriginalTexture(getTextureGuesser(originalTexture));
                
                if (Loader.isModLoaded(mod())) props.testTextures();
            }
        }
        
        /*
         * Field shorteners
         */
        
        private static String mod()
        {
            return currentMod;
        }
        
        private static String ore()
        {
            return nameStorage + "_ore";
        }
        
        private static String ero()
        {
            return "ore_" + nameStorage;
        }
        
        private static String fullName()
        {
            return mod().contains("minecraft") ? nameStorage + "_ore" : mod() + "_" + nameStorage + "_ore";
        }

        private static int[] rng(int... range)
        {
            return range;
        }
        
        private static String drop(String resourcePath)
        {
            return mod() + ":" + resourcePath;
        }
        
        /*
         * Texture finders
         */
        
        private static String getTextureGuesser(String s)
        {
            if (s.equals(GUESS_TEXTURE)) return path(ore());
            if (s.equals(GUESS_REVERSE)) return path(ero());
            if (s.equals(GUESS_THERMAL)) return path("ore/" + ero());
            if (s.equals(GUESS_EMBERS)) return path(ero() + "_vanilla");
            
            return s;
        }
        
        private static String path(String endOfPath)
        {
            return "assets/" + mod() + "/textures/blocks/" + endOfPath + ".png";
        }
        
        /**
         * Miscellaneous
         */
        
        private static void loadAdditionalSettings() //There are too few of these to warrant additional columns.
        {            
            OreProperties.propertiesOf("lit_redstone_ore").setLightLevel(0.625F);
            OreProperties.propertiesOf("thermalfoundation_iridium_ore").setLightLevel(4.0F);
            OreProperties.propertiesOf("thermalfoundation_platinum_ore").setLightLevel(4.0F);
            OreProperties.propertiesOf("thermalfoundation_silver_ore").setLightLevel(4.0F);
            OreProperties.propertiesOf("thermalfoundation_mithril_ore").setLightLevel(8.0F);
            OreProperties.propertiesOf("mineralogy_phosphorous_ore").setBackgroundMatcher("assets/mineralogy/textures/blocks/limestone.png");
            OreProperties.propertiesOf("quark_biotite_ore").setBackgroundMatcher("assets/minecraft/textures/blocks/end_stone.png");

            DropProperties curio = new DropProperties("thaumcraft:curio:1", "thaumcraft:ore_amber", new int[] {1}, new int [] {0});
            curio.setChance(5.0);
            
            OreProperties.propertiesOf("thaumcraft_amber_ore").addDropProperties(curio);
        }
    }
    
    private static final Type[] NO_TYPE = new Type[] {};
    private static final String[] NO_NAMES = new String[] {};
    
    public static class DefaultWorldGenProperties
    {
        public static void init()
        {
            //      name                                size,   count,  minY,   maxY,   biome types,                            biome names
            genProp("coal_ore",                         20,     17,     0,         128,     NO_TYPE,                                 NO_NAMES);
            genProp("diamond_ore",                         8,         1,         0,         16,        NO_TYPE,                                 NO_NAMES);
            genProp("emerald_ore",                        3,         2,         0,         32,        new Type[] {Type.MOUNTAIN},             NO_NAMES);
            genProp("gold_ore",                            9,         2,         0,         32,        NO_TYPE,                                 NO_NAMES);
            genProp("iron_ore",                            9,         20,     0,         64,        NO_TYPE,                                 NO_NAMES);
            genProp("lapis_ore",                        7,         2,         0,         32,        NO_TYPE,                                 NO_NAMES);
            genProp("redstone_ore",                        8,         8,         0,         32,        NO_TYPE,                                 NO_NAMES);
            genProp("lit_redstone_ore",                    0,         0,         0,         0,        NO_TYPE,                                 NO_NAMES);
            genProp("quartz_ore",                        9,        20,        0,        128,    NO_TYPE,                                NO_NAMES);
            
            genProp("quark_biotite_ore",                8,        8,        0,        128,    NO_TYPE,                                NO_NAMES);
            
            genProp("iceandfire_sapphire_ore",            3,        2,        4,        32,        NO_TYPE,                                 new String[] {"iceandfire:glacier"});
            genProp("iceandfire_silver_ore",            9,        2,        4,        32,        NO_TYPE,                                 NO_NAMES);
 
            genProp("simpleores_adamantium_ore",        6,        6,        1,        30,        NO_TYPE,                                NO_NAMES);
            genProp("simpleores_copper_ore",            10,        35,        1,        90,        NO_TYPE,                                 NO_NAMES);
            genProp("simpleores_mythril_ore",            8,        10,        1,        40,        NO_TYPE,                                 NO_NAMES);
            genProp("simpleores_tin_ore",                10,        30,        1,        90,        NO_TYPE,                                 NO_NAMES);
            genProp("simpleores_onyx_ore",                6,        6,        1,        127,    NO_TYPE,                                NO_NAMES);
 
            genProp("basemetals_antimony_ore",             0,        0,        0,        0,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_bismuth_ore",             0,        0,        0,        0,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_copper_ore",             8,        10,        0,        96,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_lead_ore",                 8,        5,        0,        64,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_mercury_ore",             8,        3,        0,        32,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_nickel_ore",             8,        1,        32,        96,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_pewter_ore",             0,        0,        0,        0,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_platinum_ore",             8,        1,        1,        32,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_silver_ore",             8,        4,        0,        32,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_tin_ore",                 8,        10,        0,        128,    NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_zinc_ore",                 8,        5,        0,        96,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_adamantine_ore",        8,        2,        0,        128,    NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_coldiron_ore",            8,        5,        0,        128,    NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_cupronickel_ore",        0,        0,        0,        0,        NO_TYPE,                                 NO_NAMES);
            genProp("basemetals_starsteel_ore",            8,        5,        0,        255,    NO_TYPE,                                 NO_NAMES);
 
            genProp("biomesoplenty_amber_ore",            4,        1,        4,        32,        new Type[] {Type.FOREST},                 NO_NAMES);
            genProp("biomesoplenty_malachite_ore",        4,        1,        4,        32,        new Type[] {Type.SWAMP},                 NO_NAMES);
            genProp("biomesoplenty_peridot_ore",        4,        1,        4,        32,        new Type[] {Type.PLAINS},                NO_NAMES);
            genProp("biomesoplenty_ruby_ore",            4,        1,        4,        32,        new Type[] {Type.DRY, Type.SANDY},         NO_NAMES);
            genProp("biomesoplenty_sapphire_ore",        4,        1,        4,        32,        new Type[] {Type.OCEAN, Type.WATER},    NO_NAMES);
            genProp("biomesoplenty_tanzanite_ore",        4,        1,        4,        32,        new Type[] {Type.SNOWY},                 NO_NAMES);
            genProp("biomesoplenty_topaz_ore",            4,        1,        4,        32,        new Type[] {Type.JUNGLE, Type.BEACH},    NO_NAMES);
            genProp("biomesoplenty_amethyst_ore",        4,        1,        4,        32,        NO_TYPE,                                 NO_NAMES);

            genProp("glasshearts_agate_ore",            3,         2,         0,         32,        NO_TYPE,                                 NO_NAMES);
            genProp("glasshearts_amethyst_ore",            3,         2,         0,         32,        NO_TYPE,                                 NO_NAMES);
            genProp("glasshearts_onyx_ore",                3,         2,         0,         32,        NO_TYPE,                                 NO_NAMES);
            genProp("glasshearts_opal_ore",             3,         2,         0,         32,        NO_TYPE,                                 NO_NAMES);
            genProp("glasshearts_ruby_ore",                3,         2,         0,         32,        NO_TYPE,                                 NO_NAMES);
            genProp("glasshearts_sapphire_ore",         3,         2,         0,         32,        NO_TYPE,                                 NO_NAMES);
            genProp("glasshearts_topaz_ore",            3,         2,         0,         32,        NO_TYPE,                                 NO_NAMES);

            genProp("thermalfoundation_copper_ore",        8,        8,         40,        75,        new Type[] {Type.OCEAN},                NO_NAMES);
            genProp("thermalfoundation_copper_ocean",    8,        4,        20,        55,        new Type[] {Type.OCEAN},                NO_NAMES);
            genProp("thermalfoundation_copper_high",    8,        8,        48,        96,        NO_TYPE,                                NO_NAMES);
            genProp("thermalfoundation_lead_ore",        8,        1,        5,        30,        NO_TYPE,                                NO_NAMES);
            genProp("thermalfoundation_nickel_ore",        4,        2,        5,        20,        NO_TYPE,                                NO_NAMES);
            genProp("thermalfoundation_silver_ore",        8,        3,        5,        30,        NO_TYPE,                                NO_NAMES);
            genProp("thermalfoundation_tin_ore",        8,        7,        20,        55,        NO_TYPE,                                NO_NAMES);
 
            genProp("immersiveengineering_aluminum_ore",4,        8,        40,        72,        NO_TYPE,                                NO_NAMES);
            genProp("immersiveengineering_copper_ore",    8,        8,        40,        72,        NO_TYPE,                                NO_NAMES);
            genProp("immersiveengineering_lead_ore",    6,        4,        8,        36,        NO_TYPE,                                NO_NAMES);
            genProp("immersiveengineering_nickel_ore",    6,        2,        8,        24,        NO_TYPE,                                NO_NAMES);
            genProp("immersiveengineering_silver_ore",    8,        3,        8,        40,        NO_TYPE,                                NO_NAMES);
            genProp("immersiveengineering_uranium_ore",    4,        1,        8,        24,        NO_TYPE,                                NO_NAMES);

            genProp("embers_aluminum_ore",                6,        4,        0,        58,        NO_TYPE,                                NO_NAMES);
            genProp("embers_copper_ore",                12,        6,        0,        64,        NO_TYPE,                                NO_NAMES);
            genProp("embers_lead_ore",                    8,        4,        0,        64,        NO_TYPE,                                NO_NAMES);
            genProp("embers_nickel_ore",                6,        4,        0,        24,        NO_TYPE,                                NO_NAMES);
            genProp("embers_silver_ore",                6,        4,        0,        28,        NO_TYPE,                                NO_NAMES);
            genProp("embers_tin_ore",                    6,        6,        0,        48,        NO_TYPE,                                NO_NAMES);
 
            //Also difficult to verify.
            genProp("thaumcraft_amber_ore",                3,        18,        0,        128,    NO_TYPE,                                NO_NAMES);
            genProp("thaumcraft_cinnabar_ore",            3,        18,        0,        128,    NO_TYPE,                                NO_NAMES);
 
            genProp("mineralogy_phosphorous_ore",        16,        1,        16,        64,        NO_TYPE,                                NO_NAMES);
            genProp("mineralogy_sulfur_ore",            16,        1,        16,        64,        NO_TYPE,                                NO_NAMES);
 
            genProp("modernmetals_aluminum_ore",        10,        8,        0,        96,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_beryllium_ore",        5,        20,        0,        128,    NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_boron_ore",            8,        8,        0,        32,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_cadmium_ore",            4,        8,        0,        96,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_chromium_ore",        2,        3,        0,        32,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_iridium_ore",            6,        5,        0,        64,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_magnesium_ore",        6,        8,        0,        96,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_manganese_ore",        6,        6,        0,        64,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_osmium_ore",            10,        8,        0,        96,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_plutonium_ore",        2,        4,        0,        32,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_rutile_ore",            6,        6,        0,        64,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_tantalum_ore",        6,        6,        0,        64,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_thorium_ore",            8,        8,        0,        32,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_tungsten_ore",        4,        6,        0,        32,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_uranium_ore",            2,        6,        0,        32,        NO_TYPE,                                NO_NAMES);
            genProp("modernmetals_zirconium_ore",        8,        6,        0,        64,        NO_TYPE,                                NO_NAMES);
            
            loadAdditionalSettings();
        }
        
        private static void genProp(String name, int size, int count, int minY, int maxY, Type[] types, String[] names)
        {
            new WorldGenProperties(name, size, count, minY, maxY, types, names).register();
        }
        
        private static void loadAdditionalSettings()
        {
            WorldGenProperties thermalfoundation_copper = WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("thermalfoundation_copper_ore");
            thermalfoundation_copper.setUseBiomeBlacklist();
            thermalfoundation_copper.setAdditionalProperties(
                WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("thermalfoundation_copper_ocean"),
                WorldGenProperties.WORLDGEN_PROPERTY_MAP.get("thermalfoundation_copper_high")
            );
        }
    }
}
