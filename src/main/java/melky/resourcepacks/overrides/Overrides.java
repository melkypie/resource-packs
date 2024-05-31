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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import static melky.resourcepacks.overrides.OverrideKey.CHILDREN;
import static melky.resourcepacks.overrides.OverrideKey.COLOR;
import static melky.resourcepacks.overrides.OverrideKey.DYNAMIC_CHILDREN;
import static melky.resourcepacks.overrides.OverrideKey.INTERFACE;
import static melky.resourcepacks.overrides.OverrideKey.SCRIPTS;
import static melky.resourcepacks.overrides.OverrideKey.TYPE;
import static melky.resourcepacks.overrides.OverrideKey.VARBIT;
import static melky.resourcepacks.overrides.OverrideKey.VARBIT_VALUE;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

@Slf4j
@Singleton
public class Overrides
{
	private final ListMultimap<Integer, WidgetOverride> widgetOverrides = ArrayListMultimap.create();
	private final Map<String, Object> properties = new HashMap<>();

	public Collection<WidgetOverride> values()
	{
		return widgetOverrides.values();
	}

	public boolean isEmpty()
	{
		return widgetOverrides.isEmpty();
	}

	public List<WidgetOverride> get(int scriptId)
	{
		return widgetOverrides.get(scriptId);
	}

	public boolean contains(int scriptId)
	{
		return widgetOverrides.containsKey(scriptId);
	}

	public void buildOverrides(final String packOverrides)
	{
		clear();

		try (var stream = Overrides.class.getResourceAsStream("/overrides/overrides.toml"))
		{
			assert stream != null;

			TomlParseResult toml = Toml.parse(stream);
			toml.errors().forEach(error -> log.error(error.toString()));

			TomlParseResult pack = Toml.parse(packOverrides);
			pack.errors().forEach(error -> log.error(error.toString()));

			loadProperties(toml, pack);
			var keys = toml.keySet();
			for (var key : keys)
			{
				var table = toml.getTableOrEmpty(key);

				walkChildren(new WidgetOverride().withName(key), table, pack);
			}
		}
		catch (IOException e)
		{
			log.error("error loading overrides", e);
		}
	}

	public void clear()
	{
		widgetOverrides.clear();
		properties.clear();
	}

	public Color getOverlayColor()
	{
		return (Color) properties.get("overlay.color");
	}

	private void loadProperties(TomlParseResult source, TomlParseResult pack)
	{
		if (source.contains("overlay.color"))
		{
			properties.put("overlay.color", new Color(source.getLong("overlay.color").intValue(), true));
		}

		if (pack.contains("overlay.color"))
		{
			properties.put("overlay.color", new Color(pack.getLong("overlay.color").intValue(), true));
		}
	}

	@VisibleForTesting
	protected WidgetOverride walkChildren(WidgetOverride parent, TomlTable table, TomlTable pack)
	{
		var node = parent;
		var map = table.toMap();
		var key = parent.getName();

		if (map.containsKey(INTERFACE.getKey()))
		{
			node = node.withInterfaceId(table.getLong(INTERFACE.getKey()).intValue());
			map.remove(INTERFACE.getKey());
		}

		if (map.containsKey(TYPE.getKey()))
		{
			node = node.withType(table.getLong(TYPE.getKey()).intValue());
			map.remove(TYPE.getKey());
		}

		if (map.containsKey(COLOR.getKey()))
		{
			int c = table.getLong(COLOR.getKey()).intValue();
			if (pack.contains(COLOR.append(key)))
			{
				c = pack.getLong(COLOR.append(key)).intValue();
			}

			node = node.withProperties(new EnumMap<>(node.getProperties()));
			node.getProperties().put(COLOR, c);

			map.remove(COLOR.getKey());
		}

		if (map.containsKey(SCRIPTS.getKey()))
		{
			var scripts = table.getArrayOrEmpty(SCRIPTS.getKey()).toList();
			map.remove(SCRIPTS.getKey());

			for (var script : scripts)
			{
				var clonedMap = new HashMap<>(map);
				var n2 = node.withScript(((Long) script).intValue());
				walkChildren(n2, clonedMap, pack);
			}
		}
		else
		{
			walkChildren(node, map, pack);
		}

		return node;
	}

	@VisibleForTesting
	protected WidgetOverride walkChildren(WidgetOverride parent, Map<String, Object> map, TomlTable pack)
	{
		var node = parent;
		var path = parent.getName();

		if (map.containsKey(COLOR.getKey()))
		{
			int c = ((Long) map.get(COLOR.getKey())).intValue();
			if (pack.contains(COLOR.append(path)))
			{
				c = pack.getLong(COLOR.append(path)).intValue();
			}

			node = node.withProperties(new EnumMap<>(node.getProperties()));
			node.getProperties().put(COLOR, c);
			map.remove(COLOR.getKey());
		}

		if (map.containsKey(TYPE.getKey()))
		{
			node = node.withType(((Long) map.get(TYPE.getKey())).intValue());
			map.remove(TYPE.getKey());
		}

		if (map.containsKey(VARBIT.getKey()))
		{
			var obj = map.get(VARBIT.getKey());
			if (obj instanceof Long)
			{
				var matcher = new WidgetOverride.VarbitMatcher(((Long) map.get(VARBIT.getKey())).intValue(), ((Long) map.get(VARBIT_VALUE.getKey())).intValue());
				node = node.withVarbits(List.of(matcher));
			}
			else if (obj instanceof TomlArray)
			{
				var varbits = ((TomlArray) obj).toList();
				var values = ((TomlArray) map.get(VARBIT_VALUE.getKey())).toList();
				if (varbits.size() != values.size())
				{
					log.error("mis matching varbits size for {}", map);
					return parent;
				}

				var matchers = IntStream.range(0, varbits.size())
					.boxed()
					.map(i -> new WidgetOverride.VarbitMatcher(((Long) varbits.get(i)).intValue(), ((Long) values.get(i)).intValue()))
					.collect(Collectors.toList());
				node = node.withVarbits(matchers);
			}

			map.remove(VARBIT.getKey());
			map.remove(VARBIT_VALUE.getKey());
		}

		if (map.containsKey(CHILDREN.getKey()))
		{
			var children = (TomlArray) map.get(CHILDREN.getKey());
			map.remove(CHILDREN.getKey());

			var list = children.toList();
			for (var child : list)
			{
				var n2 = node.withChildId(((Long) child).intValue());
				var clonedMap = new HashMap<>(map);
				walkChildren(n2, clonedMap, pack);
			}

			// remove dynamic children so we don't walk from empty children
			map.remove(DYNAMIC_CHILDREN.getKey());
		}

		var tableKeys = map.keySet()
			.stream()
			.filter(k -> (map.get(k) instanceof TomlTable || map.get(k) instanceof TomlArray)
				&& !Objects.equal(k, DYNAMIC_CHILDREN.getKey())
				&& !Objects.equal(k, CHILDREN.getKey())
				&& !Objects.equal(k, SCRIPTS.getKey()))
			.collect(Collectors.toSet());

		for (var key : tableKeys)
		{
			var obj = map.get(key);
			if (obj instanceof TomlTable)
			{
				var table = (TomlTable) obj;
				map.remove(key);

				walkChildren(node.withName(node.getName() + "." + key), table, pack);
			}
			else if (obj instanceof TomlArray)
			{
				var array = (TomlArray) obj;
				map.remove(key);

				if (array.get(0) instanceof TomlTable)
				{
					var list = array.toList();
					for (var table : list)
					{
						walkChildren(node.withName(node.getName() + "." + key), (TomlTable) table, pack);
					}
				}
			}
		}

		if (map.containsKey(DYNAMIC_CHILDREN.getKey()))
		{
			var children = (TomlArray) map.get(DYNAMIC_CHILDREN.getKey());
			var list = children.toList().stream()
				.map(l -> ((Long) l).intValue())
				.collect(Collectors.toList());
			node = node.withDynamicChildren(list);
		}

		if (map.containsKey("skip"))
		{
			return parent;
		}

		if (node.isValid())
		{
			log.debug("adding override {}", node);
			widgetOverrides.put(node.getScript(), node);
		}

		return parent;
	}
}
