package com.personthecat.orestonevariants.util;

import lombok.AllArgsConstructor;
import net.minecraft.util.text.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A bare-bones Cave expression linter for displaying some JSON data in the chat.
 *
 * Ported from Cave Generator. This class will eventually be moved into a common library.
 *
 * This class is <em>not intended</em> to be a foolproof utility. It is only
 * designed for a few scenarios and can highlight keys and documentation.
 */
public class HjsonLinter {

    /** Identifies multiline documentation comments. Just because. */
    private static final Pattern MULTILINE_DOC = Pattern.compile("/\\*\\*[\\s\\S]*?\\*/", Pattern.DOTALL);

    /** Identifies multiline / inline comments to be highlighted. */
    private static final Pattern MULTILINE_COMMENT = Pattern.compile("/\\*[\\s\\S]*?\\*/", Pattern.DOTALL);

    /** Identifies todos in single line comments. Just because. */
    private static final Pattern LINE_TODO = Pattern.compile("(?:#|//).*(?:todo|to-do).*$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    /** Identifies single line documentation comments. Just because. */
    private static final Pattern LINE_DOC = Pattern.compile("(?:#!|///).*$", Pattern.MULTILINE);

    /** Identifies single line comments to be highlighted. */
    private static final Pattern LINE_COMMENT = Pattern.compile("(?:#|//).*$", Pattern.MULTILINE);

    /** Identifies all other keys to be highlighted. */
    private static final Pattern KEY = Pattern.compile("(\"[\\w\\s]*\"|\\w+)\\s*:|[-_\\w./]+\\s*::\\s*\\w+|.*\\s[aA][sS]\\s+\\w+", Pattern.MULTILINE);


    public static ITextComponent lint(String text) {
        final IFormattableTextComponent formatted = new StringTextComponent("");
        final Context ctx = new Context(text);

        Scope s;
        int i = 0;
        while ((s = ctx.next(i)) != null) {
            final int start = s.matcher.start();
            final int end = s.matcher.end();
            ctx.skipTo(end);

            if (start - i > 0) {
                // Append unformatted text;
                formatted.append(stc(text.substring(i, start)));
            }
            formatted.append(stc(text.substring(start, end)).setStyle(s.style));

            i = end;
        }

        return formatted.append(stc(text.substring(i)));
    }

    private static IFormattableTextComponent stc(String s) {
        return new StringTextComponent(s);
    }

    private static class Context {
        static final Target[] TARGETS = {
            new Target(MULTILINE_DOC, color(TextFormatting.DARK_GREEN).setItalic(true)),
            new Target(LINE_TODO, color(TextFormatting.YELLOW)),
            new Target(LINE_DOC, color(TextFormatting.DARK_GREEN).setItalic(true)),
            new Target(MULTILINE_COMMENT, color(TextFormatting.GRAY)),
            new Target(LINE_COMMENT, color(TextFormatting.GRAY)),
            new Target(KEY, color(TextFormatting.AQUA))
        };

        final List<Scope> scopes = new ArrayList<>();
        final String text;

        Context(String text) {
            this.text = text;
            for (Target t : TARGETS) {
                final Matcher matcher = t.pattern.matcher(text);
                this.scopes.add(new Scope(matcher, t.style, matcher.find()));
            }
        }

        @Nullable
        Scope next(int i) {
            // Figure out whether any other matches have been found;
            int start = Integer.MAX_VALUE;
            Scope first = null;
            for (Scope s : this.scopes) {
                if (!s.found) continue;
                final int mStart = s.matcher.start();

                if (mStart >= i && mStart < start) {
                    start = mStart;
                    first = s;
                }
            }
            return first;
        }

        void skipTo(int i) {
            for (Scope s : this.scopes) {
                if (!s.found) continue;
                if (s.matcher.end() <= i) {
                    s.next();
                }
            }
        }
    }

    @AllArgsConstructor
    private static class Target {
        final Pattern pattern;
        final Style style;
    }

    @AllArgsConstructor
    private static class Scope {
        final Matcher matcher;
        final Style style;
        boolean found;

        void next() {
            this.found = this.matcher.find();
        }
    }

    private static Style color(TextFormatting color) {
        return Style.EMPTY.setColor(Color.fromTextFormatting(color));
    }
}
