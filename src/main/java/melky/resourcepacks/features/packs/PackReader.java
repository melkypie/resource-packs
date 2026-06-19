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
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.event.PackParsed;
import melky.resourcepacks.event.ReloadPack;
import melky.resourcepacks.model.Pack;
import melky.resourcepacks.module.PluginLifecycleComponent;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

@Slf4j
@Singleton
public class PackReader implements PluginLifecycleComponent
{
	@Inject
	private PacksService packsService;

	@Inject
	private EventBus eventBus;

	@Inject
	private PackVars packVars;

	@Setter
	@Getter
	private String sourceFile = "/overrides/overrides.toml";

	@Getter
	@Setter
	private String defaultVarsFile = "/overrides/vars.toml";

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return !packsService.isPackPathEmpty();
	}

	@Override
	public void startUp()
	{
		build();
	}

	@Subscribe
	public void onReloadPack(ReloadPack event)
	{
		build();
	}

	public PackReader build()
	{
		if (packsService.isPackPathEmpty())
		{
			return this;
		}

		Path packDir = Path.of(packsService.getCurrentPackPath());
		if (!Files.exists(packDir))
		{
			log.warn("pack directory does not exist: {}", packDir);
			return this;
		}

		Map<Object, Object> idVars = packVars.getVars();
		TomlParseResult defaultVars = loadVars(loadResourceAsString(getDefaultVarsFile()));
		TomlParseResult userVars = loadVars(packsService.getPath("vars.toml"));

		VarResolver defaultResolver = new VarResolver(idVars, defaultVars, null);
		VarResolver fullResolver = new VarResolver(idVars, defaultVars, userVars);
		String sourcesContent = loadResourceAsString(getSourceFile());

		var pack = Pack.builder()
			.vars(userVars != null ? ImmutableMap.copyOf(userVars.toMap()) : Map.of())
			.sources(parseString(defaultResolver.resolveContent(sourcesContent)))
			.overrides(parseString(fullResolver.resolveContent(getOverrides())))
			.chatColors(parseString(fullResolver.resolveContent(getChatColors())))
			.build();

		eventBus.post(new PackParsed(pack));

		return this;
	}

	@VisibleForTesting
	public String loadResourceAsString(String path)
	{
		try (InputStream stream = PackReader.class.getResourceAsStream(path))
		{
			if (stream == null)
			{
				log.error("Resource not found: {}", path);
				return "";
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			log.error("Error reading resource: {}", path, e);
		}
		return "";
	}

	protected TomlTable parseString(String data)
	{
		TomlParseResult toml = Toml.parse(data);
		toml.errors().forEach(error -> log.error("parse error: {}", error.toString()));

		return toml;
	}

	protected TomlParseResult loadVars(Path path)
	{
		if (!Files.exists(path))
		{
			return Toml.parse("");
		}

		try
		{
			return loadVars(Files.readString(path));
		}
		catch (IOException e)
		{
			log.debug("error loading vars", e);
		}

		return Toml.parse("");
	}

	protected String getChatColors()
	{
		try
		{
			var overridesFile = packsService.getPath("chat_colors.toml");
			if (Files.exists(overridesFile))
			{
				return Files.readString(overridesFile);
			}

			return "";
		}
		catch (IOException e)
		{
			log.debug("error loading chat color overrides", e);
		}

		return null;
	}

	protected String getOverrides()
	{
		try
		{
			var overridesFile = packsService.getPath("overrides.toml");
			if (Files.exists(overridesFile))
			{
				return Files.readString(overridesFile);
			}

			log.debug("overrides.toml not found, trying color.properties");
			var backwardsMap = new Properties();
			var properties = new Properties();

			File propertiesFile = packsService.getPath("color.properties").toFile();
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
			return String.join("\n", lines);
		}
		catch (IOException e)
		{
			log.error("error loading color overrides", e);
		}

		return null;
	}

	@VisibleForTesting
	public TomlParseResult loadVars(String data)
	{
		if (Strings.isNullOrEmpty(data))
		{
			return Toml.parse("");
		}

		TomlParseResult toml = Toml.parse(data);
		toml.errors().forEach(error -> log.error("Vars parse error: {}", error.toString()));

		return toml;
	}
}
