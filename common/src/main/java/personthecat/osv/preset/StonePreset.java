package personthecat.osv.preset;

import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.hjson.ParseException;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.HjsonUtils;
import personthecat.fresult.Result;
import personthecat.osv.compat.PresetCompat;
import personthecat.osv.config.Cfg;
import personthecat.osv.exception.CorruptPresetException;
import personthecat.osv.exception.InvalidPresetArgumentException;
import personthecat.osv.exception.PresetLoadException;
import personthecat.osv.exception.PresetSyntaxException;
import personthecat.osv.io.ModFolders;
import personthecat.osv.preset.data.DecoratedFeatureSettings;
import personthecat.osv.preset.data.StoneSettings;
import personthecat.osv.util.Reference;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static personthecat.catlib.util.PathUtils.noExtension;

@Value
public class StonePreset {

    StoneSettings settings;
    String name;
    String mod;
    File file;
    JsonObject raw;

    private StonePreset(final StoneSettings settings, final ResourceLocation id, final File file, final JsonObject raw) {
        this.settings = settings;
        this.name = id.getPath();
        this.mod = id.getNamespace();
        this.file = file;
        this.raw = raw;
    }

    public static Optional<StonePreset> fromFile(final File file) throws PresetLoadException {
        final JsonObject json = readContents(file, getContents(file));

        // There will be other settings through CatLib soon.
        if (Cfg.updatePresets()) {
            PresetCompat.transformStonePreset(file, json);
        }

        if (isEnabled(json) && Cfg.modEnabled(readMod(json))) {
            try {
                final StoneSettings settings = HjsonUtils.readThrowing(StoneSettings.CODEC, json);
                final ResourceLocation stone = CommonRegistries.BLOCKS.getKey(settings.getStone().getBlock());
                final ResourceLocation id = new ResourceLocation(Objects.requireNonNull(stone).getNamespace(), noExtension(file));
                return Optional.of(new StonePreset(settings, id, file, json));
            } catch (final RuntimeException e) {
                throw new InvalidPresetArgumentException(ModFolders.STONE_DIR, file, e);
            }
        }
        return Optional.empty();
    }

    private static String getContents(final File file) throws CorruptPresetException {
        final Result<String, IOException> result = FileIO.readFile(file);
        final Optional<IOException> error = result.getErr();
        if (error.isPresent()) {
            throw new CorruptPresetException(ModFolders.STONE_DIR, file, error.get());
        }
        return result.unwrap();
    }

    private static JsonObject readContents(final File file, final String contents) throws PresetSyntaxException {
        final Result<JsonValue, ParseException> result = HjsonUtils.readValue(contents);
        final Optional<ParseException> error = result.getErr();
        if (error.isPresent()) {
            throw new PresetSyntaxException(ModFolders.STONE_DIR, file, contents, error.get());
        }
        return result.unwrap().asObject();
    }

    private static boolean isEnabled(final JsonObject json) {
        return HjsonUtils.getBool(json, "enabled").orElse(true);
    }

    private static String readMod(final JsonObject json) {
        return HjsonUtils.getString(json, StoneSettings.Fields.stone)
            .map(StonePreset::getNamespace)
            .orElse(Reference.MOD_ID);
    }

    private static String getNamespace(final String stone) {
        final int index = stone.indexOf(":");
        return index < 0 ? "minecraft" : stone.substring(0, index);
    }

    public BlockState getStone() {
        return this.settings.getStone();
    }

    public RuleTest getSource() {
        return this.settings.getSource();
    }

    public List<DecoratedFeatureSettings<?, ?>> getFeatures() {
        return this.settings.getGen().getFeatures();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof StonePreset) {
            return this.name.equals(((StonePreset) o).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return this.name;
    }
}
