package personthecat.osv.preset.reader;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.hjson.JsonValue;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.util.HjsonUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static personthecat.catlib.serialization.CodecUtils.dynamic;
import static personthecat.catlib.serialization.DynamicField.field;

public class ComponentReader {

    public static final Codec<Component> CODEC = dynamic(ComponentBuilder::new, ComponentBuilder::build).create(
        field(Codec.STRING, "text", Component::getContents, ComponentBuilder::setText),
        field(Codec.BOOL, "underlined", c -> c.getStyle().isUnderlined(), ComponentBuilder::setUnderlined),
        field(Codec.BOOL, "bold", c -> c.getStyle().isBold(), ComponentBuilder::setBold),
        field(Codec.BOOL, "italic", c -> c.getStyle().isItalic(), ComponentBuilder::setItalic),
        field(ResourceLocation.CODEC, "font", c -> c.getStyle().getFont(), ComponentBuilder::setFont),
        field(Codec.STRING, "color", ComponentReader::getColor, ComponentBuilder::setColor)
    );

    public static final Map<String, Object> DEFAULT_DENSE =
        ImmutableMap.<String, Object>builder()
            .put("text", "{osv.denseKey} {fg} ({bg})")
            .build();

    public static final Map<String, Object> DEFAULT_NORMAL =
        ImmutableMap.<String, Object>builder()
            .put("text", "{fg} ({bg})")
            .build();

    private static final Pattern KEY_PATTERN = Pattern.compile("(?<!\\\\)\\{([^}]+)}");

    @Nullable
    private static String getColor(final Component c) {
        final TextColor color = c.getStyle().getColor();
        if (color == null) return null;
        return color.serialize();
    }

    public static Component fromRaw(final Map<?, ?> raw) {
        return HjsonUtils.readThrowing(CODEC, JsonValue.valueOf(raw));
    }

    public static MutableComponent translateAny(final String text) {
        final MutableComponent component = new TextComponent("");
        if (text.isEmpty()) return component;

        final Matcher matcher = KEY_PATTERN.matcher(text);
        int end = 0;

        while (matcher.find()) {
            final String key = matcher.group(1);
            component.append(new TextComponent(text.substring(end, matcher.start())));
            component.append(new TranslatableComponent(key));
            end = matcher.end();
        }

        if (end == 0) return new TextComponent(text);

        return component.append(new TextComponent(text.substring(end)));
    }

    public static String unescape(final String text) {
        return text.replace("\\{", "{");
    }

    private static class ComponentBuilder {
        String text = "";
        Style style = Style.EMPTY;

        void setText(final String text) {
            this.text = text;
        }

        void setUnderlined(final boolean underlined) {
            this.style = this.style.withUnderlined(underlined);
        }

        void setBold(final boolean bold) {
            this.style = this.style.withBold(bold);
        }

        void setItalic(final boolean italic) {
            this.style = this.style.withItalic(italic);
        }

        void setFont(final ResourceLocation font) {
            this.style = this.style.withFont(font);
        }

        void setColor(final String color) {
            this.style = this.style.withColor(TextColor.parseColor(color));
        }

        Component build() {
            return new TextComponent(text).setStyle(style);
        }
    }
}
