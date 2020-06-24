package melky.resourcepacks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ResourcePacksConfig.GROUP_NAME)
public interface ResourcePacksConfig extends Config
{
	String GROUP_NAME = "resourcepacks";
	String HUB_RESOURCEPACKS = "hubPacks";


	enum ResourcePack
	{
		FIRST,
		SECOND,
		THIRD,
		HUB
	}

	@ConfigItem(
		keyName = "resourcePack",
		name = "Use resource pack",
		description = "Select which resource pack you want to use",
		position = 1
	)
	default ResourcePack resourcePack()
	{
		return ResourcePack.FIRST;
	}

	@ConfigItem(
		keyName = "resourcePackPath",
		name = "Resource pack path 1",
		description = "Path to the first resource pack which you want to use (without the ending /)",
		position = 2
	)
	default String resourcePackPath()
	{
		return "";
	}

	@ConfigItem(
		keyName = "resourcePack2Path",
		name = "Resource pack path 2",
		description = "Path to the second resource pack which you want to use (without the ending /)",
		position = 3
	)
	default String resourcePack2Path()
	{
		return "";
	}

	@ConfigItem(
		keyName = "resourcePack3Path",
		name = "Resource pack path 3",
		description = "Path to the third resource pack which you want to use (without the ending /)",
		position = 4
	)
	default String resourcePack3Path()
	{
		return "";
	}

	@ConfigItem(
		keyName = "selectedHubPack",
		name = "Selected pack in hub",
		description = "Internal name of the selected pack from the hub",
		hidden = true
	)
	default String selectedHubPack()
	{
		return "";
	}
}
