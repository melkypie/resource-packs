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

package melky.resourcepacks.harness;

import com.google.common.base.Strings;
import java.awt.Color;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.util.ColorUtil;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestConfigManager
{
	@Getter
	private final Map<String, String> storage = new HashMap<>();

	public ConfigManager create()
	{
		ConfigManager mockConfigManager = mock(ConfigManager.class);

		// setConfiguration(String, String, String)
		doAnswer(invocation -> {
			String groupName = invocation.getArgument(0);
			String key = invocation.getArgument(1);
			String value = invocation.getArgument(2);
			storage.put(compositeKey(groupName, key), value);
			return null;
		}).when(mockConfigManager).setConfiguration(anyString(), anyString(), anyString());

		// setConfiguration(String, String, Object)
		doAnswer(invocation -> {
			String groupName = invocation.getArgument(0);
			String key = invocation.getArgument(1);
			Object value = invocation.getArgument(2);
			storage.put(compositeKey(groupName, key), objectToString(value));
			return null;
		}).when(mockConfigManager).setConfiguration(anyString(), anyString(), any(Object.class));

		// getConfiguration(String, String) -> String
		when(mockConfigManager.getConfiguration(anyString(), anyString()))
			.thenAnswer(invocation -> {
				String groupName = invocation.getArgument(0);
				String key = invocation.getArgument(1);
				return storage.get(compositeKey(groupName, key));
			});

		// getConfiguration(String, String, Type) -> T
		when(mockConfigManager.getConfiguration(anyString(), anyString(), any(Type.class)))
			.thenAnswer(invocation -> {
				String groupName = invocation.getArgument(0);
				String key = invocation.getArgument(1);
				Type type = invocation.getArgument(2);
				String value = storage.get(compositeKey(groupName, key));
				if (Strings.isNullOrEmpty(value))
				{
					return null;
				}
				return stringToObject(value, type);
			});

		// getConfiguration(String, String, Class) -> T (overload)
		when(mockConfigManager.getConfiguration(anyString(), anyString(), any(Class.class)))
			.thenAnswer(invocation -> {
				String groupName = invocation.getArgument(0);
				String key = invocation.getArgument(1);
				Class<?> clazz = invocation.getArgument(2);
				String value = storage.get(compositeKey(groupName, key));
				if (Strings.isNullOrEmpty(value))
				{
					return null;
				}
				return stringToObject(value, clazz);
			});

		// unsetConfiguration(String, String)
		doAnswer(invocation -> {
			String groupName = invocation.getArgument(0);
			String key = invocation.getArgument(1);
			storage.remove(compositeKey(groupName, key));
			return null;
		}).when(mockConfigManager).unsetConfiguration(anyString(), anyString());


		when(mockConfigManager.getConfigurationKeys(anyString()))
			.thenAnswer(invocation -> {
				String prefix = invocation.getArgument(0);
				List<String> result = new ArrayList<>();
				for (String key : storage.keySet())
				{
					if (key.startsWith(prefix))
					{
						result.add(key);
					}
				}
				return result;
			});

		return mockConfigManager;
	}

	private String compositeKey(String groupName, String key)
	{
		return groupName + "." + key;
	}

	private String objectToString(Object object)
	{
		if (object == null)
		{
			return null;
		}
		if (object instanceof Color)
		{
			return String.valueOf(((Color) object).getRGB());
		}
		if (object instanceof Enum)
		{
			return ((Enum<?>) object).name();
		}
		return object.toString();
	}

	private Object stringToObject(String str, Type type)
	{
		if (type == boolean.class || type == Boolean.class)
		{
			return Boolean.parseBoolean(str);
		}
		if (type == int.class || type == Integer.class)
		{
			return Integer.parseInt(str);
		}
		if (type == long.class || type == Long.class)
		{
			return Long.parseLong(str);
		}
		if (type == double.class || type == Double.class)
		{
			return Double.parseDouble(str);
		}
		if (type == Color.class)
		{
			return ColorUtil.fromString(str);
		}
		if (type instanceof Class && ((Class<?>) type).isEnum())
		{
			return Enum.valueOf((Class<? extends Enum>) type, str);
		}
		return str;
	}
}
