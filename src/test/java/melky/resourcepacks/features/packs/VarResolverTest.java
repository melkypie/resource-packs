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

import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

public class VarResolverTest
{
	@Test
	public void resolve_basicSubstitution()
	{
		VarResolver tokenizer = new VarResolver(Map.of("blue", "0x0000ff"));
		assertEquals("0x0000ff", tokenizer.resolve("\"${blue}\""));
	}

	@Test
	public void resolve_nestedVariables()
	{
		Map<String, String> vars = new LinkedHashMap<>();
		vars.put("base", "0x123");
		vars.put("mid", "${base}");
		vars.put("top", "${mid}");
		VarResolver tokenizer = new VarResolver(vars);
		assertEquals("0x123", tokenizer.resolve("\"${top}\""));
	}

	@Test
	public void resolve_missingVariableLeftIntact()
	{
		VarResolver tokenizer = new VarResolver(Map.of("blue", "0x0000ff"));
		assertEquals("\"${unknown}\"", tokenizer.resolve("\"${unknown}\""));
	}

	@Test
	public void resolve_circularReference()
	{
		Map<String, String> vars = new LinkedHashMap<>();
		vars.put("a", "${b}");
		vars.put("b", "${a}");
		VarResolver tokenizer = new VarResolver(vars);
		assertEquals("${b}", tokenizer.resolveValue("${a}"));
	}

	@Test
	public void resolveContent_singleVar_substitutesValue()
	{
		Map<String, String> vars = Map.of("border_color", "0x474745");

		String overrides = "[bank.menu.border.inner]\ncolor=\"${border_color}\"";

		VarResolver resolver = new VarResolver(vars);
		String resolved = resolver.resolveContent(overrides);

		TomlParseResult parsed = Toml.parse(resolved);
		assertTrue("should have no parse errors", parsed.errors().isEmpty());

		Object color = parsed.getTableOrEmpty("bank").getTableOrEmpty("menu")
			.getTableOrEmpty("border").getTableOrEmpty("inner").get("color");
		assertEquals("color should be resolved to Long", 0x474745L, color);
	}

	@Test
	public void resolveContent_multipleVars_allSubstituted()
	{
		Map<String, String> vars = Map.of(
			"border_color", "0x474745",
			"hidden", "0"
		);

		String overrides = "[bank.menu.border.inner]\ncolor=\"${border_color}\"\nopacity=\"${hidden}\"";

		VarResolver resolver = new VarResolver(vars);
		String resolved = resolver.resolveContent(overrides);

		TomlParseResult parsed = Toml.parse(resolved);
		assertTrue("should have no parse errors", parsed.errors().isEmpty());

		var inner = parsed.getTableOrEmpty("bank").getTableOrEmpty("menu")
			.getTableOrEmpty("border").getTableOrEmpty("inner");
		assertEquals("color should be Long", 0x474745L, inner.get("color"));
		assertEquals("opacity should be Long", 0L, inner.get("opacity"));
	}

	@Test
	public void resolveContent_recursiveVar_substitutesDeeply()
	{
		Map<String, String> vars = new LinkedHashMap<>();
		vars.put("base_color", "0x123456");
		vars.put("theme_color", "${base_color}");

		String overrides = "[section]\ncolor=\"${theme_color}\"";

		VarResolver resolver = new VarResolver(vars);
		String resolved = resolver.resolveContent(overrides);

		TomlParseResult parsed = Toml.parse(resolved);
		assertTrue("should have no parse errors", parsed.errors().isEmpty());

		Object color = parsed.getTableOrEmpty("section").get("color");
		assertEquals("recursive var should resolve to base value", 0x123456L, color);
	}

	@Test
	public void resolveContent_undefinedVar_leftAsUnquotedString()
	{
		Map<String, String> vars = Map.of("known", "42");

		String overrides = "[section]\ncolor=\"${unknown}\"";

		VarResolver resolver = new VarResolver(vars);
		String resolved = resolver.resolveContent(overrides);

		TomlParseResult parsed = Toml.parse(resolved);
		assertTrue("should have no parse errors", parsed.errors().isEmpty());

		Object color = parsed.getTableOrEmpty("section").get("color");
		assertEquals("undefined var should remain as string", "${unknown}", color);
	}

	@Test
	public void resolveContent_circularReference_stopsAndLeavesValue()
	{
		Map<String, String> vars = new LinkedHashMap<>();
		vars.put("a", "${b}");
		vars.put("b", "${a}");

		String overrides = "[section]\ncolor=\"${a}\"";

		VarResolver resolver = new VarResolver(vars);
		String resolved = resolver.resolveContent(overrides);

		assertTrue("resolved should contain unquoted ${b}", resolved.contains("color=${b}"));
		assertFalse("resolved should not have ${a}", resolved.contains("${a}"));
	}

	@Test
	public void resolveContent_idempotent_sameResultTwice()
	{
		Map<String, String> vars = Map.of("my_color", "0xFF");

		String overrides = "[section]\ncolor=\"${my_color}\"";

		VarResolver resolver = new VarResolver(vars);

		String first = resolver.resolveContent(overrides);
		String second = resolver.resolveContent(overrides);

		assertEquals("resolveContent should be idempotent", first, second);

		TomlParseResult parsed = Toml.parse(second);
		assertEquals(0xFFL, parsed.getTableOrEmpty("section").get("color"));
	}

	@Test
	public void resolveContent_noVarsInContent_unchanged()
	{
		Map<String, String> vars = Map.of("unused", "0");

		String overrides = "[section]\ncolor=0x111111\nopacity=255";

		VarResolver resolver = new VarResolver(vars);
		String resolved = resolver.resolveContent(overrides);

		TomlParseResult parsed = Toml.parse(resolved);
		assertTrue("should have no parse errors", parsed.errors().isEmpty());

		var section = parsed.getTableOrEmpty("section");
		assertEquals(0x111111L, section.get("color"));
		assertEquals(255L, section.get("opacity"));
	}

	@Test
	public void resolveContent_userOverridesDefaultVar()
	{
		Map<String, String> defaultVars = Map.of("theme", "0xAAAAAA");
		Map<String, String> userVars = new LinkedHashMap<>();
		userVars.put("theme", "0xBBBBBB");

		VarResolver defaultResolver = new VarResolver(defaultVars);
		VarResolver userResolver = new VarResolver(userVars);

		String overrides = "[section]\ncolor=\"${theme}\"";

		String defaultResolved = defaultResolver.resolveContent(overrides);
		String userResolved = userResolver.resolveContent(overrides);

		TomlParseResult defaultParsed = Toml.parse(defaultResolved);
		TomlParseResult userParsed = Toml.parse(userResolved);

		assertEquals("default should use 0xAAAAAA", 0xAAAAAAL,
			defaultParsed.getTableOrEmpty("section").get("color"));
		assertEquals("user should override to 0xBBBBBB", 0xBBBBBBL,
			userParsed.getTableOrEmpty("section").get("color"));
	}
}
