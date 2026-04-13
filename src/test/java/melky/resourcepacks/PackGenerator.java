/*
 * Copyright (c) 2025, Ron Young <https://github.com/raiyni>
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

package melky.resourcepacks;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.model.SpriteOverride;
import org.junit.AfterClass;
import org.junit.Test;

@Slf4j
public class PackGenerator
{
	private static final int EOF = -1;


	private static final List<String> errors = new ArrayList<>();
	private static final List<String> warnings = new ArrayList<>();

	@AfterClass
	public static void afterClass() throws IOException
	{
		String basePath = System.getProperty("user.dir");

		log.info("logging {}", errors);

		PrintWriter errorFile = new PrintWriter(basePath + '/' + "errors.txt", StandardCharsets.UTF_8);
		errorFile.write(String.join("\n", errors));
		errorFile.close();

		log.info("logging {}", warnings);

		PrintWriter warningsFile = new PrintWriter(basePath + '/' + "warnings.txt", StandardCharsets.UTF_8);
		warningsFile.write(String.join("\n", warnings));
		warningsFile.close();
	}

	/**
	 * Allow you to build your own sample-vanilla pack (with some drawbacks)
	 * Sprite 990 is custom made (is janky when using a dumper), login screen is also not being moved and also runelite's tag tab sprites are not moved. These have to be manually added
	 * <p>
	 * spriteFolder needs sprites to be dumped using correct offsets/correct max dimensions otherwise most of the sprites will be not positioned correctly (can be downloaded from sprite-exporter repo)
	 * packFolder where to output the sample-vanilla pack to
	 *
	 * @throws IOException
	 */
	@Test
	public void generatePack() throws IOException
	{
		String spriteFolder = System.getProperty("spriteFolder");
		String packFolder = System.getProperty("packFolder");
		if (Strings.isNullOrEmpty(spriteFolder) || Strings.isNullOrEmpty(packFolder))
		{
			throw new RuntimeException("inputFolder and outputFolder need to be defined");
		}

		for (SpriteOverride override : SpriteOverride.values())
		{
			// TODO:
			// Grab the tag tab images from rl repo and make an exception for login screen
			if (override.getSpriteID() < 0)
			{
				continue;
			}
			File folder = createOrRetrieve(packFolder + "/" + override.getFolder().toString().toLowerCase());
			File destinationSprite = new File(folder, override.toString().toLowerCase().replaceFirst(override.getFolder().toString().toLowerCase() + "_", "") + ".png");
			File sourceSprite;
			if (override.getFrameID() != -1)
			{
				sourceSprite = new File(spriteFolder + "/" + override.getSpriteID() + "-" + override.getFrameID() + ".png");
			}
			else
			{
				sourceSprite = new File(spriteFolder + "/" + override.getSpriteID() + "-0.png");
			}


			if (sourceSprite.exists() && !(destinationSprite.exists() && fileContentEquals(sourceSprite, destinationSprite)))
			{
				Files.copy(sourceSprite, destinationSprite);
				log.info("Updated sprite " + override.name() + " (" + override.getSpriteID() + ")");
			}
		}
		File outputFolderFile = new File(packFolder);
		loopDirectory(outputFolderFile.listFiles(), outputFolderFile.getName(), spriteFolder, true);
	}


	private File createOrRetrieve(final String target) throws IOException
	{
		File outputDir = new File(target);
		if (!outputDir.exists())
		{
			outputDir.mkdirs();
		}

		return outputDir;
	}

	private boolean fileContentEquals(File file1, File file2) throws IOException
	{
		try (FileInputStream finput1 = new FileInputStream(file1);
			 FileInputStream finput2 = new FileInputStream(file2))
		{
			try (BufferedInputStream binput1 = new BufferedInputStream(finput1);
				 BufferedInputStream binput2 = new BufferedInputStream(finput2))
			{
				int b1 = binput1.read();
				while (EOF != b1)
				{
					int b2 = binput2.read();
					if (b1 != b2)
					{
						return false;
					}
					b1 = binput1.read();
				}
				return binput2.read() == EOF;
			}
		}
	}

	private List<String> loopDirectory(File[] directory, String dirName, String spriteDir, boolean delete) throws IOException
	{
		List<String> errorMessages = new ArrayList<String>();
		if (dirName.equals(".git"))
		{
			return errorMessages;
		}

		if (directory == null)
		{
			warn("\u001B[33mDirectory " + dirName + " is not needed as it is empty\u001B[0m");
			return errorMessages;
		}
		for (File file : directory)
		{
			if (file.isDirectory())
			{
				loopDirectory(file.listFiles(), file.getName(), spriteDir, delete);
			}
			else
			{
				if (file.getName().contains(".png") && !file.getName().equals("icon.png"))
				{
					try
					{
						SpriteOverride override;
						if (dirName.equalsIgnoreCase("other"))
						{
							override = SpriteOverride.valueOf(file.getName().replace(".png", "").toUpperCase());
						}
						else
						{
							override = SpriteOverride.valueOf(dirName.toUpperCase() + "_" + file.getName().replace(".png", "").toUpperCase());
						}

						if (override.getSpriteID() < 0)
						{
							continue;
						}

						File originalSprite;
						if (override.getFrameID() != -1)
						{
							originalSprite = new File(spriteDir + "/" + override.getSpriteID() + "-" + override.getFrameID() + ".png");
						}
						else
						{
							originalSprite = new File(spriteDir + "/" + override.getSpriteID() + "-0.png");
						}
						if (fileContentEquals(file, originalSprite) && !delete)
						{
							warn("\u001B[33mFile " + file.getName() + " (" + override.getSpriteID() + ") in folder " + dirName + " is the same as the vanilla sprite\u001B[0m");
						}
					}
					catch (IllegalArgumentException e)
					{
						if (delete)
						{
							file.delete();
						}
						else
						{
							warn("\u001B[33mFile " + file.getName() + " in folder " + dirName + " is redundant\u001B[0m");
						}
					}
				}
				else if (!file.getName().contains(".properties") && !file.getName().contains(".toml") && !file.getName().contains(".md") && !file.getName().equals("icon.png"))
				{
					errorMessages.add("\u001B[31mFound a file " + file.getName() + " in folder " + dirName + " that is not a sprite, icon, properties or markdown file\u001B[0m");
				}
			}
		}
		return errorMessages;
	}


	private void warn(String msg)
	{
		log.warn(msg);
		warnings.add(msg);
	}
}
