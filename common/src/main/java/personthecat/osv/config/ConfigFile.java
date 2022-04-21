package personthecat.osv.config;

import xjs.core.JsonObject;

import java.io.File;

public class ConfigFile {
    public final File file;
    public final JsonObject json;

    ConfigFile(final File file, final JsonObject json) {
        this.file = file;
        this.json = json;
    }
}
