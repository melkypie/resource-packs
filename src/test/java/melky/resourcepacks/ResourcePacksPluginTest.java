package melky.resourcepacks;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import org.junit.Test;

public class ResourcePacksPluginTest
{
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


		for (SpriteOverride override: SpriteOverride.values()) {
			File folder = createOrRetrieve(outputFolder + "/" + override.getFolder().toString().toLowerCase());
			File destinationSprite = new File(folder, override.toString().toLowerCase().replaceFirst(override.getFolder().toString().toLowerCase() + "_", "") + ".png");
			File sourceSprite = new File(inputFolder + "/" + override.getSpriteID() + "-0.png");

			if (sourceSprite.exists()) {
				Files.copy(sourceSprite, destinationSprite);
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
}