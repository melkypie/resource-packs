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
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN contract, strict liability, or tort
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE use of
    SOFTWARE, EVEN IF ADVISED OF the possibility of such damage.
 */

package melky.resourcepacks.harness;

import java.awt.Color;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.tomlj.TomlTable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public abstract class OverridesTestHarness
{
	protected TestPackReader testPackReader;

    @Before
    public void setUpHarness()
    {
        testPackReader = new TestPackReader();
    }

    protected TomlTable loadTomlFromResources(String path) throws IOException
    {
        return testPackReader.parseTestResource(path);
    }

    protected TestPackBuilder.TestPackBuilderBuilder packBuilder()
    {
        return TestPackBuilder.builder(testPackReader);
    }

    protected void assertColorEquals(int expected, Color actual)
    {
        Color expectedColor = new Color(expected);
        assertEquals("Red channel mismatch", expectedColor.getRed(), actual.getRed());
        assertEquals("Green channel mismatch", expectedColor.getGreen(), actual.getGreen());
        assertEquals("Blue channel mismatch", expectedColor.getBlue(), actual.getBlue());
    }

    protected void assertColorEqualsWithAlpha(int expected, Color actual)
    {
        Color expectedColor = new Color(expected, true);
        assertEquals("Alpha channel mismatch", expectedColor.getAlpha(), actual.getAlpha());
        assertEquals("Red channel mismatch", expectedColor.getRed(), actual.getRed());
        assertEquals("Green channel mismatch", expectedColor.getGreen(), actual.getGreen());
        assertEquals("Blue channel mismatch", expectedColor.getBlue(), actual.getBlue());
    }
}
