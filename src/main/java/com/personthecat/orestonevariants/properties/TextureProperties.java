package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.MultiValueMap;
import com.personthecat.orestonevariants.util.PathTools;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.ResourceLocation;
import org.hjson.JsonObject;
import org.hjson.JsonValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.HjsonTools.asOrToArray;
import static com.personthecat.orestonevariants.util.HjsonTools.toStringArray;
import static com.personthecat.orestonevariants.util.HjsonTools.getBoolOr;
import static com.personthecat.orestonevariants.util.HjsonTools.getFloat;
import static com.personthecat.orestonevariants.util.HjsonTools.getString;
import static com.personthecat.orestonevariants.util.HjsonTools.getValue;

@Builder
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TextureProperties {

    /** The background texture used for extracting overlays. */
    @Default String background = extract("block/stone");

    /** Whether to use fancy "shaded" overlays. */
    @Default boolean shade = true;

    /** An optional parameter specifying the overlay extraction threshold. */
    @Default Optional<Float> threshold = empty();

    /** All of the original textures and which state they're mapped to. */
    @Default MultiValueMap<String, String> originals = new MultiValueMap<>();

    /** All of the resource locations for our overlays and which states they're mapped to. */
    @Default MultiValueMap<String, ResourceLocation> overlayLocations = new MultiValueMap<>();

    /** All of the raw file paths for our overlays and which states they're mapped to. */
    @Default MultiValueMap<String, String> overlayPaths = new MultiValueMap<>();

    /** Syntactically more consistent than calling TextureProperties::new. */
    public static TextureProperties from(JsonObject json) {
        final TexturePropertiesBuilder builder = builder();
        final boolean shade = getBoolOr(json, "shade", true);

        getString(json, "background").map(TextureProperties::extract)
            .ifPresent(builder::background);
        getValue(json, "original", value -> {
            final MultiValueMap<String, String> originals = readOriginals(value);
            final MultiValueMap<String, ResourceLocation> overlays = toOverlays(originals, shade);
            final MultiValueMap<String, String> paths = extractLocations(overlays);
            builder.originals(extractRaw(originals)).overlayLocations(overlays).overlayPaths(paths);
        });
        return builder.threshold(getFloat(json, "threshold")).shade(shade).build();
    }

    /** Produces a texture path from a resource location string. */
    private static String extract(String id) {
        return extract(new ResourceLocation(id));
    }

    /** Produces a raw (texture) file path from a resource location. */
    private static String extract(ResourceLocation location) {
        return f("/assets/{}/textures/{}.png", location.getNamespace(), location.getPath());
    }

    /**
     * Converts the json field "originals" into a map of state -> originals
     *
     * @param json either a list of strings, map of strings, or map of string lists.
     * @return A map of state -> originals
     */
    private static MultiValueMap<String, String> readOriginals(JsonValue json) {
        final MultiValueMap<String, String> overlays = new MultiValueMap<>();
        if (json.isArray()) {
            for (String texture : toStringArray(json.asArray())) {
                overlays.add("", texture);
            }
        } else if (json.isObject()) {
            for (JsonObject.Member member : json.asObject()) {
                for (String texture: toStringArray(asOrToArray(member.getValue()))) {
                    overlays.add(member.getName(), texture);
                }
            }
        } else {
            overlays.add("", json.asString());
        }
        return overlays;
    }

    /**
     * Converts a series of raw texture locations as strings to overlay locations.
     *
     * @param originals All of the original texture locations as raw strings.
     * @param shade Whether to shade all of these textures.
     * @return A list of resource locations pointing to these overlays.
     */
    private static MultiValueMap<String, ResourceLocation> toOverlays(MultiValueMap<String, String> originals, boolean shade) {
        final MultiValueMap<String, ResourceLocation> locations = new MultiValueMap<>();
        for (Map.Entry<String, List<String>> entry : originals.entrySet()) {
            for (String value : entry.getValue()) {
                final String filename = getFilename(new ResourceLocation(value), shade);
                locations.add(entry.getKey(), new ResourceLocation(Main.MODID, filename));
            }
        }
        return locations;
    }

    /**
     * Generates a file name to be associated with these properties' overlay sprites.
     *
     * @param location The resource location of the original texture.
     * @param shade whether to use a shaded overlay variant.
     * @return The new filename for this overlay.
     */
    private static String getFilename(ResourceLocation location, boolean shade) {
        final String filename = f("{}_overlay", PathTools.namespaceToSub(location));
        if (shade && Cfg.shadedTextures.get()) {
            return PathTools.ensureShaded(filename);
        }
        return filename;
    }

    /**
     * Extracts a series of resource locations to concrete paths.
     *
     * @param locations A list of resource locations.
     * @return A list of concrete paths.
     */
    private static MultiValueMap<String, String> extractLocations(MultiValueMap<String, ResourceLocation> locations) {
        final MultiValueMap<String, String> extracted = new MultiValueMap<>();
        for (Map.Entry<String, List<ResourceLocation>> entry : locations.entrySet()) {
            for (ResourceLocation value : entry.getValue()) {
                extracted.add(entry.getKey(), extract(value));
            }
        }
        return extracted;
    }

    /**
     * Variant of {@link #extractLocations} which accepts locations as strings.
     *
     * @param locations A list of resource locations as strings.
     * @return A list of concrete paths.
     */
    private static MultiValueMap<String, String> extractRaw(MultiValueMap<String, String> locations) {
        final MultiValueMap<String, String> extracted = new MultiValueMap<>();
        for (Map.Entry<String, List<String>> entry : locations.entrySet()) {
            for (String value : entry.getValue()) {
                extracted.add(entry.getKey(), extract(value));
            }
        }
        return extracted;
    }
}