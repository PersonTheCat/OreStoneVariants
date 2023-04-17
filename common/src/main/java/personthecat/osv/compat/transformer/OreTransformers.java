package personthecat.osv.compat.transformer;

import dev.architectury.injectables.annotations.ExpectPlatform;
import lombok.experimental.UtilityClass;
import personthecat.catlib.serialization.json.JsonTransformer;
import personthecat.catlib.serialization.json.JsonTransformer.ObjectResolver;
import personthecat.osv.preset.data.BlockSettings;
import personthecat.osv.preset.data.OreSettings;

@UtilityClass
public class OreTransformers {

    public static final ObjectResolver ROOT =
        JsonTransformer.root()
            .markRemoved("name", "7.0")
            .markRemoved("mod", "7.0")
            .freeze();

    public static final ObjectResolver VARIANT =
        JsonTransformer.root()
            .relocate("block.location", "variant.original")
            .relocate("block.xp", "variant.xp")
            .relocate("block.translationKey", "variant.translationKey")
            .relocate("block.copyTags", "variant.copyTags")
            .relocate("block.canBeDense", "variant.canBeDense")
            .freeze();

    public static final ObjectResolver BLOCK =
        JsonTransformer.withPath(OreSettings.Fields.block)
            .history("blocksMovement", BlockSettings.Fields.hasCollision)
            .history("resistance", BlockSettings.Fields.explosionResistance)
            .history("hardness", BlockSettings.Fields.destroyTime)
            .history("requiresTool", BlockSettings.Fields.requiresCorrectToolForDrops)
            .history("ticksRandomly", BlockSettings.Fields.isRandomlyTicking)
            .history("slipperiness", BlockSettings.Fields.friction)
            .history("isSolid", BlockSettings.Fields.canOcclude)
            .freeze();

    public static final ObjectResolver PLATFORM = createPlatform();

    public static final ObjectResolver STATE =
        JsonTransformer.root()
            .relocate("block.light", "state.lightEmission")
            .freeze();

    public static final ObjectResolver GEN =
        JsonTransformer.withPath(OreSettings.Fields.gen)
            .markRemoved("stage", "7.0")
            .relocate("blacklistBiomes", "biomes.blacklist")
            .relocate("decorator", "placement")
            .relocate("height.offset", "height.surface")
            .freeze();

    @ExpectPlatform
    private static ObjectResolver createPlatform() {
        throw new AssertionError();
    }
}
