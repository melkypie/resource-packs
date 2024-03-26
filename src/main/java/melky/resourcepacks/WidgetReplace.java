package melky.resourcepacks;

import static melky.resourcepacks.WidgetReplace.Constants.*;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.annotations.Component;

@Getter
@RequiredArgsConstructor
public enum WidgetReplace
{
	RESIZABLE_VIEWPORT_CLASSIC(RESIZABLE_VIEWPORT_CLASSIC_COMPONENT_ID, SpriteOverride.RESIZEABLE_MODE_CLASSIC_BACKGROUND.getSpriteID()),
	RESIZABLE_VIEWPORT_MODERN(RESIZABLE_VIEWPORT_MODERN_COMPONENT_ID, SpriteOverride.RESIZEABLE_MODE_MODERN_BACKGROUND.getSpriteID()),

	BANK_BACKGROUND(BANK_BACKGROUND_COMPONENT_ID, new Integer[]{0}, SpriteOverride.BANK_BACKGROUND.getSpriteID()),

	BANK_CONTAINER_SEPARATOR_LINE(BANK_CONTAINER_SEPARATOR_LINE_COMPONENT_ID, BANK_CONTAINER_CHILDREN_INDEX, SpriteOverride.BANK_CONTAINER_SEPARATOR_LINE.getSpriteID()),

	SEED_VAULT_CONTAINER_SEPARATOR_LINE(SEED_VAULT_CONTAINER_SEPARATOR_LINE_COMPONENT_ID, SEED_VAULT_CHILDREN_INDEX, SpriteOverride.BANK_CONTAINER_SEPARATOR_LINE.getSpriteID()),

	EQUIPMENT_SIDE_PANEL_VERTICAL_BAR(EQUIPMENT_VERTICAL_BAR, SpriteOverride.EQUIPMENT_VERTICAL_BAR.getSpriteID()),
	EQUIPMENT_SIDE_PANEL_HORIZONTAL_BAR(EQUIPMENT_HORIZONTAL_BAR, SpriteOverride.EQUIPMENT_HORIZONTAL_BAR.getSpriteID()),

	;

	@Component
	private final int[] componentId;
	private final Integer[] childIndex;
	private final int spriteId;

	WidgetReplace(int componentId, int spriteId)
	{
		this(new int[]{componentId}, new Integer[]{-1}, spriteId);
	}

	WidgetReplace(int[] componentId, int spriteId)
	{
		this(componentId, new Integer[]{-1}, spriteId);
	}

	WidgetReplace(int componentId, Integer[] childIndex, int spriteId)
	{
		this(new int[]{componentId}, childIndex, spriteId);
	}

	static class Constants
	{
		static final int RESIZABLE_VIEWPORT_MODERN_COMPONENT_ID = 10551334;
		static final int RESIZABLE_VIEWPORT_CLASSIC_COMPONENT_ID = 10551334;
		static final int BANK_BACKGROUND_COMPONENT_ID = 786434;
		static final int BANK_CONTAINER_SEPARATOR_LINE_COMPONENT_ID = 786445;
		static final Integer[] BANK_CONTAINER_CHILDREN_INDEX = {
			1220, 1221, 1222, 1223, 1224, 1225, 1226, 1227, 1228
		};

		static final int SEED_VAULT_CONTAINER_SEPARATOR_LINE_COMPONENT_ID = 41353230;
		static final Integer[] SEED_VAULT_CHILDREN_INDEX = {
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
		};
		
		//find all occurrences of worn equipment slots, TODO
		static final int[] EQUIPMENT_VERTICAL_BAR = {
			25362441, 25362442, 25362443,//side panel

			5505029, 5505030, 5505031,//stats

			786503, 786504, 786505,//bank

			49676301, 49676304, 49676305,//pvp_max
			49676333, 49676336, 49676337,//pvp_zerker
			49676365, 49676368, 49676369,//pvp_pure

			42336267, 42336270, 42336271,//lms_max
			42336297, 42336300, 42336301,//lms_zerker
			42336327, 42336330, 42336331,//lms_pure

		};
		static final int[] EQUIPMENT_HORIZONTAL_BAR = {
			25362444, 25362445,//side panel

			5505032, 5505033,//stats

			786506, 786507,//bank

			49676302, 49676303,//pvp_max
			49676334, 49676335,//pvp_zerker
			49676366, 49676367,//pvp_pure

			42336268, 42336269,//lms_max
			42336298, 42336399,//lms_zerker
			42336328, 42336329,//lms_pure

		};
	}
	
}