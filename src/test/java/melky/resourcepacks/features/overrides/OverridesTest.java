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

package melky.resourcepacks.features.overrides;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.features.overrides.model.WidgetOverride;
import melky.resourcepacks.features.packs.PacksService;
import melky.resourcepacks.harness.OverridesTestHarness;
import melky.resourcepacks.harness.TestHarnessModule;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class OverridesTest extends OverridesTestHarness
{
	@Inject
	WidgetPropertiesOverride widgetPropertiesOverride;

	@Mock
	private Client client;

	@Mock
	private ClientThread clientThread;

	@Mock
	private PacksService packsService;

	@Before
	public void before()
	{
		TestHarnessModule.builder()
			.packsService(packsService)
			.eventBus(new EventBus())
			.client(client)
			.clientThread(clientThread)
			.build()
			.createInjector(this);
	}

	private TomlTable loadSources() throws IOException
	{
		try (InputStream stream = getClass().getResourceAsStream("/overrides/sources/base.toml"))
		{
			TomlParseResult result = Toml.parse(stream);
			result.errors().forEach(error -> log.error("Parse error: {}", error));
			return result;
		}
	}

	private void buildOverrides(TomlTable sources, TomlTable overrides)
	{
		widgetPropertiesOverride.getOverrides().clear();

		for (String key : sources.keySet())
		{
			widgetPropertiesOverride.walkChildren(
				new WidgetOverride().withName(key),
				sources.getTableOrEmpty(key),
				overrides
			);
		}
	}

	@Test
	public void parentColor() throws IOException
	{
		TomlTable sources = loadSources();
		TomlTable overrides = testPackReader.parseTestResource("parent-color.toml");

		buildOverrides(sources, Toml.parse(""));
		var defaultList = widgetPropertiesOverride.getOverrides().get(1).stream()
			.filter(w -> w.getChildId() == 59)
			.collect(Collectors.toList());

		buildOverrides(sources, overrides);
		var list = widgetPropertiesOverride.getOverrides().get(1).stream()
			.filter(w -> w.getChildId() == 59)
			.collect(Collectors.toList());

		assertFalse("override list is empty", list.isEmpty());
		assertFalse("default list is empty", defaultList.isEmpty());

		assertEquals(2, list.size());
		assertEquals(2, defaultList.size());

		assertNotEquals(list.get(0), defaultList.get(0));
		assertNotEquals(list.get(1), defaultList.get(1));

		var newColors = list.stream().map(WidgetOverride::getNewColor).collect(Collectors.toSet());
		assertTrue(newColors.contains(0x643A4B));
		assertTrue(newColors.contains(0x9D6A76));
	}

	@Test
	public void nestedColor() throws IOException
	{
		TomlTable sources = loadSources();
		TomlTable overrides = testPackReader.parseTestResource("nested-color.toml");

		buildOverrides(sources, Toml.parse(""));
		var defaultList = widgetPropertiesOverride.getOverrides().get(2).stream()
			.filter(w -> w.getChildId() == 65)
			.collect(Collectors.toList());

		buildOverrides(sources, overrides);
		var list = widgetPropertiesOverride.getOverrides().get(2).stream()
			.filter(w -> w.getChildId() == 65)
			.collect(Collectors.toList());

		assertFalse("override list is empty", list.isEmpty());
		assertFalse("default list is empty", defaultList.isEmpty());

		assertEquals(2, list.size());
		assertEquals(2, defaultList.size());

		assertNotEquals(list.get(0), defaultList.get(0));
		assertNotEquals(list.get(1), defaultList.get(1));

		var byNewColor = list.stream().collect(Collectors.toMap(WidgetOverride::getNewColor, w -> w));
		assertEquals(2, byNewColor.size());

		assertTrue(byNewColor.containsKey(0x643A4B));
		assertEquals(0x726451, byNewColor.get(0x643A4B).getColor());

		assertTrue(byNewColor.containsKey(0x9D6A76));
		assertEquals(0x2e2b23, byNewColor.get(0x9D6A76).getColor());
	}

	@Test
	public void allDynamicChildren() throws IOException
	{
		TomlTable sources = loadSources();
		TomlTable overrides = testPackReader.parseTestResource("all-children.toml");

		buildOverrides(sources, Toml.parse(""));
		var defaultList = widgetPropertiesOverride.getOverrides().get(123).stream()
			.filter(w -> w.getChildId() == 2)
			.collect(Collectors.toList());

		buildOverrides(sources, overrides);
		var list = widgetPropertiesOverride.getOverrides().get(123).stream()
			.filter(w -> w.getChildId() == 2)
			.collect(Collectors.toList());

		assertFalse("override list is empty", list.isEmpty());
		assertFalse("default list is empty", defaultList.isEmpty());

		assertEquals(1, list.size());
		assertEquals(1, defaultList.size());

		assertNotEquals(list.get(0), defaultList.get(0));

		assertTrue(list.get(0).isAllChildren());
		assertEquals(0, list.get(0).getDynamicChildren().size());
	}

	@Test
	public void nestedChildren() throws IOException
	{
		TomlTable sources = loadSources();
		TomlTable overrides = testPackReader.parseTestResource("nested-children.toml");

		buildOverrides(sources, Toml.parse(""));
		var defaultList = widgetPropertiesOverride.getOverrides().get(8870).stream()
			.filter(w -> w.getChildId() == 9)
			.collect(Collectors.toList());

		buildOverrides(sources, overrides);
		var list = widgetPropertiesOverride.getOverrides().get(8870).stream()
			.filter(w -> w.getChildId() == 9)
			.collect(Collectors.toList());

		assertFalse("override list is empty", list.isEmpty());
		assertFalse("default list is empty", defaultList.isEmpty());

		assertEquals(1, list.size());
		assertEquals(1, defaultList.size());

		assertNotEquals(list.get(0), defaultList.get(0));

		assertEquals(0x000000, list.get(0).getNewColor());
	}
}
