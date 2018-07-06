package personthecat.mod.util;

public class NameReader
{
	/*
	 * To-do: get rid of even more of this class.
	 */
	
	//I was doing these things a lot.
	public static String getOre(String name)
	{	
		name = name.replace(".", "~").toLowerCase().replaceAll("tile~", "").replaceAll("~name", "");
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
	
	@Deprecated
	public static String getOreIgnoreAllVariants(String name)
	{
		return getOre(name).replaceAll("dense_", "").replaceAll("lit_redstone_ore", "redstone_ore");
	}
	
	@Deprecated
	public static String getMod(String name)
	{
		String[] nameSplit = getOreIgnoreAllVariants(name).split("_");

		return (nameSplit.length == 3) ? nameSplit[0] : "vanilla";
	}
	
	@Deprecated
	public static String getOreWithoutMod(String name)
	{
		return getOreIgnoreAllVariants(name).replaceAll(getMod(name) + "_", "");
	}
	
	public static String getEndOfPath(String path)
	{
		String[] nameFinder = path.split("/");
		
		return nameFinder[nameFinder.length - 1];
	}
}
