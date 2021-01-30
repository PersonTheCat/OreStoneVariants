package com.personthecat.orestonevariants.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.personthecat.orestonevariants.properties.OreProperties;
import com.personthecat.orestonevariants.util.Lazy;
import com.personthecat.orestonevariants.util.PathTools;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import org.hjson.JsonObject;

import java.io.File;
import java.util.stream.Stream;

import static com.personthecat.orestonevariants.util.CommonMethods.cmdEx;
import static com.personthecat.orestonevariants.util.CommonMethods.extension;
import static com.personthecat.orestonevariants.util.CommonMethods.getOSVDir;
import static com.personthecat.orestonevariants.util.HjsonTools.readJson;

public class HjsonArgument implements ArgumentType<HjsonArgument.Result> {

    public static void register() {
        ArgumentTypes.register("osv:hjson_argument", HjsonArgument.class, new ArgumentSerializer<>(HjsonArgument::OSV));
        ArgumentTypes.register("osv:variant_argument", HjsonArgument.Ore.class, new ArgumentSerializer<>(HjsonArgument::ore));
    }

    private final FileArgument getter;
    public HjsonArgument(File dir) {
        this.getter = new FileArgument(dir);
    }

    public static HjsonArgument OSV() {
        return new HjsonArgument(getOSVDir());
    }

    public static HjsonArgument.Ore ore() {
        return new HjsonArgument.Ore(OreProperties.DIR);
    }

    @Override
    public HjsonArgument.Result parse(StringReader reader) throws CommandSyntaxException {
        final File f = getter.parse(reader);
        if (f.exists() && !(f.isDirectory() || extension(f).endsWith("json"))) {
            throw cmdEx(reader, "Unsupported format");
        }
        return new HjsonArgument.Result(getter.dir, f);
    }

    public static class Ore extends HjsonArgument {
        private Ore(File dir) {
            super(dir);
        }
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