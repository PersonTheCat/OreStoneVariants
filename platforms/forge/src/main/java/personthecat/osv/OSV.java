package personthecat.osv;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import personthecat.catlib.command.CommandRegistrationContext;
import personthecat.catlib.event.world.FeatureModificationEvent;
import personthecat.osv.client.VariantColorizer;
import personthecat.osv.command.CommandOsv;
import personthecat.osv.config.Cfg;
import personthecat.osv.init.VariantLoadingContext;
import personthecat.osv.io.JarFiles;
import personthecat.osv.recipe.RecipeHelper;
import personthecat.osv.tag.TagHelper;
import personthecat.osv.util.Reference;
import personthecat.osv.world.OreGen;
import personthecat.osv.world.interceptor.InterceptorDispatcher;

@Mod(Reference.MOD_ID)
public class OSV {

    public OSV() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus eventBus = MinecraftForge.EVENT_BUS;

        this.initCommon();

        modBus.addListener(EventPriority.LOWEST,
            (FMLClientSetupEvent e) -> this.initClient(modBus));
        eventBus.addListener(EventPriority.LOWEST,
            (FMLServerStartingEvent e) -> this.serverStarting(e.getServer()));
        modBus.addGenericListener(Block.class, EventPriority.LOWEST,
            (RegistryEvent.Register<Block> e) -> VariantLoadingContext.startLoading());
        eventBus.addListener(EventPriority.HIGHEST,
            (TagsUpdatedEvent.CustomTagTypes e) -> TagHelper.injectTags(e.getTagManager()));

        FeatureModificationEvent.EVENT.register(OreGen::setupOreFeatures);
        eventBus.addListener((FMLServerStoppingEvent e) -> this.serverStopping());
    }

    private void initCommon() {
        Cfg.register();
        JarFiles.copyFiles();

        CommandRegistrationContext.forMod(Reference.MOD_DESCRIPTOR)
            .addLibCommands().addAllCommands(CommandOsv.class).registerAll();
    }

    private void initClient(final IEventBus modBus) {
        modBus.addListener(EventPriority.LOWEST,
            (FMLLoadCompleteEvent e) -> VariantColorizer.colorizeAll());
    }

    private void serverStarting(final MinecraftServer server) {
        RecipeHelper.injectRecipes(server.getRecipeManager());
    }

    private void serverStopping() {
        InterceptorDispatcher.unloadAll();
        OreGen.onWorldClosed();
    }
}