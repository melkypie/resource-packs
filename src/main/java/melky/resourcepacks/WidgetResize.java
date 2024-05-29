package melky.resourcepacks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.annotations.Component;

@Getter
@RequiredArgsConstructor
public enum WidgetResize
{
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
