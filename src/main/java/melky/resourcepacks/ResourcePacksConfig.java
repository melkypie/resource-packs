package melky.resourcepacks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("resourcepacks")
public interface ResourcePacksConfig extends Config
{
	@ConfigItem(
		keyName = "resourcePackPath",
		name = "Resource pack path",
		description = "Path to the resource pack which you want to use (without the ending /)"
	)
	default String resourcePackPath()
	{
		return "";
	}
}
