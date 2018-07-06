package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.common.Loader;
import personthecat.mod.config.ConfigFile;

public class PropertyGroup
{
	private boolean conditions = true, isCustom = false;
	private List<OreProperties> propertiesList = new ArrayList<>();
	private String modName;
	
	private static final Map<String, PropertyGroup> PROPERTY_GROUP_MAP = new HashMap<String, PropertyGroup>();
	
	public static final PropertyGroup CUSTOM_PROPERTY_GROUP = new PropertyGroup("custom");
	
	static {CUSTOM_PROPERTY_GROUP.isCustom = true; CUSTOM_PROPERTY_GROUP.register();}
	
	public PropertyGroup(String modName)
	{
		this.modName = modName;
		
		setDefaultConditions();
	}
	
	public static Collection<PropertyGroup> getPropertyGroupRegistry()
	{
		return PROPERTY_GROUP_MAP.values();
	}
	
	public static PropertyGroup[] getSortedPropertyGroups()
	{		
		String[] groupNames = getPropertyGroupNames();
		Arrays.sort(groupNames);
		
		PropertyGroup[] groups = new PropertyGroup[groupNames.length];
		
		for (int i = 0; i < groups.length; i++)
		{
			groups[i] = getPropertyGroup(groupNames[i]);
		}
		
		return groups;
	}
	
	public static String[] getPropertyGroupNames()
	{
		Collection<PropertyGroup> registry = getPropertyGroupRegistry();
		
		String[] groupNames = new String[registry.size()]; 
		
		int index = 0;
		
		for (PropertyGroup group : registry)
		{			
			groupNames[index] = group.getModName();
			
			index++;
		}
		
		return groupNames;
	}
	
	public static PropertyGroup getPropertyGroup(String modName)
	{
		if (modName.equals("vanilla") || modName.equals("base"))
		{
			return PROPERTY_GROUP_MAP.get("minecraft");
		}
		
		return PROPERTY_GROUP_MAP.get(modName);
	}
	
	public static PropertyGroup getGroupByProperties(OreProperties properties)
	{
		if (properties == null) return null;
		
		for (PropertyGroup group : getPropertyGroupRegistry())
		{
			for (OreProperties inGroup : group.getProperties())
			{
				if (inGroup.equals(properties)) return group;
			}
		}
		
		return null;
	}
	
	public String getModName()
	{
		return modName;
	}
	
	public void addProperties(OreProperties properties)
	{
		propertiesList.add(properties);
	}
	
	public List<OreProperties> getProperties()
	{
		return propertiesList;
	}
	
	public void setConditions(boolean conditions)
	{		
		this.conditions = conditions;
	}
	
	public boolean getConditions()
	{
		return conditions || isCustom;
	}
	
	private void setDefaultConditions()
	{
		setConditions(Loader.isModLoaded(modName) && ConfigFile.isSupportEnabled(modName));
	}
	
	public boolean isCustom()
	{
		return isCustom;
	}
	
	public static void unassignProperty(OreProperties property)
	{
		for (PropertyGroup group : PROPERTY_GROUP_MAP.values())
		{
			group.getProperties().remove(property);
		}
	}
	
	public static PropertyGroup locateOrCreateGroup(String name)
	{
		if (PROPERTY_GROUP_MAP.get(name) != null) return PROPERTY_GROUP_MAP.get(name);
		
		PropertyGroup newPropertyGroup = new PropertyGroup(name);
		
		newPropertyGroup.isCustom = true;
		
		newPropertyGroup.register();
		
		return newPropertyGroup;
	}
	
	public void register()
	{
		PROPERTY_GROUP_MAP.put(modName, this);
	}
}
