package personthecat.osv.compat.collector;

import personthecat.osv.preset.OrePreset;
import xjs.core.JsonObject;

public interface McMetaCollector {
    void collect(final OrePreset cfg, final JsonObject output, final JsonObject original);
}
