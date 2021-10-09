package personthecat.osv.command;

import personthecat.catlib.command.CommandContextWrapper;
import personthecat.catlib.command.annotations.ModCommand;
import personthecat.catlib.command.annotations.Node;

public class CommandOsv {

    @ModCommand(name = "debug", branch = @Node(name = "test"))
    private static void debugTest(final CommandContextWrapper ctx) {
        ctx.sendMessage("Hello, world!");
    }
}
