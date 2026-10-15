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

package melky.resourcepacks.features.creators.sprites;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.features.creators.events.TrackerBuilt;
import melky.resourcepacks.features.state.LocalConfig;
import melky.resourcepacks.model.SpriteOverride;
import melky.resourcepacks.module.PluginLifecycleComponent;
import net.runelite.api.Client;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@Slf4j
public class SpriteTracker implements PluginLifecycleComponent
{
	public static final boolean TRACKER_DEFAULT_VISIBLE = false;

	public enum TitleMode
	{
		INLINE,
		SPIDERWEB,
		CONDENSED,
		HIDDEN;
	}

	@Data
	@AllArgsConstructor
	public static class HighlightOptions implements Comparable<HighlightOptions>
	{
		private int spriteId;

		@EqualsAndHashCode.Exclude
		private SpriteOverride override;

		@EqualsAndHashCode.Exclude
		private Color color;

		@EqualsAndHashCode.Exclude
		private boolean visible;

		@Override
		public int compareTo(HighlightOptions o1)
		{
			return override.toPath().compareTo(o1.getOverride().toPath());
		}
	}

	private Client client;
	private EventBus eventBus;

	@Getter
	private final Set<Widget> widgets = new LinkedHashSet<>();

	@Getter
	private final Map<Integer, HighlightOptions> highlights = new HashMap<>();

	private int ticksSince = 0;
	private boolean newHighlights = false;

	@Inject
	public SpriteTracker(Client client, EventBus eventBus)
	{
		this.client = client;
		this.eventBus = eventBus;

		for (var s : SpriteOverride.values())
		{
			var id = s.getSpriteID();
			if (id != -1)
			{
				highlights.put(id, new HighlightOptions(id, s, getColorFromHashCode(s), TRACKER_DEFAULT_VISIBLE));
			}
		}
	}

	@Override
	public boolean isEnabled(ResourcePacksConfig config, LocalConfig localConfig)
	{
		return config.developerTools() && (localConfig.getState(SpriteOverlay.class));
	}

	public boolean isEmpty()
	{
		return widgets.isEmpty();
	}

	public void computeTracking(Widget w)
	{
		Integer key = w.getSpriteId();
		if (highlights.containsKey(key))
		{
			widgets.add(w);
		}
	}

	public HighlightOptions getHighlight(int spriteId)
	{
		return highlights.get(spriteId);
	}

	private Color getColorFromHashCode(Object obj)
	{
		if (obj == null)
		{
			return Color.WHITE;
		}

		int hash = obj.hashCode();

		// Use different bits of the hash for RGB components
		int r = (hash & 0xFF0000) >> 16;
		int g = (hash & 0x00FF00) >> 8;
		int b = hash & 0x0000FF;

		return new Color(r, g, b);
	}

	@Override
	public void startUp()
	{
		eventBus.post(new TrackerBuilt());
	}

	@Override
	public void shutDown()
	{
		widgets.clear();
		newHighlights = false;
	}


	@Subscribe
	public void onPostClientTick(PostClientTick event)
	{
		if (ticksSince++ < 5)
		{
			return;
		}

		ticksSince = 0;
		widgets.clear();
		var widgets = client.getWidgetRoots();
		for (var w : widgets)
		{
			walkWidget(w);
		}

		if (newHighlights)
		{
			eventBus.post(new TrackerBuilt());
			newHighlights = false;
		}
	}

	public List<HighlightOptions> getHighlightList()
	{
		return getHighlights().values()
			.stream()
			.sorted()
			.collect(Collectors.toCollection(ArrayList::new));
	}

	private void walkWidget(Widget parent)
	{
		if (parent.getType() == WidgetType.GRAPHIC)
		{
			computeTracking(parent);
			return;
		}

		for (var w : parent.getStaticChildren())
		{
			walkWidget(w);
		}

		for (var w : parent.getNestedChildren())
		{
			walkWidget(w);
		}

		for (var w : parent.getDynamicChildren())
		{
			walkWidget(w);
		}
	}
}
