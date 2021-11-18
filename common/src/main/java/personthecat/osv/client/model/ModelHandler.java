package personthecat.osv.client.model;

import org.hjson.JsonObject;
import org.hjson.Stringify;
import personthecat.catlib.event.error.LibErrorContext;
import personthecat.catlib.exception.GenericFormattedException;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.PathUtils;
import personthecat.osv.client.ClientResourceHelper;
import personthecat.osv.client.texture.TextureHandler;
import personthecat.osv.config.Cfg;
import personthecat.osv.config.VariantDescriptor;
import personthecat.osv.io.ModFolders;
import personthecat.osv.io.ResourceHelper;
import personthecat.osv.preset.data.ModelSettings;
import personthecat.osv.util.Reference;

import java.io.File;

public class ModelHandler {

    private static final String OVERLAY_TEMPLATE_PATH = "assets/osv/overlay_template.txt";
    private static final String OVERLAY_MODEL_PATH = "assets/osv/models/block/overlay.json";

    /**
     * Moves existing resources to the backup directory. Deletes any folders more
     * than 10 versions old.
     */
    public static void primeForRegen() {
        final File backups = Reference.MOD_DESCRIPTOR.getBackupFolder();
        FileIO.backup(backups, ModFolders.RESOURCE_DIR, false);
        FileIO.truncateBackups(backups, ModFolders.RESOURCE_DIR, 10);
    }

    /**
     * Constructs the overlay model to be used by any ore variant that needs it. The
     * generated file can be edited by the user, but it will come with defaults which
     * are ultimately defined in the config file.
     */
    public static void generateOverlayModel() {
        if (!ClientResourceHelper.hasResource(OVERLAY_MODEL_PATH)) {
            final double offset = ((Cfg.getModelScale() * 16.0) - 16.0) / 2.0;
            final String overlay = FileIO.getResourceAsString(OVERLAY_TEMPLATE_PATH)
                .expect("Reading overlay template")
                .replace("{min}", String.valueOf(0.0 - offset))
                .replace("{max}", String.valueOf(16.0 + offset))
                .replace("{shade}", String.valueOf(Cfg.shadeOverlays()));
            writeModel(OVERLAY_MODEL_PATH, overlay);
        }
    }

    /**
     * Generates <b>every</b> model required for this block entry, including its block
     * state model, block models, and item models. In addition, this method is ultimately
     * responsible for generating overlays for this ore type (if applicable), as well as
     * any single layer textures needed.
     *
     * @param variant Information about a single foreground / background pair.
     */
    public static void generateModels(final VariantDescriptor variant) {
        final String blockStatePath = PathUtils.asBlockStatePath(variant.getId());
        if (!ClientResourceHelper.hasResource(blockStatePath)) {
            if (!TextureHandler.overlaysGenerated(variant.getForeground())) {
                TextureHandler.generateOverlays(variant.getForeground());
            }
            runGenerator(blockStatePath, variant);
        }
    }

    /**
     * Executes the generator for this block entry after all checks have passed.
     *
     * @param blockStatePath The generated path to this variant's block state JSON.
     * @param variant Information about a single foreground / background pair.
     */
    private static void runGenerator(final String blockStatePath, final VariantDescriptor variant) {
        final ModelSettings cfg = variant.getForeground().getModel();
        final ModelGenerator gen = cfg.getType().getGenerator();

        try {
            gen.generateModels(variant, blockStatePath, ModelHandler::writeModel);
        } catch (final RuntimeException e) {
            LibErrorContext.registerSingle(Reference.MOD_NAME, new GenericFormattedException(e));
        }
    }

    /**
     * Writes a new JSON object to the /resources directory as a formatted string.
     *
     * @param concretePath The raw, relative path to this model.
     * @param json The JSON object being serialized.
     */
    private static void writeModel(final String concretePath, final JsonObject json) {
        writeModel(concretePath, json.toString(Stringify.FORMATTED));
    }

    /**
     * Writes a string to the /resources directory.
     *
     * @param concretePath The raw, relative path to this file.
     * @param json Any string data (which will always be a JSON).
     */
    private static void writeModel(final String concretePath, final String json) {
        ResourceHelper.writeResource(concretePath, json).ifErr(e ->
            LibErrorContext.registerSingle(Reference.MOD_NAME, new GenericFormattedException(e)));
    }
}
