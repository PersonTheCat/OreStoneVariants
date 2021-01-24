package com.personthecat.orestonevariants.util;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Random;

import static com.personthecat.orestonevariants.util.CommonMethods.f;
import static com.personthecat.orestonevariants.util.CommonMethods.numBetween;

@EqualsAndHashCode
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
        this(max, max);
    }

    public static Range of(int a, int b) {
        return new Range(a, b);
    }

    public static Range of(int max) {
        return new Range(max);
    }

    public int rand(Random rand) {
        return numBetween(rand, min, max);
    }

    @NotNull
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

    @Override
    public String toString() {
        return f("Range[{}-{}]", min, max);
    }
}