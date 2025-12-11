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

package melky.resourcepacks.features.overrides;

import javax.inject.Inject;
import javax.inject.Singleton;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.WidgetResize;
import melky.resourcepacks.event.UpdateAllOverrides;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.packs.PacksManager;
import net.runelite.api.Client;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class WidgetDimensionOverride extends OverrideAction
{
	@Inject
	private Client client;

	@Inject
	private ResourcePacksConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PacksManager packsManager;

	@Inject
	private Overrides overrides;

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return false;
	}

	@Override
	public void startUp()
	{
	}

	@Override
	public void shutDown()
	{
		clientThread.invokeLater(this::reset);
	}

	@Subscribe
	public void onUpdateAllOverrides(UpdateAllOverrides event)
	{
		reset();
		apply();
	}

	@Override
	public void reset()
	{
		adjustWidgetDimensions(false);
	}

	@Override
	public void apply()
	{
		adjustWidgetDimensions(true);
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		apply();
	}

	void adjustWidgetDimensions(boolean modify)
	{
		for (WidgetResize widgetResize : WidgetResize.values())
		{
			Widget widget = client.getWidget(widgetResize.getComponent());

			if (widget != null)
			{
				if (widgetResize.getOriginalX() != null)
				{
					widget.setOriginalX(modify ? widgetResize.getModifiedX() : widgetResize.getOriginalX());
				}

				if (widgetResize.getOriginalY() != null)
				{
					widget.setOriginalY(modify ? widgetResize.getModifiedY() : widgetResize.getOriginalY());
				}

				if (widgetResize.getOriginalWidth() != null)
				{
					widget.setOriginalWidth(modify ? widgetResize.getModifiedWidth() : widgetResize.getOriginalWidth());
				}

				if (widgetResize.getOriginalHeight() != null)
				{
					widget.setOriginalHeight(modify ? widgetResize.getModifiedHeight() : widgetResize.getOriginalHeight());
				}

				widget.revalidate();
			}
		}
	}
}
