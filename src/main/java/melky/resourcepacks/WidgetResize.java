package melky.resourcepacks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.widgets.WidgetInfo;

@Getter
@AllArgsConstructor
enum WidgetResize
{
	RESIZABLE_QUESTS_TAB(WidgetInfo.RESIZABLE_VIEWPORT_QUESTS_TAB),
	RESIZABLE_IGNORES_TAB(WidgetInfo.RESIZABLE_VIEWPORT_IGNORES_TAB),
	FIXED_QUESTS_TAB(WidgetInfo.FIXED_VIEWPORT_QUESTS_TAB),
	FIXED_IGNORES_TAB(WidgetInfo.FIXED_VIEWPORT_IGNORES_TAB),
	;

	private final WidgetInfo widgetInfo;
}
