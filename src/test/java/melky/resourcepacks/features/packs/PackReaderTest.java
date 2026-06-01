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

import java.io.IOException;
import java.util.Map;
import melky.resourcepacks.harness.OverridesTestHarness;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

public class PackReaderTest extends OverridesTestHarness
{
	@Test
	public void resolveTemplates_basicSubstitution() throws IOException
	{
		TomlParseResult vars = testPackReader.loadVars(
			testPackReader.loadTestResourceContent("template-vars.toml")
		);

		TomlTable overrides = testPackReader.parseTestResourceWithTemplates(
			"template-overrides.toml", vars
		);

		assertNotNull(overrides);

		TomlTable inner = overrides.getTable("bank.menu.border.inner");
		assertNotNull(inner);
		assertEquals(0x474745L, inner.getLong("color").longValue());
		assertEquals(0L, inner.getLong("opacity").longValue());
	}

	@Test
	public void resolveTemplates_nestedVariables() throws IOException
	{
		TomlParseResult vars = testPackReader.loadVars(
			testPackReader.loadTestResourceContent("template-vars.toml")
		);

		TomlTable overrides = testPackReader.parseTestResourceWithTemplates(
			"template-overrides.toml", vars
		);

		assertNotNull(overrides);

		TomlTable fillersInner = overrides.getTable("bank.menu.border.inner.fillers");
		assertNotNull(fillersInner);
		assertEquals(0x726451L, fillersInner.getLong("color").longValue());
	}

	@Test
	public void resolveTemplates_missingVariable() throws IOException
	{
		PackReader reader = new PackReader();
		TomlParseResult vars = reader.loadVars("blue=0x0000ff");

		String input = "color=\"${unknown}\"\nopacity=0";
		String resolved = new VarResolver(
			Map.of(), null, vars
		).resolveContent(input);

		assertEquals("color=\"${unknown}\"\nopacity=0", resolved);
	}

	@Test
	public void resolveTemplates_builderIntegration() throws IOException
	{
		var pack = packBuilder()
			.varsFromFile("template-vars.toml")
			.sourcesFromFile("../sources/base.toml")
			.overridesWithTemplates("template-overrides.toml")
			.build()
			.toPack();

		assertNotNull(pack.getOverrides());
		assertTrue(pack.getVars().containsKey("border_inner"));

		TomlTable inner = pack.getOverrides().getTable("bank.menu.border.inner");
		assertNotNull(inner);
		assertEquals(0x474745L, inner.getLong("color").longValue());
		assertEquals(0L, inner.getLong("opacity").longValue());
	}
}
