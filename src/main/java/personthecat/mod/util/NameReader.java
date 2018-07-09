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
	
	/**
	 * Returns the part of the string that comes immediately before "_ore"
	 */
	public static String getActualName(String name)
	{
		String[] nameSplit = getOre(name).split("_");
		
		if (nameSplit[nameSplit.length - 1].equals("ore"))
		{
			return nameSplit[nameSplit.length - 2];
		}
		
		System.out.println("Error: tried to retrieve actual name from an invalid format.");
		
		return null;
	}
	
	@Deprecated
	public static String getOreIgnoreAllVariants(String name)
	{
		return getOre(name).replaceAll("dense_", "").replaceAll("lit_redstone_ore", "redstone_ore");
	}
	
	public static String getEndOfPath(String path)
	{
		String[] nameFinder = path.split("/");
		
		return nameFinder[nameFinder.length - 1];
	}
}
