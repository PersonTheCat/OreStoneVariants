package personthecat.osv.io;

import lombok.AllArgsConstructor;
import personthecat.fresult.functions.ThrowingSupplier;

import java.io.IOException;
import java.io.InputStream;

@AllArgsConstructor
public class FileSpec {
    public final ThrowingSupplier<InputStream, IOException> is;
    public final String path;
}
