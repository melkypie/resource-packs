package melky.resourcepacks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import static melky.resourcepacks.WidgetResize.Group.*;
import static melky.resourcepacks.WidgetResize.Children.*;

@Getter
@RequiredArgsConstructor
public enum WidgetResize
{

	RESIZABLE_QUESTS_TAB(WidgetInfo.RESIZABLE_VIEWPORT_QUESTS_TAB.getGroupId(), WidgetInfo.RESIZABLE_VIEWPORT_QUESTS_TAB.getChildId(), null, null, null, null, 33, null, 38, null),
	RESIZABLE_IGNORES_TAB(WidgetInfo.RESIZABLE_VIEWPORT_IGNORES_TAB.getGroupId(), WidgetInfo.RESIZABLE_VIEWPORT_IGNORES_TAB.getChildId(), null, null, null, null, 33, null, 38, null),
	FIXED_QUESTS_TAB(WidgetInfo.FIXED_VIEWPORT_QUESTS_TAB.getGroupId(), WidgetInfo.FIXED_VIEWPORT_QUESTS_TAB.getChildId(), null, null, null, null, 33, null, 38, null),
	FIXED_IGNORES_TAB(WidgetInfo.FIXED_VIEWPORT_IGNORES_TAB.getGroupId(), WidgetInfo.FIXED_VIEWPORT_IGNORES_TAB.getChildId(), null, null, null, null, 33, null, 38, null),
	
	//was squished, due to default width and height set to 16x16 instead of 20x18
	FAIRY_RING_SEARCH_ICON(WidgetInfo.FAIRY_RING_HEADER.getGroupId(), new Integer[] {2}, null, null, null, null, 20, 18, 20, 18, 8),

	//reposition ge broder_left 1 pixel in offer screen and collection box
	GRAND_EXCHANGE_OFFER_BORDER_LEFT(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER.getGroupId(), GRAND_EXCHANGE_OFFER_BORDER_LEFT_CHILDREN_IDS, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_COLLECTION_BOX_BORDER_LEFT(GRAND_EXCHANGE_COLLECTION_BOX_GROUP, GRAND_EXCHANGE_COLLECTION_BOX_BORDER_LEFT_CHILDREN_IDS, -13, null, -14, null, null, null, null, null, 7),

	//HOUSE_OPTIONS_BUTTON_CENTER has spriteTiling(true), which causes modifiedWidth/modifiedHeight to change the clipping, not the sprites width/height
	HOUSE_OPTIONS_BUTTON_CENTER(HOUSE_OPTIONS_GROUP, HOUSE_OPTIONS_BUTTON_CHILDREN_IDS, null, null, null, null, null, 30, null, 36, 0),
	HOUSE_OPTIONS_BUTTON_LEFT(HOUSE_OPTIONS_GROUP, HOUSE_OPTIONS_BUTTON_CHILDREN_IDS, null, null, null, null, null, 30, null, 30, 1),
	HOUSE_OPTIONS_BUTTON_RIGHT(HOUSE_OPTIONS_GROUP, HOUSE_OPTIONS_BUTTON_CHILDREN_IDS, null, null, null, null, null, 30, null, 30, 2),

	//was squished, due to default width and height set to 24x24 instead of 26x23
	FOSSIL_POOL_HOPPER_CLOSE_BUTTON(FOSSIL_POOL_HOPPER_GROUP, new Integer[] {2}, -2, -1, null, null, 26, 23, 24, 24, 0),
	FOSSIL_POOL_HOPPER_CLOSE_BUTTON_HOVER(FOSSIL_POOL_HOPPER_GROUP, new Integer[] {2}, -2, -1, null, null, 26, 23, 24, 24, 1),
	;

	private final Integer group;
	private final Integer child;
	private final Integer modifiedX;
	private final Integer modifiedY;
	private final Integer originalX;
	private final Integer originalY;
	private final Integer modifiedWidth;
	private final Integer modifiedHeight;
	private final Integer originalWidth;
	private final Integer originalHeight;
		private final Integer childIndex;

	static class Group {
		static final Integer GRAND_EXCHANGE_COLLECTION_BOX_GROUP = 402;
		static final Integer HOUSE_OPTIONS_GROUP = 370;
		static final Integer FOSSIL_POOL_HOPPER_GROUP = 613;
	}

	static class Children {

		static final Integer[] GRAND_EXCHANGE_OFFER_BORDER_LEFT_CHILDREN_IDS = {
				7, 8, 9, 10, 11, 12, 13, 14
		};
		static final Integer[] GRAND_EXCHANGE_COLLECTION_BOX_BORDER_LEFT_CHILDREN_IDS = {
				5, 6, 7, 8, 9, 10, 11, 12
		};
		static final Integer[] HOUSE_OPTIONS_BUTTON_CHILDREN_IDS = {
				20, 21, 22
		};

	}
}
