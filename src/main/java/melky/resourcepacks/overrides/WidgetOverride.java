/*
 * Copyright (c) 2024, Ron Young <https://github.com/raiyni>
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

package melky.resourcepacks.overrides;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import net.runelite.api.Client;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class WidgetOverride
{
	String name;

	int script = -1;
	int interfaceId = -1;
	int childId = -1;

	int type = -1;
	int newType = -1;

	int opacity = -1;
	int newOpacity = -1;

	int color = -1;
	int newColor = -1;

	List<Map.Entry<Integer, Integer>> varbits = new ArrayList<>();
	List<Integer> dynamicChildren = new ArrayList<>();

	boolean allChildren = false;
	boolean activeWidget = false;
	boolean explicit = false;

	public boolean isValid()
	{
		return script > -1 &&
			interfaceId > -1 &&
			childId > -1 &&
			color > -1;
	}

	public boolean checkVarbit(final Client client)
	{
		if (varbits.isEmpty())
		{
			return true;
		}

		for (var matcher : varbits)
		{
			if (client.getVarbitValue(matcher.getKey()) != matcher.getValue())
			{
				return false;
			}
		}

		return true;
	}
}
