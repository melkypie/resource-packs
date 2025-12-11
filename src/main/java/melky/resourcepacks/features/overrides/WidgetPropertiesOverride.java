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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.event.UpdateAllOverrides;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.overrides.model.WidgetOverride;
import melky.resourcepacks.features.packs.PacksManager;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginMessage;

@Slf4j
@Singleton
public class WidgetPropertiesOverride extends OverrideAction
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

	@Inject
	private EventBus eventBus;

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return !packsManager.isPackPathEmpty();
	}

	@Override
	public void startUp()
	{
		reload();

		clientThread.invokeLater(this::apply);
	}

	@Override
	public void shutDown()
	{
		clientThread.invokeLater(this::reset);
	}

	@Subscribe
	public void onUpdateAllOverrides(UpdateAllOverrides event)
	{
		startUp();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (!overrides.isEmpty() && overrides.contains(event.getScriptId()))
		{
			for (var widgetOverride : overrides.get(event.getScriptId()))
			{
				addPropertyToWidget(widgetOverride, false);
			}
		}
	}

	@Override
	public void reset()
	{
		log.debug("resetting widget overrides");

		for (WidgetOverride widgetOverride : overrides.values())
		{
			addPropertyToWidget(widgetOverride, true);
		}

		overrides.clear();
	}

	@Override
	public void apply()
	{
		if (overrides.isEmpty())
		{
			return;
		}

		for (WidgetOverride widgetOverride : overrides.values())
		{
			addPropertyToWidget(widgetOverride, false);
		}
	}


	public void addPropertyToWidget(WidgetOverride widgetOverride, boolean reset)
	{
		Widget widgetToOverride = client.getWidget(widgetOverride.getInterfaceId(), widgetOverride.getChildId());
		if (widgetToOverride == null)
		{
			return;
		}

		if (!widgetOverride.getDynamicChildren().isEmpty())
		{
			for (var arrayId : widgetOverride.getDynamicChildren())
			{
				if (arrayId < 0)
				{
					var tmp = widgetToOverride.getDynamicChildren().length - (arrayId * -1);
					log.debug("rewriting index {} to {} for {}", arrayId, tmp, widgetOverride);
					arrayId = tmp;
				}

				Widget arrayWidget = widgetToOverride.getChild(arrayId);
				if (arrayWidget == null)
				{
					continue;
				}

				applyWidgetProperties(arrayWidget, widgetOverride, reset);
			}
		}
		else if (widgetOverride.isAllChildren())
		{
			for (var widget : widgetToOverride.getDynamicChildren())
			{
				if (widget == null)
				{
					continue;
				}

				applyWidgetProperties(widget, widgetOverride, reset);
			}
		}
		else
		{
			applyWidgetProperties(widgetToOverride, widgetOverride, reset);
		}
	}

	private void applyWidgetProperties(Widget widget, WidgetOverride widgetOverride, boolean reset)
	{
		if (widget == null || widget.getTextColor() == -1)
		{
			return;
		}

		int oldColor = widgetOverride.getColor();
		int newColor = widgetOverride.getNewColor();

		if (reset)
		{
			widget.setTextColor(oldColor);

			if (widget.getType() == widgetOverride.getNewType())
			{
				widget.setType(widgetOverride.getType());
			}

			if (widgetOverride.getOpacity() > -1)
			{
				widget.setOpacity(widgetOverride.getOpacity());
			}

			return;
		}

		if (widget.getTextColor() == newColor ||
			!widgetOverride.checkVarbit(client) ||
			typeCompare(widgetOverride, widget) ||
			explicitCompare(widgetOverride, widget))
		{
			return;
		}

		if (widgetOverride.isActiveWidget())
		{
			var w = client.getScriptActiveWidget();
			if (w == null || w.getId() != widget.getId() || w.getTextColor() != widgetOverride.getColor())
			{
				return;
			}

			widget = w;
		}


		if (widgetOverride.getColor() != widgetOverride.getNewColor())
		{
			widget.setTextColor(widgetOverride.getNewColor());
		}

		if (widgetOverride.getNewOpacity() > -1)
		{
			widget.setOpacity(widgetOverride.getNewOpacity());
		}

		if (widgetOverride.getNewType() > -1)
		{
			log.debug("{} overriding widget type {} to {}", widget.getId(), widget.getType(), widgetOverride.getNewType());
			widget.setType(widgetOverride.getNewType());
			if (widgetOverride.getNewType() == 3)
			{
				widget.setFilled(true);
			}
		}
	}

	void reload()
	{
		try
		{
			File overridesFile = Path.of(packsManager.getCurrentPackPath(), "overrides.toml").toFile();
			if (overridesFile.exists())
			{
				var data = com.google.common.io.Files.asCharSource(overridesFile, Charset.defaultCharset()).read();
				overrides.buildOverrides(data);
			}
			else
			{
				log.debug("overrides.toml not found, trying color.properties");
				var backwardsMap = new Properties();
				var properties = new Properties();

				File propertiesFile = Path.of(packsManager.getCurrentPackPath(), "color.properties").toFile();
				try (var is = new FileInputStream(propertiesFile); var is2 = PacksManager.class.getResourceAsStream("/overrides/backwards-map.properties"))
				{
					properties.load(is);
					backwardsMap.load(is2);
				}

				var lines = new ArrayList<String>();
				for (var entry : backwardsMap.entrySet())
				{
					if (properties.containsKey(entry.getValue()))
					{
						lines.add("[" + entry.getKey() + "]");
						lines.add("color=" + properties.get(entry.getValue()));
					}
				}

				log.debug("built {}", String.join("\n", lines));
				overrides.buildOverrides(String.join("\n", lines));
			}

			eventBus.post(new PluginMessage("resource-packs", "pack-loaded"));
		}
		catch (IOException e)
		{
			log.debug("error loading color overrides", e);


			overrides.buildOverrides("");
//			resetOverlayColor();
		}
	}

	private static boolean typeCompare(WidgetOverride widgetOverride, Widget widget)
	{
		return widgetOverride.getType() > -1 && (widget.getType() != widgetOverride.getType() && widgetOverride.getNewType() != widget.getType());
	}

	private static boolean explicitCompare(WidgetOverride widgetOverride, Widget widget)
	{
		return widgetOverride.isExplicit() &&
			(widget.getTextColor() != widgetOverride.getColor() ||
				(widgetOverride.getType() > -1 && widget.getType() != widgetOverride.getType()) ||
				(widgetOverride.getOpacity() > -1 && widget.getOpacity() != widgetOverride.getOpacity())
			);
	}
}
