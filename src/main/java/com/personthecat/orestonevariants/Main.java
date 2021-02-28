package com.personthecat.orestonevariants;

import com.personthecat.orestonevariants.commands.CommandOSV;
import com.personthecat.orestonevariants.commands.HjsonArgument;
import com.personthecat.orestonevariants.commands.PathArgument;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.io.JarFiles;
import com.personthecat.orestonevariants.io.ResourceHelper;
import com.personthecat.orestonevariants.models.ModelConstructor;
import com.personthecat.orestonevariants.recipes.RecipeHelper;
import com.personthecat.orestonevariants.textures.SpriteHandler;
import com.personthecat.orestonevariants.world.OreGen;
import com.personthecat.orestonevariants.world.WorldInterceptor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public class Main {

    /** A setting representing this mod's namespace. */
    public static final String MODID = "osv";

    /** A convenient reference to the current mod event bus. */
    private final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

    /** A reference to Forge's main event bus. */
    private final IEventBus eventBus = MinecraftForge.EVENT_BUS;

    public Main() {
        JarFiles.copyFiles();
        Cfg.register(ModLoadingContext.get().getActiveContainer());
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        modBus.addListener(EventPriority.LOWEST, this::initCommon);
        modBus.addListener(EventPriority.LOWEST, this::initClient);
        eventBus.addListener(EventPriority.HIGHEST, this::initServer);
        eventBus.addListener(EventPriority.LOWEST, OreGen::setupOreFeatures);
    }

    @SuppressWarnings("unused")
    private void initCommon(final FMLCommonSetupEvent event) {
        PathArgument.register();
        HjsonArgument.register();
    }

    @SuppressWarnings("unused")
    private void initClient(final FMLClientSetupEvent event) {
        ResourceHelper.enableResourcePack();
        ModelConstructor.generateOverlayModel();
        SpriteHandler.generateOverlays();
    }

    private void initServer(final FMLServerStartingEvent event) {
        WorldInterceptor.init(event.getServer().getWorlds().iterator().next());
        RecipeHelper.handleRecipes(event.getServer().getRecipeManager());
        CommandOSV.register(event.getServer().getCommandManager());
    }
}
