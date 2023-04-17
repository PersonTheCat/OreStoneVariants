package personthecat.osv.preset.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import personthecat.catlib.registry.CommonRegistries;
import personthecat.osv.block.AdditionalProperties;
import personthecat.osv.config.Cfg;
import personthecat.osv.item.VariantItem;
import personthecat.osv.util.Reference;

import java.util.Objects;
import java.util.function.Function;

import static personthecat.catlib.serialization.codec.CodecUtils.codecOf;
import static personthecat.catlib.serialization.codec.FieldDescriptor.nullable;

@Value
@FieldNameConstants
public class RecipeSettings implements DynamicSerializable<RecipeSettings> {

    @Nullable ResourceLocation result;
    @Nullable String group;
    @Nullable Integer time;
    @Nullable Float xp;
    @Nullable Integer count;

    private static final Codec<RecipeSettings> OBJECT_CODEC = codecOf(
        nullable(ResourceLocation.CODEC, Fields.result, RecipeSettings::getResult),
        nullable(Codec.STRING, Fields.group, RecipeSettings::getGroup),
        nullable(Codec.INT, Fields.time, RecipeSettings::getTime),
        nullable(Codec.FLOAT, Fields.xp, RecipeSettings::getXp),
        nullable(Codec.INT, Fields.count, RecipeSettings::getCount),
        RecipeSettings::new
    );

    public static final String NONE_KEY = "NONE";

    public static final RecipeSettings NONE = new RecipeSettings(new ResourceLocation("air"), null, null, null, 0);

    private static Function<String, DataResult<String>> NONE_VALIDATOR =
        s -> s.equalsIgnoreCase(NONE_KEY) ? DataResult.success(s) : DataResult.error("Unknown constant");

    private static final Codec<String> NONE_CODEC = Codec.STRING.flatXmap(NONE_VALIDATOR, NONE_VALIDATOR);

    public static final Codec<RecipeSettings> CODEC = Codec.either(OBJECT_CODEC, NONE_CODEC).xmap(
        either -> either.map(Function.identity(), s -> NONE),
        recipe -> recipe == NONE ? Either.right(NONE_KEY) : Either.left(recipe)
    );

    public static final RecipeSettings EMPTY = new RecipeSettings(null, null, null, null, null);
    public static final Checked NONE_CHECKED = new Checked(Ingredient.of(new ItemLike[0]), Items.AIR, "", 200, 0, 0);

    public static RecipeSettings fromChecked(final Checked checked) {
        if (checked == null) return EMPTY;

        final ResourceLocation result = CommonRegistries.ITEMS.getKey(checked.result);
        return new RecipeSettings(result, checked.group, checked.time, checked.xp, checked.count);
    }

    public boolean isNone() {
        return this == NONE;
    }

    public boolean isSufficient() {
        return this.result != null;
    }

    public Checked checked(final String defaultGroup, final Block originalInput) {
        final ResourceLocation item = Objects.requireNonNull(this.result, "Unchecked item");
        final Ingredient original = Ingredient.of(originalInput.asItem());
        final String group = this.group != null ? this.group : defaultGroup;
        final int time = this.time != null ? this.time : 200;
        final float xp = this.xp != null ? this.xp : 1.0F;
        final int count = this.count != null ? this.count : 1;
        final Item result = Objects.requireNonNull(CommonRegistries.ITEMS.lookup(item), "No such item: " + item);
        return new Checked(original, result, group, time, xp, count);
    }

    @Override
    public Codec<RecipeSettings> codec() {
        return CODEC;
    }

    @Value
    public static class Checked {
        @NotNull Ingredient original;
        @NotNull Item result;
        @NotNull String group;
        int time;
        float xp;
        int count;

        public boolean matches(final ItemStack item) {
            return this.original.test(item);
        }

        public boolean isNone() {
            return this == NONE_CHECKED;
        }

        public AbstractCookingRecipe getRecipe(final ResourceLocation inputId, final VariantItem input, final boolean blasting) {
            final String affix = blasting ? "blasting" : "smelting";
            final ResourceLocation id = new ResourceLocation(Reference.MOD_ID, inputId.getPath() + "_" + affix);
            final Ingredient ingredient = Ingredient.of(input);
            int quantity = this.count;
            if (AdditionalProperties.isDense(input.getState())) {
                quantity *= Cfg.denseSmeltMultiplier();
            }
            final float xp = quantity * this.xp;
            final ItemStack result = new ItemStack(this.result, quantity);
            final int time = Math.max(1, this.time) / (blasting ? 2 : 1);

            if (blasting) {
                return new BlastingRecipe(id, this.group, ingredient, result, xp, time);
            } else {
                return new SmeltingRecipe(id, this.group, ingredient, result, xp, time);
            }
        }
    }
}
