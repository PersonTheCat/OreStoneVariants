package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyGroup
{
	private boolean conditions = true;
	private List<OreProperties> propertiesList = new ArrayList<>();
	private String modName;
	
	private static final Map<String, PropertyGroup> PROPERTY_GROUP_MAP = new HashMap<String, PropertyGroup>();
	
	public static final PropertyGroup CUSTOM_PROPERTY_GROUP = new PropertyGroup("custom");
	
	public PropertyGroup(String modName)
	{
		this.modName = modName;
		
		register();
	}
	
	public static Collection<PropertyGroup> getPropertyGroupRegistry()
	{
		return PROPERTY_GROUP_MAP.values();
	}
	
	public static PropertyGroup getPropertyGroup(String modName)
	{
		return PROPERTY_GROUP_MAP.get(modName);
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
		return conditions;
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
		
		return new PropertyGroup(name);
	}
	
	private void register()
	{
		PROPERTY_GROUP_MAP.put(modName, this);
	}
}
