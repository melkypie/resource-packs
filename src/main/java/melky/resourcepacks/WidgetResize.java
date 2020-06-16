package melky.resourcepacks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;

@Getter
@RequiredArgsConstructor
public enum WidgetResize
{

	GE_BORDER_RIGHT(WidgetID.GRAND_EXCHANGE_GROUP_ID, 20, 340, null, 344, null, null, null, null, null),
	RESIZABLE_QUESTS_TAB(WidgetInfo.RESIZABLE_VIEWPORT_QUESTS_TAB.getGroupId(), WidgetInfo.RESIZABLE_VIEWPORT_QUESTS_TAB.getChildId(), null, null, null, null, 33, null, 38, null),
	RESIZABLE_IGNORES_TAB(WidgetInfo.RESIZABLE_VIEWPORT_IGNORES_TAB.getGroupId(), WidgetInfo.RESIZABLE_VIEWPORT_IGNORES_TAB.getChildId(), null, null, null, null, 33, null, 38, null),
	FIXED_QUESTS_TAB(WidgetInfo.FIXED_VIEWPORT_QUESTS_TAB.getGroupId(), WidgetInfo.FIXED_VIEWPORT_QUESTS_TAB.getChildId(), null, null, null, null, 33, null, 38, null),
	FIXED_IGNORES_TAB(WidgetInfo.FIXED_VIEWPORT_IGNORES_TAB.getGroupId(), WidgetInfo.FIXED_VIEWPORT_IGNORES_TAB.getChildId(), null, null, null, null, 33, null, 38, null),
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
}
