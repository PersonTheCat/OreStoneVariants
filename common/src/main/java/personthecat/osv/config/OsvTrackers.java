package personthecat.osv.config;

import personthecat.catlib.versioning.ConfigTracker;
import personthecat.osv.util.Reference;

public class OsvTrackers {
    public static final ConfigTracker<ModelCache> MODEL_CACHE =
        ConfigTracker.forMod(Reference.MOD_DESCRIPTOR).withCategory("model_cache")
            .scheduleSave(ConfigTracker.PersistOption.GAME_READY).track(new ModelCache());
}
