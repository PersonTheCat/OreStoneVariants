package personthecat.mod.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyGroup
{
	private List<OreProperties> propertiesList = new ArrayList<OreProperties>();
	private boolean conditions;
	private String modName;
	
	public static final List<PropertyGroup> PROPERTY_GROUP_REGISTRY = new ArrayList<PropertyGroup>();
	public static final Map<String, PropertyGroup> PROPERTY_GROUP_MAP = new HashMap<String, PropertyGroup>();
	
	public PropertyGroup(String modName)
	{
		this.modName = modName;
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
	
	public void register()
	{
		PROPERTY_GROUP_REGISTRY.add(this);
		PROPERTY_GROUP_MAP.put(modName, this);
	}
}
