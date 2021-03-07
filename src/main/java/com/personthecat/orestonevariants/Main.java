package com.personthecat.orestonevariants;

import com.personthecat.orestonevariants.commands.CommandOSV;
import com.personthecat.orestonevariants.commands.HjsonArgument;
import com.personthecat.orestonevariants.commands.PathArgument;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.init.ClientRegistryHandler;
import com.personthecat.orestonevariants.io.JarFiles;
import com.personthecat.orestonevariants.io.ResourceHelper;
import com.personthecat.orestonevariants.models.ModelConstructor;
import com.personthecat.orestonevariants.recipes.RecipeHelper;
import com.personthecat.orestonevariants.textures.SpriteHandler;
import com.personthecat.orestonevariants.world.OreGen;
import com.personthecat.orestonevariants.world.WorldInterceptor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
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
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ResourceHelper::enableResourcePack);
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
        ModelConstructor.generateOverlayModel();
        SpriteHandler.generateOverlays();

        modBus.addListener(EventPriority.LOWEST, ClientRegistryHandler::clientLoadComplete);
    }

    private void initServer(final FMLServerStartingEvent event) {
        WorldInterceptor.init(event.getServer().func_241755_D_());
        RecipeHelper.handleRecipes(event.getServer().getRecipeManager());
        CommandOSV.register(event.getServer().getCommandManager());
    }
}
