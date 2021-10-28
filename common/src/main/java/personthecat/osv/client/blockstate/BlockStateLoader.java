package personthecat.osv.client.blockstate;

import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.util.HjsonUtils;
import personthecat.catlib.util.PathUtils;
import personthecat.osv.client.ClientResourceHelper;
import personthecat.osv.util.StateMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
public class BlockStateLoader {

    public static StateMap<List<VariantWrapper>> getModel(final ResourceLocation id) {
        final StateMap<List<VariantWrapper>> map = new StateMap<>();
        final JsonObject variants = loadVariants(id);
        if (variants != null) {
            for (final JsonObject.Member member : variants) {
                final List<VariantWrapper> wrappers = new ArrayList<>();
                for (final JsonValue value : HjsonUtils.asOrToArray(member.getValue())) {
                    if (value.isObject()) {
                        VariantWrapper.tryCreate(value.asObject()).ifPresent(wrappers::add);
                    }
                }
                map.put(member.getName(), wrappers);
            }
        }
        return map;
    }

    @Nullable
    private static JsonObject loadVariants(final ResourceLocation id) {
        final String path = PathUtils.asBlockStatePath(id);
        final Optional<InputStream> resource = ClientResourceHelper.locateResource(path);
        if (resource.isPresent()) {
            try (final InputStream is = resource.get()) {
                final JsonObject def = JsonObject.readHjson(new InputStreamReader(is)).asObject();
                final JsonValue variants = def.get("variants");
                if (variants == null) {
                    log.warn("No variants definition in {}. Unable to load block state definition.", id);
                    return null;
                }
                return variants.asObject();
            } catch (final IOException | UnsupportedOperationException ignored) {}
        }
        return null;
    }
}
