package com.personthecat.orestonevariants.properties;

import com.personthecat.orestonevariants.config.Cfg;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.util.ResourceLocation;
import org.hjson.JsonObject;

import java.util.Objects;
import java.util.Optional;

import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.CommonMethods.osvLocation;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

public class TextureProperties {
    /** The name of the image file to be generated. */
    public final String fileName;
    /** The original ore sprite to generate overlays from. */
    public final String original;
    /** The background texture used for extracting overlays. */
    public final String background;
    /** A path to these properties' overlay sprite. */
    public final String overlayPath;
    /** A ResourceLocation representing these properties' overlay sprite. */
    public final ResourceLocation overlayLocation;
    /** Whether to use fancy "shaded" overlays. */
    public final boolean shade;
    /** An optional parameter specifying the overlay extraction threshold. */
    public final Optional<Float> threshold;

    /** The default overlay texture used for generating overlays. */
    private static final String DEFAULT_TEXTURE = "item/string";
    /** The default background texture used for generating overlays. */
    private static final String DEFAULT_MATCHER = "block/stone";

    public TextureProperties(ResourceLocation location, JsonObject json) {
        this(
            location,
            getStringOr(json, "original", DEFAULT_TEXTURE),
            getStringOr(json, "background", DEFAULT_MATCHER),
            getBoolOr(json, "shade", true),
            getFloat(json, "threshold")
        );
    }

    public TextureProperties(
        ResourceLocation location,
        String original,
        String background,
        boolean shade,
        Optional<Float> threshold
    ) {
        this.original = extract(original);
        this.background = extract(background);
        this.shade = shade;
        this.fileName = getFileName(location, shade);
        this.overlayPath = f("assets/{}/textures/block/{}.png", location.getNamespace(), fileName);
        this.overlayLocation = new ResourceLocation(location.getNamespace(), "block/" + fileName);
        this.threshold = threshold;
    }

    /** Syntactically more consistent than calling TextureProperties::new. */
    public static TextureProperties from(ResourceLocation location, JsonObject json) {
        return new TextureProperties(location, json);
    }

    private static String extract(String condensedPath) {
        ResourceLocation asRL = new ResourceLocation(condensedPath);
        return f("/assets/{}/textures/{}.png", asRL.getNamespace(), asRL.getPath());
    }

    /** Generates a file name to be associated with these properties' overlay sprite. */
    private static String getFileName(ResourceLocation location, boolean shade) {
        final String fileName = f("{}/{}_overlay", location.getNamespace(), location.getPath());
        if (shade && Cfg.shadedTextures.get()) {
            return PathTools.ensureShaded(fileName);
        }
        return fileName;
    }

    public Builder builder() {
        return new Builder();
    }

    // Api
    public static class Builder {
        private String original;
        private ResourceLocation location;
        private String background = DEFAULT_MATCHER;
        private boolean shade = true;
        private Optional<Float> threshold = Optional.empty();

        private Builder() {}

        public TextureProperties build() {
            Objects.requireNonNull(original, "You must provide the path to the original ore texture");
            Objects.requireNonNull(location, "You must provide a location to store this overlay");
            return new TextureProperties(location, original, background, shade, threshold);
        }

        public Builder original(String original) {
            this.original = original;
            return this;
        }

        public Builder overlayLocation(ResourceLocation overlayLocation) {
            this.location = overlayLocation;
            return this;
        }

        public Builder background(String background) {
            this.background = background;
            return this;
        }

        public Builder shade(boolean shade) {
            this.shade = true;
            return this;
        }

        public Builder threshold(float threshold) {
            this.threshold = Optional.of(threshold);
            return this;
        }
    }
}