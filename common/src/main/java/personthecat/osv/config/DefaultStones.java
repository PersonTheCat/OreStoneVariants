package personthecat.osv.config;

import lombok.experimental.UtilityClass;
import personthecat.osv.util.Group;

@UtilityClass
public class DefaultStones {

    public static final Group[] LISTED = {
        Group.named("byg").withEntries("rocky_stone", "scoria_stone", "soapstone"),
        Group.named("create").withEntries("dolomite", "gabbro", "limestone", "scoria", "weathered_limestone"),
        Group.named("minecraft").withEntries("stone", "andesite", "diorite", "granite"),
        Group.named("quark").withEntries("jasper", "limestone", "marble", "slate")
    };

    public static final Group[] UNLISTED = {
        Group.named("minecraft").withEntries("gravel", "magma_block", "blackstone"),
        Group.named("quark").withEntries("myalite", "voidstone")
    };
}
