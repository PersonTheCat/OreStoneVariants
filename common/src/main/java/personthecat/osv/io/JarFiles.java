package personthecat.osv.io;

import lombok.extern.log4j.Log4j2;
import personthecat.catlib.data.JsonType;
import personthecat.catlib.io.FileIO;
import personthecat.fresult.Result;
import personthecat.osv.config.DefaultOres;
import personthecat.osv.config.DefaultStones;
import personthecat.osv.util.Group;
import personthecat.osv.util.Reference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static personthecat.catlib.util.PathUtils.filename;
import static personthecat.catlib.util.PathUtils.noExtension;
import static personthecat.catlib.util.Shorthand.f;
import static personthecat.osv.io.ModFolders.*;

@Log4j2
public class JarFiles {

    public static final String TUTORIAL = "TUTORIAL.hjson";
    private static final String PACK_MCMETA = "pack.mcmeta";
    private static final String PACK_MCMETA_PATH = "assets/" + Reference.MOD_ID + "/" + PACK_MCMETA;

    public static void copyFiles() {
        FileIO.mkdirsOrThrow(ORE_DIR, STONE_DIR);

        final List<String> ores = new ArrayList<>();
        for (final Group g : DefaultOres.LISTED) ores.addAll(g.filenames());
        for (final Group g : DefaultOres.UNLISTED) ores.addAll(g.filenames());
        ores.add(TUTORIAL);

        final List<String> stones = new ArrayList<>();
        for (final Group g : DefaultStones.LISTED) stones.addAll(g.filenames());
        for (final Group g : DefaultStones.UNLISTED) stones.addAll(g.filenames());
        stones.remove("minecraft/stone.hjson");
        stones.add(TUTORIAL);

        copyPresets(ORE_DIR, ores);
        copyPresets(STONE_DIR, stones);
        copyMcMeta();
    }

    private static void copyPresets(final File dir, final Collection<String> filenames) {
        final List<String> existing = getAllPresets(dir);

        for (final String name : filenames) {
            if (!existing.contains(noExtension(filename(name)))) {
                final String from = f("data/{}/{}/{}", Reference.MOD_ID, dir.getName(), name);
                final String to = f("{}/{}", dir.getPath(), name);

                log.info("copying from [{}] to [{}]", from, to);
                copyFile(from, to);
            }
        }
    }

    private static List<String> getAllPresets(File dir) {
        final List<File> files = FileIO.listFilesRecursive(dir);
        if (files.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> names = new ArrayList<>();
        for (final File f : files) {
            if (JsonType.isSupported(f)) {
                names.add(noExtension(f));
            }
        }
        return names;
    }

    private static void copyMcMeta() {
        final File file = ResourceHelper.file(PACK_MCMETA);
        FileIO.mkdirsOrThrow(RESOURCE_DIR);

        if (!FileIO.fileExists(file)) {
            copyFile(PACK_MCMETA_PATH, file.getPath());
        }
    }

    private static void copyFile(final String from, final String to) {
        final File dir = new File(to).getParentFile();
        FileIO.mkdirsOrThrow(dir);
        Result.with(() -> new FileOutputStream(to), fos -> {
            final InputStream toCopy = FileIO.getRequiredResource(from);
            FileIO.copyStream(toCopy, fos).throwIfErr();
        }).expect("Error copying {}", from);
    }
}
