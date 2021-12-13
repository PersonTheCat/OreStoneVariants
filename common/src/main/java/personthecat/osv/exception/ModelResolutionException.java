package personthecat.osv.exception;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.exception.FormattedException;

public class ModelResolutionException extends FormattedException {

    private final ResourceLocation model;

    public ModelResolutionException(final ResourceLocation model) {
        super("Could not resolve required model: " + model);
        this.model = model;
    }

    @Override
    public @NotNull String getCategory() {
        return "osv.errorMenu.models";
    }

    @Override
    public @NotNull Component getDisplayMessage() {
        return new TextComponent(this.model.toString());
    }

    @Override
    public @NotNull Component getTitleMessage() {
        return new TranslatableComponent("osv.errorText.couldNotReadModel", this.model.toString());
    }

    @Override
    public @Nullable Component getTooltip() {
        return new TextComponent(this.getLocalizedMessage());
    }
}
