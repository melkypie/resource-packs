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

package melky.resourcepacks.features.overrides;

import com.google.gson.Gson;
import java.awt.Color;
import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.features.packs.PacksService;
import melky.resourcepacks.harness.OverridesTestHarness;
import melky.resourcepacks.harness.TestConfigManager;
import melky.resourcepacks.harness.TestHarnessModule;
import melky.resourcepacks.model.runelite.ChatColorKey;
import static melky.resourcepacks.model.runelite.ConfigKeys.RuneLiteConfig.CHAT_COLOR_CONFIG;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ChatColorsTest extends OverridesTestHarness
{
	@Mock(lenient = true)
	private PacksService packsService;

	@Mock(lenient = true)
	private ResourcePacksConfig config;

	@Inject
	private ChatColorOverride chatColorOverride;

	private TestConfigManager testConfigManager;
	private ConfigManager configManager;

	@Before
	public void setUp()
	{
		when(config.allowChatColors()).thenReturn(true);
		when(packsService.isPackPathEmpty()).thenReturn(false);

		testConfigManager = new TestConfigManager();
		configManager = testConfigManager.create();

		TestHarnessModule.builder()
			.configManager(configManager)
			.packsService(packsService)
			.config(config)
			.eventBus(new EventBus())
			.gson(new Gson())
			.build()
			.createInjector(this);
	}

	@Test
	public void parseColors_basicColors() throws IOException
	{
		var pack = packBuilder()
			.chatColorsFromFile("chat_colors.toml")
			.build()
			.toPack();

		chatColorOverride.parseColors(pack.getChatColors());

		Map<String, Color> parsedColors = chatColorOverride.getParsedColors();

		Color publicChat = parsedColors.get(ChatColorKey.PUBLIC_CHAT.opaqueConfig());
		Color publicChatHighlight = parsedColors.get(ChatColorKey.PUBLIC_CHAT_HIGHLIGHT.opaqueConfig());

		assertNotNull(publicChat);
		assertNotNull(publicChatHighlight);

		assertColorEquals(0x123, publicChat);
		assertColorEquals(0xabc, publicChatHighlight);
	}

	@Test
	public void parseColors_vars() throws IOException
	{
		var pack = packBuilder()
			.varsFromFile("vars.toml")
			.chatColorsWithTemplates("chat_colors.toml")
			.build()
			.toPack();

		chatColorOverride.parseColors(pack.getChatColors());

		Map<String, Color> parsedColors = chatColorOverride.getParsedColors();

		Color publicChat = parsedColors.get(ChatColorKey.PUBLIC_CHAT.transparentConfig());
		Color publicChatHighlight = parsedColors.get(ChatColorKey.PUBLIC_CHAT_HIGHLIGHT.transparentConfig());

		assertNotNull(publicChat);
		assertNotNull(publicChatHighlight);

		assertColorEquals(0x0, publicChat);
		assertColorEquals(0xff0000, publicChatHighlight);
	}

	@Test
	public void save_storesExistingColors()
	{
		Color existingColor = new Color(0xFF0000);
		configManager.setConfiguration(CHAT_COLOR_CONFIG, ChatColorKey.PUBLIC_CHAT.opaqueConfig(), existingColor);

		chatColorOverride.save();

		Map<String, Color> savedColors = chatColorOverride.getSavedColors();
		assertEquals(existingColor, savedColors.get(ChatColorKey.PUBLIC_CHAT.opaqueConfig()));
	}

	@Test
	public void apply_setsParsedColors()
	{
		Color parsedColor = new Color(0x123);
		chatColorOverride.getParsedColors().put(ChatColorKey.PUBLIC_CHAT.opaqueConfig(), parsedColor);

		chatColorOverride.apply();

		Color storedColor = configManager.getConfiguration(CHAT_COLOR_CONFIG, ChatColorKey.PUBLIC_CHAT.opaqueConfig(), Color.class);
		assertEquals(parsedColor, storedColor);
	}

	@Test
	public void apply_unsetsThenSets()
	{
		Color existingColor = new Color(0xFF0000);
		configManager.setConfiguration(CHAT_COLOR_CONFIG, ChatColorKey.PUBLIC_CHAT.opaqueConfig(), existingColor);

		Color parsedColor = new Color(0x123);
		chatColorOverride.getParsedColors().put(ChatColorKey.PUBLIC_CHAT.opaqueConfig(), parsedColor);

		chatColorOverride.apply();

		Color storedColor = configManager.getConfiguration(CHAT_COLOR_CONFIG, ChatColorKey.PUBLIC_CHAT.opaqueConfig(), Color.class);
		assertEquals(parsedColor, storedColor);
	}

	@Test
	public void reset_restoresSavedColors()
	{
		Color savedColor = new Color(0xFF0000);
		chatColorOverride.getSavedColors().put(ChatColorKey.PUBLIC_CHAT.opaqueConfig(), savedColor);

		chatColorOverride.reset();

		Color restoredColor = configManager.getConfiguration(CHAT_COLOR_CONFIG, ChatColorKey.PUBLIC_CHAT.opaqueConfig(), Color.class);
		assertEquals(savedColor, restoredColor);
		assertTrue(chatColorOverride.getSavedColors().isEmpty());
	}

	@Test
	public void reset_clearsSavedColors()
	{
		Color savedColor = new Color(0xFF0000);
		chatColorOverride.getSavedColors().put(ChatColorKey.PUBLIC_CHAT.opaqueConfig(), savedColor);

		chatColorOverride.reset();

		assertTrue(chatColorOverride.getSavedColors().isEmpty());
	}
}
