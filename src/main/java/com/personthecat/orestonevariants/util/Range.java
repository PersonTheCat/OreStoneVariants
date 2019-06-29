package com.personthecat.orestonevariants.util;

import java.util.Iterator;
import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class Range implements Iterable<Integer> {
    public final int min, max;

    public Range(int a, int b) {
        if (b > a) {
            min = a;
            max = b;
        } else {
            min = b;
            max = a;
        }
    }

    public Range(int max) {
        this(0, max);
    }

    public int rand(Random rand) {
        return numBetween(rand, min, max);
    }

    public int rand() {
        return numBetween(new Random(), min, max);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int i = min;

            @Override
            public boolean hasNext() {
                return i < max;
            }

            @Override
            public Integer next() {
                return i++;
            }
        };
    }
}