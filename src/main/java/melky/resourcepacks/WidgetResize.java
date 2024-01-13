package melky.resourcepacks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.annotations.Component;
import net.runelite.api.widgets.ComponentID;

@Getter
@RequiredArgsConstructor
public enum WidgetResize
{

	RESIZABLE_QUESTS_TAB(ComponentID.RESIZABLE_VIEWPORT_QUESTS_TAB, null, null, null, null, 33, null, 38, null),
	RESIZABLE_IGNORES_TAB(ComponentID.RESIZABLE_VIEWPORT_IGNORES_TAB, null, null, null, null, 33, null, 38, null),
	FIXED_QUESTS_TAB(ComponentID.FIXED_VIEWPORT_QUESTS_TAB, null, null, null, null, 33, null, 38, null),
	FIXED_IGNORES_TAB(ComponentID.FIXED_VIEWPORT_IGNORES_TAB, null, null, null, null, 33, null, 38, null),
	;

	@Component
	private final int component;
	private final Integer modifiedX;
	private final Integer modifiedY;
	private final Integer originalX;
	private final Integer originalY;
	private final Integer modifiedWidth;
	private final Integer modifiedHeight;
	private final Integer originalWidth;
	private final Integer originalHeight;
}
