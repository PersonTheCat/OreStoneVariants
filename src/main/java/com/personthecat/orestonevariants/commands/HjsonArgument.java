package com.personthecat.orestonevariants.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import org.hjson.JsonObject;

import java.io.File;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.*;
import static com.personthecat.orestonevariants.util.HjsonTools.*;

public class HjsonArgument implements ArgumentType<HjsonArgument.Result> {

    public static void register() {
        ArgumentTypes.register("osv:hjson_argument", HjsonArgument.class, new ArgumentSerializer<>(HjsonArgument::OSV));
    }

    private final FileArgument getter;
    public HjsonArgument(File dir) {
        this.getter = new FileArgument(dir);
    }

    public static HjsonArgument OSV() {
        return new HjsonArgument(getOSVDir());
    }

    @Override
    public HjsonArgument.Result parse(StringReader reader) throws CommandSyntaxException {
        return new HjsonArgument.Result(getter.dir, getter.parse(reader));
    }

    public static class Result {

        private final File root;
        public final File file;
        public final Lazy<JsonObject> json;

        private Result(File root, File file) {
            this.root = root;
            this.file = file;
            this.json = new Lazy<>(() -> {
                synchronized(this) {
                    return readJson(file).orElseGet(JsonObject::new);
                }
            });
        }

        public Stream<String> getNeighbors() {
            return PathTools.getSimpleContents(root, file);
        }
    }
}