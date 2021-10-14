package personthecat.catlib.util;

import java.util.Iterator;
import java.util.function.BiConsumer;

public class ShorthandMod {
    public static <T, U> void biConsume(final Iterable<T> t, final Iterable<U> u, final BiConsumer<T, U> fn) {
        final Iterator<T> tIterator = t.iterator();
        final Iterator<U> uIterator = u.iterator();

        while (tIterator.hasNext() && uIterator.hasNext()) {
            fn.accept(tIterator.next(), uIterator.next());
        }
    }
}
