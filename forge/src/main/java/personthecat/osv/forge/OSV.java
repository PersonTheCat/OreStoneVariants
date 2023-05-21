package personthecat.osv.forge;

import lombok.extern.log4j.Log4j2;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import personthecat.catlib.command.CommandRegistrationContext;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.event.lifecycle.CheckErrorsEvent;
import personthecat.catlib.event.world.FeatureModificationEvent;
import personthecat.catlib.util.McUtils;
import personthecat.osv.ModRegistries;
import personthecat.osv.client.VariantColorizer;
import personthecat.osv.client.model.ModelHandler;
import personthecat.osv.command.CommandOsv;
import personthecat.osv.command.argument.BackgroundArgument;
import personthecat.osv.command.argument.BlockGroupArgument;
import personthecat.osv.command.argument.OrePresetArgument;
import personthecat.osv.command.argument.PropertyArgument;
import personthecat.osv.command.argument.PropertyGroupArgument;
import personthecat.osv.config.Cfg;
import personthecat.osv.config.OsvTrackers;
import personthecat.osv.exception.CompatibilityModeException;
import personthecat.osv.exception.ConfigFileNotLoadedException;
import personthecat.osv.exception.JarFilesNotCopiedException;
import personthecat.osv.init.VariantLoadingContext;
import personthecat.osv.init.forge.DeferredRegistryHelper;
import personthecat.osv.io.JarFiles;
import personthecat.osv.preset.writer.PresetWriter;
import personthecat.osv.recipe.RecipeHelper;
import personthecat.osv.tag.TagHelper;
import personthecat.osv.util.Reference;
import personthecat.osv.world.OreGen;
import personthecat.osv.world.carver.DimensionLocalCarver;
import personthecat.osv.world.carver.GiantClusterCarver;
import personthecat.osv.world.carver.GiantSphereCarver;
import personthecat.osv.world.feature.ClusterFeature;
import personthecat.osv.world.feature.SphereFeature;
import personthecat.osv.world.interceptor.InterceptorDispatcher;
import personthecat.osv.world.placement.DimensionPlacementModifier;
import personthecat.osv.world.placement.FlexiblePlacementModifier;
import personthecat.osv.world.providers.SimpleCount;
import personthecat.osv.world.providers.SimpleHeight;

import static personthecat.catlib.event.error.LibErrorContext.apply;

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
            (ServerStartingEvent e) -> this.serverStarting(e.getServer()));
        modBus.addGenericListener(Biome.class, EventPriority.LOWEST,
            DeferredRegistryHelper.defer(VariantLoadingContext::startLoading));
        eventBus.addListener(EventPriority.HIGHEST,
            (TagsUpdatedEvent e) -> TagHelper.injectTags());
        modBus.addGenericListener(Feature.class, this::registerFeatures);
        modBus.addGenericListener(WorldCarver.class, this::registerCarvers);

        CheckErrorsEvent.EVENT.register(VariantLoadingContext::stopLoading);
        CheckErrorsEvent.EVENT.register(ModRegistries.STONE_PRESETS::load);
        FeatureModificationEvent.EVENT.register(OreGen::setupOreFeatures);
        eventBus.addListener((ServerStoppingEvent e) -> this.serverStopping());
    }

    private boolean initCommon() {
        boolean e;
        e = apply(Reference.MOD, Cfg::register, ConfigFileNotLoadedException::new);
        e |= apply(Reference.MOD, JarFiles::copyPresets, JarFilesNotCopiedException::new);
        if (e) return false;

        BackgroundArgument.register();
        BlockGroupArgument.register();
        OrePresetArgument.register();
        PropertyArgument.register();
        PropertyGroupArgument.register();

        if (InterceptorDispatcher.COMPATIBILITY_MODE) {
            LibErrorContext.warn(Reference.MOD, new CompatibilityModeException());
        }
        CommandRegistrationContext.forMod(Reference.MOD)
            .addLibCommands().addAllCommands(CommandOsv.class).registerAll();

        return true;
    }

    private void initClient(final IEventBus modBus) {
        if (OsvTrackers.MODEL_CACHE.isUpdated()) {
            log.info("Model settings were updated. Resources will be regenerated.");
            ModelHandler.primeForRegen();
        }
        modBus.addListener(EventPriority.LOWEST, (FMLLoadCompleteEvent e) -> {
            ModelHandler.generateOverlayModel();
            VariantColorizer.colorizeAll();
        });
    }

    private void serverStarting(final MinecraftServer server) {
        RecipeHelper.injectRecipes(server.getRecipeManager());
        PresetWriter.savePresets(server.registryAccess());
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

        // Assuming Forge will eventually require an event for this...
        Registry.register(
            Registry.PLACEMENT_MODIFIERS,
            new ResourceLocation(Reference.MOD_ID, "flexible_placement_modifier"),
            FlexiblePlacementModifier.TYPE);
        Registry.register(
            Registry.PLACEMENT_MODIFIERS,
            new ResourceLocation(Reference.MOD_ID, "dimension_placement_modifier"),
            DimensionPlacementModifier.TYPE);
        Registry.register(
            Registry.HEIGHT_PROVIDER_TYPES,
            new ResourceLocation(Reference.MOD_ID, "simple_height"),
            SimpleHeight.TYPE);
        Registry.register(
            Registry.INT_PROVIDER_TYPES,
            new ResourceLocation(Reference.MOD_ID, "simple_count"),
            SimpleCount.TYPE);
    }

    private void registerCarvers(final RegistryEvent.Register<WorldCarver<?>> event) {
        event.getRegistry().register(GiantClusterCarver.INSTANCE
            .setRegistryName(new ResourceLocation(Reference.MOD_ID, "giant_cluster")));
        event.getRegistry().register(GiantSphereCarver.INSTANCE
            .setRegistryName(new ResourceLocation(Reference.MOD_ID, "giant_sphere")));
        event.getRegistry().register(DimensionLocalCarver.INSTANCE
            .setRegistryName(new ResourceLocation(Reference.MOD_ID, "dimension_local_carver")));
    }
}
