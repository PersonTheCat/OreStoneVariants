package personthecat.osv.client.model.forge;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.MultiLayerModel;
import org.hjson.JsonObject;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.io.FileIO;
import personthecat.catlib.util.HjsonUtils;
import personthecat.osv.client.model.ModelWrapper;
import personthecat.osv.config.Cfg;
import personthecat.osv.config.VariantDescriptor;

import java.util.Optional;

public class OverlayModelGeneratorImpl {

    private static final String MODEL_TEMPLATE = FileIO.getResourceAsString("assets/osv/forge_model_template.txt")
        .expect("Loading forge model template");

    public static JsonObject platformModel(final VariantDescriptor cfg, final ModelWrapper model, final ResourceLocation overlay) {
        final String raw = MODEL_TEMPLATE.replace("{bg}", model.getId().toString())
            .replace("{fg}", overlay.toString())
            .replace("{particle}", resolveParticle(model.getModel()).orElseGet(overlay::toString))
            .replace("{bg_layer}", getBgLayer(cfg.getBackground()))
            .replace("{fg_layer}", getFgLayer());

        return HjsonUtils.readValue(raw).unwrap().asObject();
    }

    private static Optional<String> resolveParticle(final JsonObject model) {
        return HjsonUtils.getObject(model, "textures").flatMap(t -> HjsonUtils.getString(t, "particle"));
    }

    private static String getBgLayer(final ResourceLocation bg) {
        final Block block = CommonRegistries.BLOCKS.lookup(bg);
        if (block == null) return "solid";

        final RenderType layer = ItemBlockRenderTypes.getRenderType(block.defaultBlockState(), true);
        final String name = MultiLayerModel.Loader.BLOCK_LAYERS.inverse().get(layer);

        return name == null ? "solid" : name;
    }

    private static String getFgLayer() {
        return Cfg.overlayTransparency() ? "translucent" : "cutout_mipped";
    }
}
