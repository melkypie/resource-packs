/*
 * Copyright (c) 2025, Ron Young <https://github.com/raiyni>
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package melky.resourcepacks.features.widgettracker;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.util.ColorUtil;

@Data
public class WidgetState
{
	private int id;
	private int index = -1;
	private int color = -1;
	private int type = -1;
	private int opacity = -1;
	private int spriteId = -1;
	private boolean hidden = false;
	private boolean empty = false;

	public WidgetState(int id, Widget widget)
	{
		this.id = id;
		if (widget == null)
		{
			empty = true;
			return;
		}

		this.index = widget.getIndex();
		this.color = widget.getTextColor();
		this.type = widget.getType();
		this.opacity = widget.getOpacity();
		this.spriteId = widget.getSpriteId();
		this.hidden = widget.isHidden();
	}

	public String getName()
	{
		String name = WidgetUtil.componentToInterface(id) + "." + WidgetUtil.componentToId(id);
		if (index != -1)
		{
			name += " [" + index + "]";
		}

		return name;
	}

	public boolean hasChanged(Widget current)
	{
		if (current == null && isEmpty())
		{
			return false;
		}

		if (current == null  || isEmpty())
		{
			return true;
		}

		return current.getId() != getId() ||
			current.getIndex() != getIndex() ||
			current.getTextColor() != getColor() ||
			current.getType() != getType() ||
			current.getOpacity() != getOpacity() ||
			current.getSpriteId() != getSpriteId();
	}
}
