package com.personthecat.orestonevariants.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.io.File;

import static com.personthecat.orestonevariants.io.SafeFileIO.*;
import static com.personthecat.orestonevariants.util.CommonMethods.*;

/**
 * Generates references to files on the fly on the command line.
 * Todo: This would be useful in a separate MC library.
 */
public class FileArgument implements ArgumentType<File> {

    public final File dir;

    public FileArgument(File dir) {
        this.dir = dir;
    }

    @Override
    public File parse(StringReader reader) throws CommandSyntaxException {
        final String path;
        if (reader.peek() == '"') {
            path = reader.readQuotedString();
        } else {
            final int start = reader.getCursor();
            while (reader.canRead() && inPath(reader.peek())) {
                reader.skip();
            }
            path = reader.getString().substring(start, reader.getCursor());
        }
        return lazyFile(dir, path);
    }

    private static boolean inPath(char c) {
        return c == '/' || StringReader.isAllowedInUnquotedString(c);
    }

    /** Retrieves files without needing extensions. */
    private static File lazyFile(File dir, String path) {
        final File test = new File(dir, path);
        if (fileExists(test, "FS error.")) {
            return test;
        }
        for (File f : safeListFiles(test.getParentFile())) {
            if (test.getName().equals(noExtension(f))) {
                return f;
            }
        }
        return test;
    }
}