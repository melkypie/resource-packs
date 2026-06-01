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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.features.packs.PackReader;
import melky.resourcepacks.features.packs.VarResolver;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

@Slf4j
public class TestPackReader extends PackReader
{
	private static final String TEST_RESOURCE_BASE = "/overrides/tests";

	public TomlTable parseTestResource(String path) throws IOException
	{
		try (InputStream stream = getClass().getResourceAsStream(TEST_RESOURCE_BASE + "/" + path))
		{
			if (stream == null)
			{
				throw new IOException("Test resource not found: " + TEST_RESOURCE_BASE + "/" + path);
			}
			TomlParseResult result = Toml.parse(stream);
			result.errors().forEach(error -> log.error("Parse error: {}", error));
			return result;
		}
	}

	public TomlTable parseTestResourceWithTemplates(String path, TomlTable vars) throws IOException
	{
		String raw = loadTestResourceContent(path);
		VarResolver tokenizer = new VarResolver(Map.of(), null, vars);
		String resolved = tokenizer.resolveContent(raw);
		TomlParseResult result = Toml.parse(resolved);
		result.errors().forEach(error -> log.error("Parse error: {}", error));
		return result;
	}

	public String loadTestResourceContent(String path) throws IOException
	{
		try (InputStream stream = getClass().getResourceAsStream(TEST_RESOURCE_BASE + "/" + path))
		{
			if (stream == null)
			{
				throw new IOException("Test resource not found: " + TEST_RESOURCE_BASE + "/" + path);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
