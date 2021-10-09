package personthecat.osv.config;

import lombok.Value;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import personthecat.catlib.data.Lazy;
import personthecat.catlib.event.registry.RegistryHandle;
import personthecat.osv.util.VariantNamingService;
import personthecat.osv.block.SharedStateBlock;
import personthecat.osv.preset.OrePreset;

import java.util.Objects;

@Value
public class VariantDescriptor {

    String path;
    OrePreset foreground;
    ResourceLocation background;

    Lazy<ResourceLocation> id = Lazy.of(() -> VariantNamingService.create(this.getPath(), this.getBackground()));

    public boolean canLoad(final RegistryHandle<Block> blocks) {
        return blocks.isRegistered(this.background) && blocks.isRegistered(this.foreground.getOreId());
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

    public SharedStateBlock generateBlock(final RegistryHandle<Block> blocks) {
        final Block bg = this.resolveBackground(blocks);
        final Block fg = this.resolveForeground(blocks);
        final BlockBehaviour.Properties properties = this.foreground.generateBehavior(bg, fg);
        return SharedStateBlock.createPlatformVariant(bg, fg, properties);
    }

    private Block resolveBackground(final RegistryHandle<Block> blocks) {
        return Objects.requireNonNull(blocks.lookup(this.background), "Background not in registry: " + this.background);
    }

    private Block resolveForeground(final RegistryHandle<Block> blocks) {
        return Objects.requireNonNull(blocks.lookup(this.foreground.getOreId()), "Foreground not in registry: " +  this.foreground.getOreId());
    }
}
