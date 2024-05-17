package melky.resourcepacks;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(ResourcePacksConfig.GROUP_NAME)
public interface ResourcePacksConfig extends Config
{
	String GROUP_NAME = "resourcepacks";
	String HUB_RESOURCEPACKS = "hubPacks";
	String ORIGINAL_OVERLAY_COLOR = "originalOverlayColor";

	enum ResourcePack
	{
		FIRST,
		SECOND,
		THIRD,
		HUB
	}

	@ConfigSection(name = "Resource pack paths",
		description = "Contains resource pack paths",
		position = 2
	)
	String resourcePackPaths = "resourcePackPaths";

	@ConfigSection(name = "Experimental options",
		description = "Do not touch if you don't know what you are doing",
		position = 9,
		closedByDefault = true
	)
	String experimentalOptions = "experimentalOptions";

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
		keyName = "resourcePack",
		name = "",
		description = ""
	)
	void resourcePack(ResourcePack pack);

	@ConfigItem(
		keyName = "resourcePackPath",
		name = "Resource pack path 1",
		description = "Path to the first resource pack which you want to use (without the ending /)",
		position = 2,
		section = resourcePackPaths
	)
	default String resourcePackPath()
	{
		return "";
	}

	@ConfigItem(
		keyName = "resourcePack2Path",
		name = "Resource pack path 2",
		description = "Path to the second resource pack which you want to use (without the ending /)",
		position = 3,
		section = resourcePackPaths
	)
	default String resourcePack2Path()
	{
		return "";
	}

	@ConfigItem(
		keyName = "resourcePack3Path",
		name = "Resource pack path 3",
		description = "Path to the third resource pack which you want to use (without the ending /)",
		position = 4,
		section = resourcePackPaths
	)
	default String resourcePack3Path()
	{
		return "";
	}

	@ConfigItem(
		keyName = "hideSidePanelButton",
		name = "Hide side panel button",
		description = "Allows you to hide the side panel button to reduce clutter when not changing packs frequently",
		position = 5
	)
	default boolean hideSidePanelButton()
	{
		return false;
	}

	@ConfigItem(
		keyName = "allowLoginScreen",
		name = "Allow login screen to be changed",
		description = "Gives permissions for resource packs to change your login screen",
		position = 6
	)
	default boolean allowLoginScreen()
	{
		return true;
	}

	@ConfigItem(
		keyName = "allowOverlayColor",
		name = "Allow overlay color to be changed",
		description = "Gives permissions for resource packs to change your overlays color",
		position = 7
	)
	default boolean allowOverlayColor()
	{
		return true;
	}

	@ConfigItem(
		keyName = "allowSpellsPrayers",
		name = "Allow spells/prayers to be changed",
		description = "Gives permissions for resource packs to change your spells and prayers icons",
		position = 8
	)
	default boolean allowSpellsPrayers()
	{
		return true;
	}

	@ConfigItem(
		keyName = "allowCrossSprites",
		name = "Allow mouse click sprite to be changed",
		description = "Allows the cross/mouse click sprites to be changed (This won't work if you have cross sprites enabled in Interface styles)",
		position = 9
	)
	default boolean allowCrossSprites()
	{
		return true;
	}

	@ConfigItem(
		keyName = "disableInterfaceStylesPrompt",
		name = "Don't change Interface Styles gameframe option",
		description = "Turning this option on will disable resource packs changing the Interface Styles gameframe option to default",
		position = 10
	)
	default boolean disableInterfaceStylesPrompt()
	{
		return false;
	}

	@ConfigItem(
		keyName = "displayWarnings",
		name = "Display Warnings",
		description = "Display warnings when changing a setting in Runelite that would conflict/be blocked with resource packs",
		position = 10
	)
	default boolean displayWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "allowColorPack",
		name = "Enables color current pack option",
		description = "This option must be on for Color current pack option to work",
		position = 11,
		section = experimentalOptions
	)
	default boolean allowColorPack()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "colorPack",
		name = "Color current pack",
		description = "Allows you to apply a color overlay over the currently selected resource pack",
		position = 12,
		section = experimentalOptions
	)
	Color colorPack();

	@ConfigItem(
		keyName = "colorPackOverlay",
		name = "Allows color current pack to change overlays",
		description = "This option will only work if color current pack is enabled and a color is assigned",
		position = 13,
		section = experimentalOptions
	)
	default boolean colorPackOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = HUB_RESOURCEPACKS,
		name = "",
		description = "",
		hidden = true
	)
	default String hubPacks()
	{
		return "";
	}

	@ConfigItem(
		keyName = HUB_RESOURCEPACKS,
		name = "",
		description = ""
	)
	void hubPacks(String packs);

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

	@ConfigItem(
		keyName = "selectedHubPack",
		name = "",
		description = ""
	)
	void selectedHubPack(String pack);

	@ConfigItem(
		keyName = ORIGINAL_OVERLAY_COLOR,
		name = "",
		description = "",
		hidden = true
	)
	default String originalOverlayColor()
	{
		return "";
	}

	@ConfigItem(
		keyName = ORIGINAL_OVERLAY_COLOR,
		name = "",
		description = ""
	)
	void originalOverlayColor(String color);
}
