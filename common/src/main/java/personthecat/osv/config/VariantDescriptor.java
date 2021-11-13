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

    BlockEntry entry;
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
        return OreVariant.createPlatformVariant(this.foreground, properties, config);
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

    public VariantItem generateItem(final String key, final BlockState state) {
        final Block fg = ((OreVariant) state.getBlock()).getFg();
        return new VariantItem(key, state, this.foreground.generateBehavior(fg.asItem(), state));
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof VariantDescriptor) {
            return this.getId().equals(((VariantDescriptor) o).getId());
        }
        return false;
    }

    @Override
    public String toString() {
        return "VariantDescriptor[ " + this.foreground + " -> " + this.background + " ]";
    }
}
