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

package melky.resourcepacks.features.widgettracker.event;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;
import melky.resourcepacks.features.widgettracker.WidgetState;
import net.runelite.client.util.ColorUtil;

@Value
public class WidgetChanged
{
	long timestamp;
	int scriptId;
	WidgetState oldState;
	WidgetState newState;

	public List<String> getDiff()
	{
		List<String> changes = new ArrayList<>();

		if (!oldState.isEmpty() && newState.isEmpty())
		{
			changes.add(oldState.getName() + " → null");
			return changes;
		}

		if (oldState.getId() != newState.getId() || oldState.getIndex() != newState.getIndex())
		{
			changes.add("id: " + oldState.getName() + " → " + newState.getName());
		}

		if (oldState.getColor() != newState.getColor())
		{
			var oldColor = "0x" + ColorUtil.colorToHexCode(new Color(oldState.getColor()));
			var newColor = "0x" + ColorUtil.colorToHexCode(new Color(newState.getColor()));

			changes.add("color: " + oldColor + " → " + newColor);
		}

		if (oldState.getType() != newState.getType())
		{
			changes.add("type: " + oldState.getType() + " → " + newState.getType());
		}

		if (oldState.getOpacity() != newState.getOpacity())
		{
			changes.add("opacity: " + oldState.getOpacity() + " → " + newState.getOpacity());
		}

		if (oldState.getSpriteId() != newState.getSpriteId())
		{
			changes.add("spriteId: " + oldState.getSpriteId() + " → " + newState.getSpriteId());
		}

		if (oldState.isHidden() != newState.isHidden())
		{
			changes.add("hidden: " + oldState.isHidden() + " → " + newState.isHidden());
		}

		return changes;
	}
}
