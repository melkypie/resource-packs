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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.event.PackParsed;
import melky.resourcepacks.event.ReloadPack;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.overrides.model.OverrideKey;
import static melky.resourcepacks.features.overrides.model.OverrideKey.ACTIVE_WIDGET;
import static melky.resourcepacks.features.overrides.model.OverrideKey.CHILDREN;
import static melky.resourcepacks.features.overrides.model.OverrideKey.COLOR;
import static melky.resourcepacks.features.overrides.model.OverrideKey.DYNAMIC_CHILDREN;
import static melky.resourcepacks.features.overrides.model.OverrideKey.EXPLICIT;
import static melky.resourcepacks.features.overrides.model.OverrideKey.INTERFACE;
import static melky.resourcepacks.features.overrides.model.OverrideKey.NEW_TYPE;
import static melky.resourcepacks.features.overrides.model.OverrideKey.OPACITY;
import static melky.resourcepacks.features.overrides.model.OverrideKey.SCRIPTS;
import static melky.resourcepacks.features.overrides.model.OverrideKey.TYPE;
import static melky.resourcepacks.features.overrides.model.OverrideKey.VARBIT;
import static melky.resourcepacks.features.overrides.model.OverrideKey.VARBIT_VALUE;
import melky.resourcepacks.features.overrides.model.WidgetOverride;
import melky.resourcepacks.features.packs.PacksService;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import org.tomlj.TomlArray;
import org.tomlj.TomlTable;

@Slf4j
@Singleton
public class WidgetPropertiesOverride extends OverrideAction
{
	@Getter
	@VisibleForTesting
	private final Multimap<Integer, WidgetOverride> overrides = ArrayListMultimap.create();

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PacksService packsService;

	@Inject
	private EventBus eventBus;

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return !packsService.isPackPathEmpty();
	}

	@Override
	public void startUp()
	{
		clientThread.invokeLater(this::apply);
	}

	@Override
	public void shutDown()
	{
		clientThread.invokeLater(this::reset);
	}

	@Subscribe
	public void onReloadPack(ReloadPack event)
	{
		startUp();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (!overrides.isEmpty() && overrides.containsKey(event.getScriptId()))
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

	@Subscribe
	public void onPackParsed(PackParsed event)
	{
		var pack = event.getPack();
		if (pack.getSources() == null || pack.getOverrides() == null)
		{
			return;
		}

		var keys = pack.getSources().keySet();
		for (var key : keys)
		{
			var table = pack.getSources().getTableOrEmpty(key);
			walkChildren(new WidgetOverride().withName(key), table, pack.getOverrides());
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

	@VisibleForTesting
	protected WidgetOverride overrideProperties(WidgetOverride parent, Map<String, Object> source, TomlTable override)
	{
		var node = parent;
		var path = parent.getName();

		if (source.containsKey(INTERFACE))
		{
			node = node.withInterfaceId(((Long) source.get(INTERFACE)).intValue());
			source.remove(INTERFACE);
		}

		if (source.containsKey(COLOR))
		{
			int c = ((Long) source.get(COLOR)).intValue();

			if (override.contains(OverrideKey.append(path, COLOR)))
			{
				var v = override.get(OverrideKey.append(path, COLOR));
				if (v instanceof Long)
				{
					node = node.withNewColor(((Long) v).intValue());
				}
			}
			else
			{
				if (node.getColor() == node.getNewColor())
				{
					node = node.withNewColor(c);
				}
			}

			node = node.withColor(c);

			source.remove(COLOR);
		}

		if (source.containsKey(TYPE))
		{
			node = node.withType(((Long) source.get(TYPE)).intValue());
			if (source.containsKey(NEW_TYPE))
			{
				node = node.withNewType(((Long) source.get(NEW_TYPE)).intValue());
				source.remove(NEW_TYPE);
			}

			source.remove(TYPE);
		}

		if (source.containsKey(OPACITY))
		{
			int o = ((Long) source.get(OPACITY)).intValue();
			node = node.withOpacity(o);
			if (override.contains(OverrideKey.append(path, OPACITY)))
			{
				node = node.withNewOpacity(override.getLong(OverrideKey.append(path, OPACITY)).intValue());
			}
			else
			{
				node = node.withNewOpacity(o);
			}

			source.remove(OPACITY);
		}

		if (source.containsKey(EXPLICIT))
		{
			node = node.withExplicit(true);
			source.remove(EXPLICIT);
		}

		if (source.containsKey(ACTIVE_WIDGET))
		{
			node = node.withActiveWidget(true);
			source.remove(ACTIVE_WIDGET);
		}

		return node;
	}

	@VisibleForTesting
	public WidgetOverride walkChildren(WidgetOverride parent, TomlTable source, TomlTable override)
	{
		var node = parent;
		var map = source.toMap();

		node = overrideProperties(node, map, override);

		if (map.containsKey(SCRIPTS))
		{
			var scripts = source.getArrayOrEmpty(SCRIPTS).toList();
			map.remove(SCRIPTS);

			for (var script : scripts)
			{
				var clonedMap = new HashMap<>(map);
				var n2 = node.withScript(((Long) script).intValue());
				walkChildren(n2, clonedMap, override);
			}
		}
		else
		{
			walkChildren(node, map, override);
		}

		return node;
	}

	@VisibleForTesting
	protected WidgetOverride walkChildren(WidgetOverride parent, Map<String, Object> source, TomlTable override)
	{
		var node = parent;
		node = overrideProperties(node, source, override);

		if (source.containsKey(VARBIT))
		{
			var obj = source.get(VARBIT);
			if (obj instanceof Long)
			{
				var matcher = Map.entry(((Long) source.get(VARBIT)).intValue(), ((Long) source.get(VARBIT_VALUE)).intValue());
				node = node.withVarbits(List.of(matcher));
			}
			else if (obj instanceof TomlArray)
			{
				var varbits = ((TomlArray) obj).toList();
				var values = ((TomlArray) source.get(VARBIT_VALUE)).toList();
				if (varbits.size() != values.size())
				{
					log.error("mis matching varbits size for {}", source);
					return parent;
				}

				var matchers = IntStream.range(0, varbits.size())
					.boxed()
					.map(i -> Map.entry(((Long) varbits.get(i)).intValue(), ((Long) values.get(i)).intValue()))
					.collect(Collectors.toList());
				node = node.withVarbits(matchers);
			}

			source.remove(VARBIT);
			source.remove(VARBIT_VALUE);
		}

		if (source.containsKey(CHILDREN))
		{
			var children = (TomlArray) source.get(CHILDREN);
			source.remove(CHILDREN);

			var list = children.toList();
			for (var child : list)
			{
				var n2 = node.withChildId(((Long) child).intValue());
				var clonedMap = new HashMap<>(source);
				walkChildren(n2, clonedMap, override);
			}

			// remove dynamic children so we don't walk from empty children
			source.remove(DYNAMIC_CHILDREN);
		}

		var tableKeys = source.keySet()
			.stream()
			.filter(k -> (source.get(k) instanceof TomlTable || source.get(k) instanceof TomlArray)
				&& !Objects.equal(k, DYNAMIC_CHILDREN)
				&& !Objects.equal(k, CHILDREN)
				&& !Objects.equal(k, SCRIPTS))
			.collect(Collectors.toSet());

		for (var key : tableKeys)
		{
			var obj = source.get(key);
			if (obj instanceof TomlTable)
			{
				var table = (TomlTable) obj;
				source.remove(key);

				walkChildren(node.withName(node.getName() + "." + key), table, override);
			}
			else if (obj instanceof TomlArray)
			{
				var array = (TomlArray) obj;
				source.remove(key);

				if (array.get(0) instanceof TomlTable)
				{
					var list = array.toList();
					for (var table : list)
					{
						walkChildren(node.withName(node.getName() + "." + key), (TomlTable) table, override);
					}
				}
			}
		}

		if (source.containsKey(DYNAMIC_CHILDREN))
		{
			var obj = source.get(DYNAMIC_CHILDREN);
			if (obj instanceof TomlArray)
			{
				var children = (TomlArray) obj;
				var list = children.toList().stream()
					.map(l -> ((Long) l).intValue())
					.collect(Collectors.toList());
				node = node.withDynamicChildren(list);
			}
			else if (obj instanceof Boolean)
			{
				node = node.withAllChildren((boolean) obj);
			}
		}

		if (source.containsKey("skip"))
		{
			return parent;
		}

		if (node.isValid())
		{
			log.debug("adding override {}", node);
			overrides.put(node.getScript(), node);
		}
		else
		{
			if (node.getNewColor() > -1 && node.getInterfaceId() > -1 &&
				node.getChildId() > -1 && node.getScript() == -1)
			{
				log.debug("skipping override {}, no scriptid", node);
			}
		}

		return parent;
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
