package personthecat.osv;

import lombok.extern.log4j.Log4j2;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import personthecat.catlib.command.CommandRegistrationContext;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.event.error.Severity;
import personthecat.catlib.event.lifecycle.CheckErrorsEvent;
import personthecat.catlib.event.player.CommonPlayerEvent;
import personthecat.catlib.event.world.FeatureModificationEvent;
import personthecat.catlib.util.McUtils;
import personthecat.osv.client.VariantColorizer;
import personthecat.osv.client.model.ModelHandler;
import personthecat.osv.command.CommandOsv;
import personthecat.osv.command.argument.*;
import personthecat.osv.config.Cfg;
import personthecat.osv.config.OsvTrackers;
import personthecat.osv.exception.ConfigFileNotLoadedException;
import personthecat.osv.exception.JarFilesNotCopiedException;
import personthecat.osv.exception.UnavailableConfigException;
import personthecat.osv.init.DeferredRegistryHelper;
import personthecat.osv.init.VariantLoadingContext;
import personthecat.osv.io.JarFiles;
import personthecat.osv.preset.writer.PresetWriter;
import personthecat.osv.recipe.RecipeHelper;
import personthecat.osv.tag.TagHelper;
import personthecat.osv.util.Reference;
import personthecat.osv.world.OreGen;
import personthecat.osv.world.carver.GiantClusterCarver;
import personthecat.osv.world.carver.GiantSphereCarver;
import personthecat.osv.world.decorator.FlexibleVariantDecorator;
import personthecat.osv.world.feature.*;
import personthecat.osv.world.interceptor.InterceptorDispatcher;

@Log4j2
@Mod(Reference.MOD_ID)
public class OSV {

    public OSV() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus eventBus = MinecraftForge.EVENT_BUS;

        if (!this.initCommon()) {
            return;
        }
        if (McUtils.isClientSide()) {
            this.initClient(modBus);
        }
        eventBus.addListener(EventPriority.LOWEST,
            (FMLServerStartingEvent e) -> this.serverStarting(e.getServer()));
        modBus.addGenericListener(Biome.class, EventPriority.LOWEST,
            DeferredRegistryHelper.defer(VariantLoadingContext::startLoading));
        eventBus.addListener(EventPriority.HIGHEST,
            (TagsUpdatedEvent.CustomTagTypes e) -> TagHelper.injectTags(e.getTagManager()));
        modBus.addGenericListener(Feature.class, this::registerFeatures);
        modBus.addGenericListener(WorldCarver.class, this::registerCarvers);
        modBus.addGenericListener(FeatureDecorator.class, this::registerDecorators);

        CheckErrorsEvent.EVENT.register(VariantLoadingContext::stopLoading);
        FeatureModificationEvent.EVENT.register(OreGen::setupOreFeatures);
        eventBus.addListener((FMLServerStoppingEvent e) -> this.serverStopping());
    }

    private boolean initCommon() {
        try {
            Cfg.register();
        } catch (final RuntimeException e) {
            LibErrorContext.registerSingle(Severity.FATAL, Reference.MOD_DESCRIPTOR,
                new ConfigFileNotLoadedException(e));
            return false;
        }
        try {
            JarFiles.copyFiles();
        } catch (final RuntimeException e) {
            LibErrorContext.registerSingle(Severity.FATAL, Reference.MOD_DESCRIPTOR,
                new JarFilesNotCopiedException(e));
            return false;
        }

        BackgroundArgument.register();
        BlockGroupArgument.register();
        OrePresetArgument.register();
        PropertyArgument.register();
        PropertyGroupArgument.register();

        CommandRegistrationContext.forMod(Reference.MOD_DESCRIPTOR)
            .addLibCommands().addAllCommands(CommandOsv.class).registerAll();

        CommonPlayerEvent.LOGIN.register((p, s) ->
            p.displayClientMessage(new TextComponent("You have entered... The Scary Door"), true));

        return true;
    }

    private void initClient(final IEventBus modBus) {
        if (OsvTrackers.MODEL_CACHE.isUpdated()) {
            log.info("Model settings were updated. Resources will be regenerated.");
            ModelHandler.primeForRegen();
        }
        ModelHandler.generateOverlayModel();
        modBus.addListener(EventPriority.LOWEST,
            (FMLLoadCompleteEvent e) -> VariantColorizer.colorizeAll());
    }

    private void serverStarting(final MinecraftServer server) {
        RecipeHelper.injectRecipes(server.getRecipeManager());
        PresetWriter.savePresets();
    }

    private void serverStopping() {
        InterceptorDispatcher.unloadAll();
        OreGen.onWorldClosed();
    }

    private void registerFeatures(final RegistryEvent.Register<Feature<?>> event) {
        event.getRegistry().register(ClusterFeature.INSTANCE
            .setRegistryName(new ResourceLocation(Reference.MOD_ID, "cluster")));
        event.getRegistry().register(SphereFeature.INSTANCE
            .setRegistryName(new ResourceLocation(Reference.MOD_ID, "sphere")));
    }

    private void registerCarvers(final RegistryEvent.Register<WorldCarver<?>> event) {
        event.getRegistry().register(GiantClusterCarver.INSTANCE
            .setRegistryName(new ResourceLocation(Reference.MOD_ID, "giant_cluster")));
        event.getRegistry().register(GiantSphereCarver.INSTANCE
            .setRegistryName(new ResourceLocation(Reference.MOD_ID, "giant_sphere")));
    }

    private void registerDecorators(final RegistryEvent.Register<FeatureDecorator<?>> event) {
        event.getRegistry().register(FlexibleVariantDecorator.INSTANCE
            .setRegistryName(new ResourceLocation(Reference.MOD_ID, "flexible_decorator")));
    }
}
