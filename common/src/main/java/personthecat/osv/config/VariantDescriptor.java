package personthecat.osv.config;

import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import personthecat.catlib.data.Lazy;
import personthecat.catlib.event.registry.CommonRegistries;
import personthecat.catlib.event.registry.RegistryHandle;
import personthecat.osv.block.AdditionalProperties;
import personthecat.osv.block.OreVariant;
import personthecat.osv.block.StateConfig;
import personthecat.osv.item.VariantItem;
import personthecat.osv.util.VariantNamingService;
import personthecat.osv.preset.OrePreset;

import java.util.Objects;

@Value
public class VariantDescriptor {

    String path;
    OrePreset foreground;
    ResourceLocation background;

    Lazy<ResourceLocation> id = Lazy.of(() -> VariantNamingService.create(this.getPath(), this.getBackground()));

    public boolean canLoad(final RegistryHandle<Block> blocks) {
        if (this.foreground.isCustom() || blocks.isRegistered(this.foreground.getOreId())) {
            return blocks.isRegistered(this.background);
        }
        return false;
    }

    public boolean canLoad(final RegistryHandle<Block> blocks, final ResourceLocation id) {
        if (this.foreground.isCustom()) {
            return blocks.isRegistered(this.background);
        } else if (this.background.equals(id)) {
            return blocks.isRegistered(this.foreground.getOreId());
        } else if (this.foreground.getOreId().equals(id)) {
            return blocks.isRegistered(this.background);
        }
        return false;
    }

    public ResourceLocation getId() {
        return this.id.get();
    }

    public OreVariant generateBlock() {
        final Block bg = this.resolveBackground();
        final Block fg = this.resolveForeground();
        final BlockBehaviour.Properties properties = this.foreground.generateBehavior(bg, fg);
        final StateConfig config = this.foreground.canBeDense()
            ? new StateConfig(bg, fg, AdditionalProperties.DENSE) : new StateConfig(bg, fg);
        return OreVariant.createPlatformVariant(properties, config);
    }

    private Block resolveBackground() {
        final Block lookup = CommonRegistries.BLOCKS.lookup(this.background);
        return Objects.requireNonNull(lookup, "Background not in registry: " + this.background);
    }

    private Block resolveForeground() {
        if (this.foreground.isCustom()) return new Block(BlockBehaviour.Properties.of(Material.STONE));
        final Block lookup = CommonRegistries.BLOCKS.lookup(this.foreground.getOreId());
        return Objects.requireNonNull(lookup, "Foreground not in registry: " +  this.foreground.getOreId());
    }

    public VariantItem generateItem(final BlockState state) {
        final Block fg = ((OreVariant) state.getBlock()).getFg();
        return new VariantItem(state, this.foreground.generateBehavior(fg.asItem()));
    }
}
