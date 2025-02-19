package melky.resourcepacks;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;


@Slf4j
public class ResourcePacksPluginTest
{
	private static final int EOF = -1;

	private static StringBuilder builder = new StringBuilder();

	@AfterClass
	public static void afterClass() throws IOException
	{
		String basePath = System.getProperty("user.dir");

		log.info("logging {}", builder.toString());

		PrintWriter logFile = new PrintWriter(basePath + '/' + "out.txt", "UTF-8");
		logFile.write(builder.toString());
		logFile.close();
	}

	@Rule
	public TestWatcher watcher = new TestWatcher()
	{

		@Override
		protected void failed(Throwable e, Description description)
		{
			if (e != null)
			{
				builder.append(e.getMessage());
				builder.append("\n");
			}
		}

		@Override
		protected void succeeded(Description description)
		{
		}
	};

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ResourcePacksPlugin.class);
		RuneLite.main(args);
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
	public void moveImages() throws IOException
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

	/**
	 * Allow you to check whether a pack does not contain files that should not be in there or sprites that are the same as vanilla
	 * <p>
	 * spriteFolder needs to be dumped using correct offsets/correct max dimensions for the vanilla comparison to work (can be downloaded from sprite-exporter repo)
	 * packFolder is the path to the pack you want to be testing against
	 *
	 * @throws IOException
	 */
	@Test
	public void checkUnneededFiles() throws IOException
	{
		String spriteFolder = System.getProperty("spriteFolder");
		String packFolder = System.getProperty("packFolder");
		if (Strings.isNullOrEmpty(spriteFolder) || Strings.isNullOrEmpty(packFolder))
		{
			throw new RuntimeException("spriteFolder and packFolder need to be defined");
		}

		File packFolderFile = new File(packFolder);
		List<String> errorMessages = loopDirectory(packFolderFile.listFiles(), packFolderFile.getName(), spriteFolder, false);
		for (String error : errorMessages)
		{
			log.error(error);
		}

		if (!errorMessages.isEmpty())
		{
			throw new IllegalArgumentException(String.join("\n", errorMessages));
		}
	}

	@Test
	public void checkPackProperties() throws IOException
	{
		String packFolder = System.getProperty("packFolder");
		if (Strings.isNullOrEmpty(packFolder))
		{
			throw new RuntimeException("spriteFolder and packFolder need to be defined");
		}

		File propertiesFile = new File(packFolder, "/pack.properties");
		if (!propertiesFile.exists())
		{
			throw new IllegalArgumentException("Pack does not contain a pack.properties file");
		}

		Properties properties = new Properties();
		properties.load(new FileInputStream(propertiesFile));

		List<String> errorMessages = new ArrayList<>();

		if (Strings.isNullOrEmpty(properties.getProperty("displayName")))
		{
			errorMessages.add("pack.properties does not contain a displayName property");
		}

		if (Strings.isNullOrEmpty(properties.getProperty("author")))
		{
			errorMessages.add("pack.properties does not contain a author property");
		}

		if (!properties.containsKey("tags"))
		{
			errorMessages.add("pack.properties does not contain a tags property");
		}

		for (String error : errorMessages)
		{
			log.error(error);
		}

		if (!errorMessages.isEmpty())
		{
			throw new IllegalArgumentException(String.join("\n", errorMessages));
		}
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
			errorMessages.add("\u001B[31mDirectory " + dirName + " is not needed as it is empty\u001B[0m");
			return errorMessages;
		}
		for (File file : directory)
		{
			if (file.isDirectory())
			{
				errorMessages.addAll(loopDirectory(file.listFiles(), file.getName(), spriteDir, delete));
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
							errorMessages.add("\u001B[31mFile " + file.getName() + " (" + override.getSpriteID() + ") in folder " + dirName + " is the same as the vanilla sprite\u001B[0m");
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
							errorMessages.add("\u001B[31mFile " + file.getName() + " in folder " + dirName + " is redundant\u001B[0m");
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
}