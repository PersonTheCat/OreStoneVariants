package com.personthecat.orestonevariants.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * While it is already possible to use custom file formats, Forge will automatically
 * generate its own TOML config file regardless. This class prevents any redundant files.
 * In the future, it is my hope that will not be necessary.
 */
public class CustomModConfig extends ModConfig {
    /** The raw config to be forwarded to all of Forges processes instead of their own. */
    private final CommentedFileConfig cfg;

    public CustomModConfig(Type type, ForgeConfigSpec spec, ModContainer container, CommentedFileConfig cfg) {
        super(type, spec, container, cfg.getFile().getPath());
        this.cfg = cfg;
        spec.setConfig(cfg);
    }

    @Override
    public CommentedConfig getConfigData() {
        return cfg;
    }

    @Override
    public ConfigFileTypeHandler getHandler() {
        return new DummyConfigHandler(cfg);
    }

    /** A dummy handler for circumventing Forge's config spawning process. */
    private static class DummyConfigHandler extends ConfigFileTypeHandler {
        final CommentedFileConfig cfg;

        private DummyConfigHandler(CommentedFileConfig cfg) {
            this.cfg = cfg;
        }

        @Override
        public Function<ModConfig, CommentedFileConfig> reader(Path configBasePath) {
            // The config has already been read. Just return it.
            return c -> cfg;
        }
    }
}
