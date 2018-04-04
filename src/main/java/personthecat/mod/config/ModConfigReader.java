package personthecat.mod.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

//cheap haxx look away pls
public class ModConfigReader
{
	public static boolean quarkLimestoneOn, quarkMarbleOn;	
	
	//I need to read from Quark's config file directly because I refer to some of its assets before they get registered. 
	//This is necessary FOR NOW in order to avoid crashes, sadly. 
	//For the other mods, it doesn't actually matter because I'm not even making references to their blocks. 
	public static void readQuarkConfig()
	{
		File quarkConfigFile = new File(Loader.instance().getConfigDir(), "quark.cfg");
		Configuration quarkConfig = new Configuration(quarkConfigFile);
		quarkConfig.load();
		quarkLimestoneOn = quarkConfig.get("world.revamp stone gen", "Enable Limestone", true).getBoolean();
		quarkMarbleOn = quarkConfig.get("world.revamp stone gen", "Enable Marble", true).getBoolean();
	}
}
