package personthecat.osv.compat.collector;

import xjs.core.JsonObject;
import personthecat.osv.preset.OrePreset;

public interface McMetaCollector {
    void collect(final OrePreset cfg, final JsonObject output, final JsonObject original);
}
