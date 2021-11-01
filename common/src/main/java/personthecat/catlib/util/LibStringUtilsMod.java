package personthecat.catlib.util;

public class LibStringUtilsMod {

    /**
     * Converts a string in lower case or snake case to title case.
     *
     * <p>Note that <b>camel case is not supported</b>. Please submit
     * a PR if you would like this functionality.
     *
     * @param text The text in lower case or snake case.
     * @return The equivalent message in title case.
     */
    public static String toTitleCase(final String text) {
        if (text.isEmpty()) return "";

        final StringBuilder sb = new StringBuilder(text.length());
        boolean capitalize = false;

        sb.append(Character.toUpperCase(text.charAt(0)));

        for (int i = 1; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c == ' ' || c == '_' || c == '-') {
                capitalize = true;
                sb.append(' ');
            } else if (capitalize) {
                sb.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
