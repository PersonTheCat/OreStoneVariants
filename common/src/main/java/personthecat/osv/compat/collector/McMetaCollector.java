package personthecat.osv.compat.collector;

import org.hjson.JsonObject;
import personthecat.osv.preset.OrePreset;

public interface McMetaCollector {
    void collect(final OrePreset cfg, final JsonObject output, final JsonObject original);
}
