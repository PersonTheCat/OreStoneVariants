package com.personthecat.orestonevariants.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import org.apache.commons.lang3.CharUtils;

import java.util.ArrayList;
import java.util.List;

import static com.personthecat.orestonevariants.util.CommonMethods.cmdEx;

public class PathArgument implements ArgumentType<PathArgument.Result> {

    public static void register() {
        ArgumentTypes.register("osv:path_argument", PathArgument.class, new ArgumentSerializer<>(PathArgument::new));
    }

    public static String serialize(List<Either<String, Integer>> path) {
        final StringBuilder sb = new StringBuilder();
        for (Either<String, Integer> either : path) {
            either.ifLeft(s -> {
                sb.append('.');
                sb.append(s);
            });
            either.ifRight(i -> {
                sb.append('[');
                sb.append(i);
                sb.append(']');
            });
        }
        final String s = sb.toString();
        return s.startsWith(".") ? s.substring(1) : s;
    }

    @Override
    public Result parse(StringReader reader) throws CommandSyntaxException {
        final List<Either<String, Integer>> path = new ArrayList<>();
        final int begin = reader.getCursor();

        while(reader.canRead() && reader.peek() != ' ') {
            final char c = reader.read();
            if (c == '.') {
                checkDot(reader, begin);
            } else if (CharUtils.isAsciiAlphanumeric(c)) {
                path.add(Either.left(c + readKey(reader)));
            } else if (c == '[') {
                checkDot(reader, begin);
                path.add(Either.right(reader.readInt()));
                reader.expect(']');
            } else {
                throw cmdEx(reader,"Invalid character");
            }
        }
        return new Result(path);
    }

    private static String readKey(StringReader reader) {
        final int start = reader.getCursor();
        while (reader.canRead() && inKey(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    private static boolean inKey(char c) {
        return c != '.' && CharUtils.isAsciiAlphanumeric(c);
    }

    private static void checkDot(StringReader reader, int begin) throws CommandSyntaxException {
        final int cursor = reader.getCursor();
        final char last = reader.getString().charAt(cursor - 2);
        if (cursor - 1 == begin || last == '.') {
            throw cmdEx(reader,"Unexpected accessor");
        }
    }

    /** Provides a concrete wrapper for path arguments. */
    public static class Result {

        public final List<Either<String, Integer>> path;

        public Result(List<Either<String, Integer>> path) {
            this.path = path;
        }
    }
}