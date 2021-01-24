package com.personthecat.orestonevariants.util;

/** A utility to make sure that an event only happens once. */
public class RunOnce implements Runnable {

    private final Runnable wrapped;
    private boolean finished = false;

    private RunOnce(Runnable toWrap) {
        this.wrapped = toWrap;
    }

    public static RunOnce wrap(Runnable other) {
        return new RunOnce(other);
    }

    @Override
    public synchronized void run() {
        if (!finished) {
            wrapped.run();
            finished = true;
        }
    }
}
