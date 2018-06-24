package personthecat.mod.util;

public class NameReader
{
	//I was doing these things a lot.
	public static String getOre(String name)
	{		
		name = name.toLowerCase().replaceAll("tile.", "").replaceAll("lit_", "").replaceAll(".name", "").replaceAll("_custom", "");
		String[] nameCorrector = name.split("_");
		
		//Keep taking off the last piece until it reads "ore."
		while (!nameCorrector[nameCorrector.length - 1].equals("ore"))
		{			
			if (nameCorrector.length < 1) return null;
			
			name = nameCorrector[0];
			
			for (int i = 1; i < nameCorrector.length - 1; i++)
			{
				name = name + "_" + nameCorrector[i];
			}
			
			nameCorrector = name.split("_");
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
