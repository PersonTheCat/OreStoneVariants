package personthecat.osv.io;

import java.util.Iterator;

public class PathSet implements Iterable<String> {
    public final String normal;
    public final String shaded;
    public final String dense;

    public PathSet(final String path) {
        this.normal = OsvPaths.normalize(path);
        this.shaded = OsvPaths.normalToShaded(this.normal);
        this.dense = OsvPaths.normalToDense(this.normal);
    }

    public Iterator<String> iterator() {
        return new Iterator<String>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < 3;
            }

            @Override
            public String next() {
                switch (i++) {
                    case 0: return normal;
                    case 1: return shaded;
                    case 2: return dense;
                    default: return null;
                }
            }
        };
    }
}
