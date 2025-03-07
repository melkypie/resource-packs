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

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

@Slf4j
public class InterfacesTest
{
	Overrides overrides;

	@Before
	public void beforeEach()
	{
		overrides = new Overrides();
	}

	@Test
	public void buildOverrides() throws IOException
	{
		testOverride("settings");
//		overrides.buildOverrides("");
//		var bank1 = overrides.get(274).get(0);
//
//		overrides.buildOverrides("[bank.separator]\ncolor=0xff0000");
//		var bank2 = overrides.get(274).get(0);
//
//		assertFalse(Objects.equals(bank1, bank2));
	}

	public void testOverride(String key)
	{
		overrides.clear();

		try (var stream = Overrides.class.getResourceAsStream("/overrides/overrides.toml"))
		{
			assert stream != null;

			TomlParseResult toml = Toml.parse(stream);
			toml.errors().forEach(error -> log.error(error.toString()));

			TomlParseResult pack = Toml.parse("");
			pack.errors().forEach(error -> log.error(error.toString()));

			var table = toml.getTableOrEmpty(key);
			overrides.walkChildren(new WidgetOverride().withName(key), table, pack);
		}
		catch (IOException e)
		{
			log.error("error loading overrides", e);
		}
	}
}
