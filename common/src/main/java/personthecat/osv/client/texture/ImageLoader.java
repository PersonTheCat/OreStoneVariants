package personthecat.osv.client.texture;

import net.minecraft.resources.ResourceLocation;
import personthecat.catlib.util.PathUtils;
import personthecat.fresult.Result;
import personthecat.osv.client.ClientResourceHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class ImageLoader {

    static Optional<Color[][]> loadColors(final ResourceLocation id) {
        return loadImage(PathUtils.asTexturePath(id)).map(ImageUtils::getColors);
    }

    static Optional<Color[][]> loadColors(final String path) {
        return loadImage(path).map(ImageUtils::getColors);
    }

    static Optional<BufferedImage> loadImage(final String path) {
        return ClientResourceHelper.locateResource(path).flatMap(is -> readImage(is).get());
    }

    static Result<BufferedImage, IOException> readImage(final InputStream is) {
        return Result.of(() -> ImageIO.read(is)).ifErr(Result::IGNORE);
    }
}
