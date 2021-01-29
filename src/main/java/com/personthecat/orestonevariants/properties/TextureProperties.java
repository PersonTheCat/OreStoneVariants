package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.Main;
import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.PathTools;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.ResourceLocation;
import org.hjson.JsonObject;
import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.empty;
import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.HjsonTools.getBool;
import static com.personthecat.orestonevariants.util.HjsonTools.getFloat;
import static com.personthecat.orestonevariants.util.HjsonTools.getString;

@Builder
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TextureProperties {

    /** The original ore sprite to generate overlays from. */
    @Default String original = extract("item/string");

    /** The background texture used for extracting overlays. */
    @Default String background = extract("block/stone");

    /** Whether to use fancy "shaded" overlays. */
    @Default boolean shade = false;

    /** An optional parameter specifying the overlay extraction threshold. */
    @Default Optional<Float> threshold = empty();

    /** The name of the image file to be generated. */
    String filename;

    /** A path to these properties' overlay sprite. */
    Lazy<String> overlayPath = new Lazy<>(
        () -> f("assets/{}/textures/block/{}.png", Main.MODID, filename())
    );

    /** A ResourceLocation representing these properties' overlay sprite. */
    Lazy<ResourceLocation> overlayLocation = new Lazy<>(
        () -> new ResourceLocation(Main.MODID, "block/" + filename())
    );

    /** Syntactically more consistent than calling TextureProperties::new. */
    public static TextureProperties from(ResourceLocation location, JsonObject json) {
        final TexturePropertiesBuilder builder = builder();
        getString(json, "original").map(TextureProperties::extract)
            .ifPresent(builder::original);
        getString(json, "background").map(TextureProperties::extract)
            .ifPresent(builder::background);
        getBool(json, "shade", builder::shade);

        return builder
            .threshold(getFloat(json, "threshold"))
            .filename(getFilename(location, builder.shade$value))
            .build();
    }

    private static String extract(String condensedPath) {
        final ResourceLocation asRL = new ResourceLocation(condensedPath);
        return f("/assets/{}/textures/{}.png", asRL.getNamespace(), asRL.getPath());
    }

    /** Generates a file name to be associated with these properties' overlay sprite. */
    private static String getFilename(ResourceLocation location, boolean shade) {
        final String fileName = f("{}/{}_overlay", location.getNamespace(), location.getPath());
        if (shade && Cfg.shadedTextures.get()) {
            return PathTools.ensureShaded(fileName);
        }
        return fileName;
    }

    private String filename() {
        return filename;
    }
}