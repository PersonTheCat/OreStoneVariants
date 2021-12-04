package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.exception.FormattedException;

public class VariantLoadException extends FormattedException {

    private final ResourceLocation id;

    public VariantLoadException(final ResourceLocation id, final Throwable cause) {
        super("Error loading " + id, cause);
        this.id = id;
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.variants";
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(this.id.getPath());
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return new TranslatableComponent("osv.errorText.variantError", this.id.toString());
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TextComponent(this.getCause().getMessage());
    }
}
