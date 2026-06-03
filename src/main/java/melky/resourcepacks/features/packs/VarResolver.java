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

package melky.resourcepacks.features.packs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.tomlj.TomlTable;

@Slf4j
public class VarResolver
{
	private static final Pattern QUOTED_VAR_PATTERN = Pattern.compile("\"\\$\\{([\\w.]+)}\"");

	private final Map<String, String> vars;

	@VisibleForTesting
	public VarResolver(Map<String, String> vars)
	{
		if (vars == null || vars.isEmpty())
		{
			this.vars = vars;
			return;
		}

		Map<String, String> resolved = new LinkedHashMap<>();
		this.vars = resolved;

		for (var entry : vars.entrySet())
		{
			String value = entry.getValue();
			String resolvedValue = resolveValue(value);
			resolved.put(entry.getKey(), resolvedValue);
		}
	}

	public VarResolver(Map<Object, Object> idVars, TomlTable defaultVars, TomlTable userVars)
	{
		Map<String, String> merged = new LinkedHashMap<>();

		if (idVars != null)
		{
			putAll(merged, idVars);
		}

		if (defaultVars != null)
		{
			flattenAndPutAll(merged, defaultVars, "");
		}

		this.vars = merged;

		if (userVars != null)
		{
			flattenAndPutAll(merged, userVars, "");
		}
	}

	public String resolve(String input)
	{
		if (Strings.isNullOrEmpty(input) || vars == null || vars.isEmpty())
		{
			return input;
		}

		Matcher matcher = QUOTED_VAR_PATTERN.matcher(input);
		if (!matcher.find())
		{
			return input;
		}

		matcher.reset();
		StringBuffer sb = new StringBuffer();
		while (matcher.find())
		{
			String name = matcher.group(1);
			String value = vars.get(name);
			if (value == null)
			{
				log.warn("Undefined variable reference: {}", name);
				matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
			}
			else
			{
				Set<String> visiting = new HashSet<>();
				visiting.add(name);
				String deeplyResolved = resolveValueRecursive(value, visiting);
				visiting.remove(name);
				matcher.appendReplacement(sb, Matcher.quoteReplacement(deeplyResolved));
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public String resolveContent(String input)
	{
		if (Strings.isNullOrEmpty(input) || vars == null || vars.isEmpty())
		{
			return input;
		}

		String[] lines = input.split("\n", -1);
		List<String> result = new ArrayList<>();

		for (String line : lines)
		{
			line = line.stripTrailing();

			int eqIdx = line.indexOf('=');
			if (eqIdx < 0 || line.trim().startsWith("#"))
			{
				result.add(line);
				continue;
			}

			String value = line.substring(eqIdx + 1);
			String resolved = resolve(value);
			log.debug("resolved {} to {}", value, resolved);
			if (resolved.equals(value))
			{
				result.add(line);
			}
			else
			{
				result.add(line.substring(0, eqIdx + 1) + resolved);
			}
		}

		return String.join("\n", result);
	}

	public String resolveValue(String input)
	{
		if (Strings.isNullOrEmpty(input) || vars == null || vars.isEmpty())
		{
			return input;
		}

		return resolveValueRecursive(input, new HashSet<>());
	}

	private String resolveValueRecursive(String input, Set<String> visiting)
	{
		if (!input.startsWith("${") || !input.endsWith("}"))
		{
			return input;
		}

		String varName = input.substring(2, input.length() - 1);
		if (varName.isEmpty())
		{
			return input;
		}

		String resolved = vars.get(varName);
		if (resolved == null)
		{
			return input;
		}

		if (visiting.contains(varName))
		{
			log.error("Circular reference detected in vars: {} -> {}", visiting, varName);
			return input;
		}

		visiting.add(varName);
		String deeplyResolved = resolveValueRecursive(resolved, visiting);
		visiting.remove(varName);
		return deeplyResolved;
	}

	private void flattenAndPutAll(Map<String, String> target, TomlTable table, String prefix)
	{
		for (String key : table.keySet())
		{
			String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
			Object value = table.get(key);
			if (value instanceof TomlTable)
			{
				flattenAndPutAll(target, (TomlTable) value, fullKey);
			}
			else
			{
				String resolved = resolveValue(String.valueOf(value));
				target.put(fullKey, resolved);
			}
		}
	}

	private static void putAll(Map<String, String> target, Map<Object, Object> source)
	{
		for (var entry : source.entrySet())
		{
			target.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
		}
	}
}
