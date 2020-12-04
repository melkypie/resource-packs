package melky.resourcepacks;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import org.junit.Test;

@Slf4j
public class ResourcePacksPluginTest
{
	private static final int EOF = -1;

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ResourcePacksPlugin.class);
		RuneLite.main(args);
	}

	@Test
	public void moveImages() throws IOException
	{
		String inputFolder = System.getProperty("resources.inputFolder");
		String outputFolder = System.getProperty("resources.outputFolder");
		if (Strings.isNullOrEmpty(inputFolder) || Strings.isNullOrEmpty(outputFolder)) {
			throw new RuntimeException("inputFolder and outputFolder need to be defined");
		}

		for (SpriteOverride override : SpriteOverride.values()) {
			// TODO:
			// Grab the tag tab images from rl repo and make an exception for login screen
			// Figure out why 990 is so janky
			if (override.getSpriteID() < 0 || override.getSpriteID() == 990) {
				continue;
			}
			File folder = createOrRetrieve(outputFolder + "/" + override.getFolder().toString().toLowerCase());
			File destinationSprite = new File(folder, override.toString().toLowerCase().replaceFirst(override.getFolder().toString().toLowerCase() + "_", "") + ".png");
			File sourceSprite = new File(inputFolder + "/" + override.getSpriteID() + "-0.png");

			if (sourceSprite.exists() && !(destinationSprite.exists() && fileContentEquals(sourceSprite, destinationSprite))) {
				Files.copy(sourceSprite, destinationSprite);
				log.info("Updated sprite " + override.name() + " (" + override.getSpriteID() + ")");
			}
		}
	}

	private File createOrRetrieve(final String target) throws IOException
	{
		File outputDir = new File(target);
		if (!outputDir.exists()) {
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
}