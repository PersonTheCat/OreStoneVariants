package com.personthecat.orestonevariants.world;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.blocks.BaseOreVariant;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.Lazy;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

import static net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.*;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

@EventBusSubscriber
@SuppressWarnings("unused")
public class DisableVanillaGen {

    /** Indicates whether an OSV variant of quartz (netherrack) has been created. */
    private static final Lazy<Boolean> NETHER_QUARTZ_EXISTS = new Lazy<>(() ->
        find(Main.BLOCKS, DisableVanillaGen::isNetherQuartz).isPresent()
    );

    /** A list of all vanilla ore gen events. */
    private static final List<GenerateMinable.EventType> VANILLA_ORES = list(
        COAL, DIAMOND, GOLD, IRON, LAPIS, REDSTONE, EMERALD
    );

    /** A list of all vanilla stone gen events. */
    private static final List<GenerateMinable.EventType> VANILLA_STONE = list(
        DIRT, GRAVEL, DIORITE, GRANITE, ANDESITE
    );

    @SubscribeEvent
    public static void disableGen(GenerateMinable event) {
        final boolean enableStone = Cfg.WorldCat.enableVanillaStone;
        final boolean enableOres = Cfg.WorldCat.enableVanillaOres;
        final GenerateMinable.EventType type = event.getType();

        if (!enableOres && VANILLA_ORES.contains(type)) {
            event.setResult(Result.DENY);
        } else if (!enableStone && VANILLA_STONE.contains(type)) {
            event.setResult(Result.DENY);
        } else if (type.equals(QUARTZ) && NETHER_QUARTZ_EXISTS.get()) {
            event.setResult(Result.DENY);
        } else if (type.equals(CUSTOM) && !isModLoaded("undergroundbiomes")) {
            event.setResult(Result.DENY);
        }
    }

    /** Returns whether the input variant is equivalent to standard nether quartz. */
    private static boolean isNetherQuartz(BaseOreVariant ore) {
        return ore.bgBlock.equals(Blocks.NETHERRACK.getDefaultState())
            && ore.properties.ore.get().equals(Blocks.QUARTZ_ORE.getDefaultState());
    }
}
