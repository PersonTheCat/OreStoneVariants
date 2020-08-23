package com.personthecat.orestonevariants.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import org.apache.commons.lang3.CharUtils;

import java.util.ArrayList;
import java.util.List;

import static com.personthecat.orestonevariants.util.CommonMethods.*;

public class PathArgument implements ArgumentType<PathArgumentResult> {

    public static void register() {
        ArgumentTypes.register("osv:path_argument", PathArgument.class, new ArgumentSerializer<>(PathArgument::new));
    }

    @Override
    public PathArgumentResult parse(StringReader reader) throws CommandSyntaxException {
        final List<Either<String, Integer>> path = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
        final StringBuilder debug = new StringBuilder();

        while (reader.canRead() && reader.peek() != ' ') {
            final char c = reader.read();
            debug.append(c);

            if (c == '.') {
                if (sb.length() == 0 && !endsInNumber(path)) {
                    error("Expected a key.", debug.toString(), reader.getCursor());
                }
                resetLeft(path, sb);
            } else if (c == '[') {
                if (sb.length() == 0) {
                    error("Array access without a key.", debug.toString(), reader.getCursor());
                }
                resetLeft(path, sb);
                path.add(Either.right(reader.readInt()));
                reader.expect(']');
            } else if (!CharUtils.isAsciiAlphanumeric(c)) {
                error("Invalid character.", debug.toString(), reader.getCursor());
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            resetLeft(path, sb);
        }
        return new PathArgumentResult(path);
    }

    private static void resetLeft(List<Either<String, Integer>> path, StringBuilder sb) {
        path.add(Either.left(sb.toString()));
        sb.delete(0, sb.length());
    }

    private static boolean endsInNumber(List<Either<String, Integer>> path) {
        return path.get(path.size() - 1).right().isPresent();
    }

    // Todo: clean up dynamic type.
    private static void error(String msg, String input, int index) throws CommandSyntaxException {
        throw new CommandSyntaxException(dynamic(msg), literal(msg), input, index);
    }

    private static CommandExceptionType dynamic(String gen) {
        return new DynamicCommandExceptionType(val -> literal(f(gen, val)));
    }

    private static Message literal(String msg) {
        return new LiteralMessage(msg);
    }
}