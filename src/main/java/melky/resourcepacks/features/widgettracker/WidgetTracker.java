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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.features.widgettracker.event.WidgetChanged;
import melky.resourcepacks.features.widgettracker.event.WidgetTracked;
import melky.resourcepacks.features.widgettracker.event.WidgetUntracked;
import melky.resourcepacks.module.PluginLifecycleComponent;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class WidgetTracker implements PluginLifecycleComponent
{
	@Inject
	private WidgetSelector widgetSelector;

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	private final Table<Integer, Integer, WidgetState> trackedWidgets = HashBasedTable.create();

	private final Set<Integer> blacklist = ImmutableSet.of(
		2512,
		3174,
		1004,
		3350,
		5935,
		5939,
		2100,
		4730,
		4671
	);

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return widgetSelector.isEnabled(config);
	}

	@Subscribe
	public void onWidgetTracked(WidgetTracked event)
	{
		var w = event.getWidget();
		if (trackedWidgets.contains(w.getId(), w.getIndex()))
		{
			return;
		}

		trackedWidgets.put(w.getId(), w.getIndex(), w);

		log.debug("Tracking {}", event.getWidget().getName());
		log.debug("{}", trackedWidgets.values());
	}

	@Subscribe
	public void onWidgetUntracked(WidgetUntracked event)
	{
		log.debug("Untracking {}", event.getWidget().getName());

		var w = event.getWidget();
		trackedWidgets.remove(w.getId(), w.getIndex());
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (trackedWidgets.isEmpty() || blacklist.contains(event.getScriptId()))
		{
			return;
		}

		for (var r : trackedWidgets.rowKeySet())
		{
			var parent = client.getWidget(r);

			for (var i : trackedWidgets.row(r).keySet())
			{
				var tracked = trackedWidgets.get(r, i);
				var widget = parent;
				if (i > -1 && widget != null)
				{
					widget = widget.getChild(i);
					if (widget == null)
					{
						log.debug("child is null? {} {}", i, parent.getChildren());
					}
				}

				if (!tracked.hasChanged(widget))
				{
					continue;
				}

				log.debug("{} changed for {}", widget, tracked);

				var newState = new WidgetState(r, widget);
				var change = new WidgetChanged(System.currentTimeMillis(), event.getScriptId(), tracked, newState);

				eventBus.post(change);

				log.debug("Saving ({}, {}) = {}", r, i, newState);
				trackedWidgets.put(r, i, newState);
			}
		}
	}
}
