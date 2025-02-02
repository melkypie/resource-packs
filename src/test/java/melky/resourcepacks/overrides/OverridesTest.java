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

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
public class OverridesTest
{

	@Test
	public void parentColor() throws IOException
	{
		Overrides defaultValues = new Overrides("/overrides/sources/base.toml").buildOverrides("");
		Overrides overrides = new Overrides("/overrides/sources/base.toml");

		String text = Resources.toString(Resources.getResource("overrides/tests/parent-color.toml"), StandardCharsets.UTF_8);
		overrides.buildOverrides(text);

		var list = overrides.get(1);
		var defaultList = defaultValues.get(1);

		assertFalse("override list is empty", list.isEmpty());
		assertFalse("default list is empty", defaultList.isEmpty());


		var fillers = list.stream().filter(w -> w.getChildId() == 59).collect(Collectors.toList());
		var defaultFillers = defaultList.stream().filter(w -> w.getChildId() == 59).collect(Collectors.toList());

		assertEquals(2, fillers.size());
		assertEquals(2, defaultFillers.size());

		assertNotEquals(fillers.get(0), defaultFillers.get(0));
		assertNotEquals(fillers.get(1), defaultFillers.get(1));

		assertEquals(0x9D6A76, fillers.get(0).getNewColor());
		assertEquals(0x643A4B, fillers.get(1).getNewColor());
	}

	@Test
	public void nestedColor() throws IOException
	{
		Overrides defaultValues = new Overrides("/overrides/sources/base.toml").buildOverrides("");
		Overrides overrides = new Overrides("/overrides/sources/base.toml");

		String text = Resources.toString(Resources.getResource("overrides/tests/nested-color.toml"), StandardCharsets.UTF_8);
		overrides.buildOverrides(text);

		var list = overrides.get(2);
		var defaultList = defaultValues.get(2);

		assertFalse("override list is empty", list.isEmpty());
		assertFalse("default list is empty", defaultList.isEmpty());


		var fillers = list.stream().filter(w -> w.getChildId() == 65).collect(Collectors.toList());
		var defaultFillers = defaultList.stream().filter(w -> w.getChildId() == 65).collect(Collectors.toList());

		assertEquals(2, fillers.size());
		assertEquals(2, defaultFillers.size());

		assertNotEquals(fillers.get(0), defaultFillers.get(0));
		assertNotEquals(fillers.get(1), defaultFillers.get(1));

		log.info("{}", fillers.get(0));

		assertEquals(0x643A4B, fillers.get(1).getNewColor());
		assertEquals(0x726451, fillers.get(1).getColor());

		assertEquals(0x9D6A76, fillers.get(0).getNewColor());
		assertEquals(0x2e2b23, fillers.get(0).getColor());
	}

	@Test
	public void allDynamicChildren() throws IOException
	{
		Overrides defaultValues = new Overrides("/overrides/sources/base.toml").buildOverrides("");
		Overrides overrides = new Overrides("/overrides/sources/base.toml");

		String text = Resources.toString(Resources.getResource("overrides/tests/all-children.toml"), StandardCharsets.UTF_8);
		overrides.buildOverrides(text);

		var list = overrides.get(123);
		var defaultList = defaultValues.get(123);

		assertFalse("override list is empty", list.isEmpty());
		assertFalse("default list is empty", defaultList.isEmpty());


		var fillers = list.stream().filter(w -> w.getChildId() == 2).collect(Collectors.toList());
		var defaultFillers = defaultList.stream().filter(w -> w.getChildId() == 2).collect(Collectors.toList());

		assertEquals(1, fillers.size());
		assertEquals(1, defaultFillers.size());

		assertNotEquals(fillers.get(0), defaultFillers.get(0));

		log.info("{}", fillers.get(0));

		assertEquals(true, fillers.get(0).isAllChildren());
		assertEquals(0, fillers.get(0).getDynamicChildren().size());
	}
}
