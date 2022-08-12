package personthecat.osv.config;

import lombok.experimental.UtilityClass;
import personthecat.osv.util.Group;

@UtilityClass
public class DefaultStones {

    public static final Group[] LISTED = {
        Group.named("create").withEntries("dolomite", "gabbro", "limestone", "natural_scoria", "weathered_limestone").implicitNamespace(),
        Group.named("minecraft").withEntries("stone", "andesite", "diorite", "granite", "deepslate")
    };

    public static final Group[] UNLISTED = {
        Group.named("minecraft").withEntries("gravel", "magma_block", "blackstone")
    };

    public static final String[] NAMES = { "minecraft" };
}
