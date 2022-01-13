package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import personthecat.catlib.event.registry.DynamicRegistries;
import personthecat.catlib.exception.FormattedException;

public class EmptyFeatureException extends FormattedException {

    private final ResourceLocation id;

    public EmptyFeatureException(final ConfiguredFeature<?, ?> cf) {
        this(getId(cf));
    }

    public EmptyFeatureException(final ResourceLocation id) {
        super(id + " contains no feature");
        this.id = id;
    }

    private static ResourceLocation getId(final ConfiguredFeature<?, ?> cf) {
        return DynamicRegistries.CONFIGURED_FEATURES.getKey(cf);
    }

    @Override
    public void onErrorReceived(final Logger log) {
        log.error("Feature configuration is empty. This should never happen and must be fixed: {}", id);
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.compat";
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(String.valueOf(this.id));
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return new TranslatableComponent("osv.errorText.emptyFeature", id);
    }
}
