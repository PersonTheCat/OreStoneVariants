package personthecat.osv.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class SphereFeature extends Feature<SphereConfig> {

    public static final SphereFeature INSTANCE = new SphereFeature();

    private SphereFeature() {
        super(SphereConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<SphereConfig> ctx) {
        final double radX = ctx.config().radiusX.rand(ctx.random());
        final double radY = ctx.config().radiusY.rand(ctx.random());
        final double radZ = ctx.config().radiusZ.rand(ctx.random());
        final double radX2 = radX * radX;
        final double radY2 = radY * radY;
        final double radZ2 = radZ * radZ;
        final int minY = Math.max(0, ctx.origin().getY() - (int) radY);
        final int maxY = Math.min(255, ctx.origin().getY() + (int) radY);
        final BlockPos.MutableBlockPos mutable = ctx.origin().mutable();
        int count = 0;

        for (int x = ctx.origin().getX() - (int) radX; x <= ctx.origin().getX() + (int) radX; x++) {
            final double distX = x - ctx.origin().getX();
            final double distX2 = distX * distX;

            for (int z = ctx.origin().getZ() - (int) radZ; z <= ctx.origin().getZ() + (int) radZ; z++) {
                final double distZ = z - ctx.origin().getZ();
                final double distZ2 = distZ * distZ;

                for (int y = minY; y <= maxY; y++) {
                    final double distY = y - ctx.origin().getY();
                    final double distY2 = distY * distY;

                    if (distX2 / radX2 + distY2 / radY2 + distZ2 / radZ2 <= 1.0) {
                        if (ctx.config().integrity == 1.0 || ctx.random().nextDouble() <= ctx.config().integrity) {
                            if (ctx.config().placer.place(ctx.level(), ctx.random(), mutable.set(x, y, z))) {
                                count++;
                            }
                        }
                    }
                }
            }
        }
        return count > 0;
    }
}
