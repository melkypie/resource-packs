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
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Getter;
import melky.resourcepacks.model.Pack;
import org.tomlj.TomlTable;

@Builder
@Getter
public class TestPackBuilder
{
	private final TestPackReader reader;
	@Builder.Default
	private TomlTable vars = null;
	private TomlTable sources;
	private TomlTable overrides;
	private TomlTable chatColors;

	public static TestPackBuilderBuilder builder(TestPackReader reader)
	{
		return new TestPackBuilderBuilder().reader(reader);
	}

	public static class TestPackBuilderBuilder
	{
		public TestPackBuilderBuilder varsFromFile(String varsFile) throws IOException
		{
			vars(reader.loadVars(reader.loadTestResourceContent(varsFile)));
			return this;
		}

		public TestPackBuilderBuilder sourcesFromFile(String sourcesFile) throws IOException
		{
			sources(reader.parseTestResource(sourcesFile));
			return this;
		}

		public TestPackBuilderBuilder overridesFromFile(String overridesFile) throws IOException
		{
			overrides(reader.parseTestResource(overridesFile));
			return this;
		}

		public TestPackBuilderBuilder chatColorsFromFile(String chatColorsFile) throws IOException
		{
			chatColors(reader.parseTestResource(chatColorsFile));
			return this;
		}

		public TestPackBuilderBuilder overridesWithTemplates(String overridesFile) throws IOException
		{
			overrides(reader.parseTestResourceWithTemplates(overridesFile, vars$value));
			return this;
		}

		public TestPackBuilderBuilder chatColorsWithTemplates(String chatColorsFile) throws IOException
		{
			chatColors(reader.parseTestResourceWithTemplates(chatColorsFile, vars$value));
			return this;
		}
	}

	public Pack toPack()
	{
		return Pack.builder()
			.vars(vars != null ? ImmutableMap.copyOf(vars.toMap()) : Map.of())
			.sources(sources)
			.overrides(overrides)
			.chatColors(chatColors)
			.build();
	}
}
