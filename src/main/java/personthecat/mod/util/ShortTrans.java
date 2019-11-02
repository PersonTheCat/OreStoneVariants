package personthecat.mod.util;

import net.minecraft.util.text.TextComponentTranslation;

/**
 * This class is barely used since 4.0.
 * Consider removing it.
 */
public class ShortTrans extends TextComponentTranslation
{
    public ShortTrans(String translationKey, Object[] args)
    {
        super(translationKey, args);
    }

    public static boolean canTranslate(String text)
    {
        if (!unformatted(text).equals(text)) return true;

        return false;
    }

    public static String fullUnformatted(String text)
    {
        return new TextComponentTranslation(text).getUnformattedText();
    }

    public String fullUnformatted()
    {
        return getUnformattedText();
    }

    public static String unformatted(String text)
    {
        return new TextComponentTranslation(text).getUnformattedComponentText();
    }

    public String unformatted()
    {
        return getUnformattedComponentText();
    }

    public static String formatted(String text)
    {
        return new TextComponentTranslation(text).getFormattedText();
    }

    public String formatted()
    {
        return getFormattedText();
    }
}
