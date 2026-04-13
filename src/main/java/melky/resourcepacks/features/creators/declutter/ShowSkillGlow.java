/*
 * Copyright (c) 2026, Ron Young <https://github.com/raiyni>
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

package melky.resourcepacks.features.creators.declutter;

import javax.inject.Inject;
import javax.inject.Singleton;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.module.PluginLifecycleComponent;
import net.runelite.api.Client;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class ShowSkillGlow implements PluginLifecycleComponent
{
	@Inject
	private Client client;

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return config.developerTools() && config.showSkillGlow();
	}

	int ticks = 0;

	@Override
	public void shutDown()
	{
		for (int i = 1; i <= 25; i++)
		{
			var widget = client.getWidget(InterfaceID.STATS, i);
			if (widget != null && widget.getChildren() != null)
			{
				widget = widget.getChild(3);
				widget.setOpacity(0);
			}
		}

		ticks = 0;
	}

	@Subscribe
	public void onPostClientTick(PostClientTick postClientTick)
	{
		var tick = ticks++ % 120;
		for (int i = 1; i <= 25; i++)
		{
			var widget = client.getWidget(InterfaceID.STATS, i);
			if (widget != null && widget.getChildren() != null)
			{
				widget = widget.getChild(3);
				if (tick < 60)
				{
					widget.setOpacity(tick * 200 / 60);
				}
				else
				{
					widget.setOpacity((120 - tick) * 200 / 60);
				}
			}
		}
	}
}
