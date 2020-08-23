package com.personthecat.orestonevariants.commands;

import com.mojang.datafixers.util.Either;

import java.util.List;

/** Provides a concrete wrapper for path arguments. */
public class PathArgumentResult {

    public final List<Either<String, Integer>> path;

    public PathArgumentResult(List<Either<String, Integer>> path) {
        this.path = path;
    }
}