package melky.resourcepacks;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ResourcePacksPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ResourcePacksPlugin.class);
		RuneLite.main(args);
	}
}