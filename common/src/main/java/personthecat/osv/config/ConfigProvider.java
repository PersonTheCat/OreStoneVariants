package personthecat.osv.config;

import lombok.extern.log4j.Log4j2;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.ParseException;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.event.error.Severity;
import personthecat.catlib.exception.FormattedIOException;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.McUtils;
import personthecat.catlib.versioning.Version;
import personthecat.osv.compat.ConfigCompat;
import personthecat.osv.exception.PresetSyntaxException;
import personthecat.osv.exception.UnavailableConfigException;
import personthecat.osv.util.Reference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Log4j2
public class ConfigProvider {

    /**
     * Generates a platform-agnostic representation of this mod's config file.
     *
     * <p>Required until CatLib can handle configs for us.
     *
     * @param client Whether to generate the client config instead of the common.
     * @return A DTO containing the config file and its parsed JSON object.
     */
    public static ConfigFile loadFile(final boolean client) {
        final String filename = createFilename(client);
        final File main = new File(McUtils.getConfigDir(), filename);
        final File backup = new File(Reference.MOD_DESCRIPTOR.getBackupFolder(), filename);

        ConfigFile config = tryLoad(main);
        if (config != null) {
            backupConfig(main, backup);
        } else if (backup.exists()) {
            log.error("Main config file is invalid. Loading the backup file.");
            config = tryLoad(backup);
        }
        if (config == null) {
            throw new UnavailableConfigException(filename + " and its backup are both invalid");
        }
        if (Reference.VERSION_CACHE.isUpgraded()) {
            updateConfig(config, client);
        }
        return config;
    }

    private static String createFilename(final boolean client) {
        return Reference.MOD_ID + (client ? "-client.hjson" : "-common.hjson");
    }

    @Nullable
    private static ConfigFile tryLoad(final File file) {
        final String contents = FileIO.contents(file).orElse(null);
        if (contents == null) return new ConfigFile(file, new JsonObject());
        try {
            final JsonObject json = JsonValue.readHjson(contents, HjsonUtils.FORMATTER).asObject();
            return new ConfigFile(file, json);
        } catch (final ParseException e) {
            log.error("Error loading " + file.getName());
            LibErrorContext.registerSingle(Reference.MOD_DESCRIPTOR,
                new PresetSyntaxException(McUtils.getConfigDir(), file, contents, e));
        }
        return null;
    }

    private static void backupConfig(final File main, final File backup) {
        try {
            Files.copy(main.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            LibErrorContext.registerSingle(Severity.WARN, Reference.MOD_DESCRIPTOR, new FormattedIOException(main, e));
        }
    }

    private static void updateConfig(final ConfigFile config, final boolean client) {
        if (client) {
            ConfigCompat.transformClientConfig(config.json);
            log.info("Client config updated successfully!");
            log.info("All configs up to date! Welcome to OSV {}!", Reference.MOD_VERSION);
        } else {
            log.info("Detected an upgrade from {} to {}. Transforming old configs...",
                Reference.VERSION_CACHE.getCachedOrDefault(Version.ZERO), Reference.MOD_VERSION);
            ConfigCompat.transformCommonConfig(config.json);
            log.info("Common config updated successfully!");
        }
    }
}
