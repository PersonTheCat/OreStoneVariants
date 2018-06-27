package personthecat.mod.util;

public class NameReader
{
	//I was doing these things a lot.
	public static String getOre(String name)
	{	
		name = name.replace(".", "~");                  //This is sloppy, so it needs to go. I'm just too afraid.
		name = name.toLowerCase().replaceAll("tile~", "").replaceAll("lit_redstone_ore", "redstone_ore").replaceAll("~name", "");
		String[] nameCorrector = name.split("_");

		if (nameCorrector.length < 1) return null;
		
		//Keep taking off the last piece until it reads "ore."
		for (int i = nameCorrector.length - 1; i > 0; i--)
		{
			if (!name.endsWith("_ore"))
			{
				int amountToRemove = nameCorrector[i].length() + 1;
				
				name = name.substring(0, name.length() - amountToRemove);
			}
			
			else return name;
		}
		
		return name;
	}
	
	public static String getOreIgnoreDense(String name)
	{
		return getOre(name).replaceAll("dense_", "");
	}
	
	public static String getOreIgnoreAllVariants(String name)
	{
		return getOre(name).replaceAll("dense_", "").replaceAll("lit_redstone_ore", "redstone_ore");
	}
	
	public static String getMod(String name)
	{
		String[] nameSplit = getOreIgnoreAllVariants(name).split("_");

		return (nameSplit.length == 3) ? nameSplit[0] : "vanilla";
	}
	
	public static String getOreWithoutMod(String name)
	{
		return getOreIgnoreDense(name).replaceAll(getMod(name) + "_", "");
	}
	
	public static String getOreFromPath(String path)
	{
		String[] nameFinder = path.split("/");
		
		return nameFinder[nameFinder.length - 1];
	}
}
