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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
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

	@Setter
	@Getter
	private String sourceFile = "/overrides/overrides.toml";

	@Getter
	@Setter
	private String defaultVarsFile = "/overrides/vars.toml";

	@Inject
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

		String sourcesContent = loadResourceAsString(getSourceFile());

		var pack = Pack.builder()
			.sources(parseString(sourcesContent))
			.overrides(parseString(getOverrides()))
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
			try (var is = new FileInputStream(propertiesFile);
			     var is2 = PacksManager.class.getResourceAsStream("/overrides/backwards-map.properties"))
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
}
