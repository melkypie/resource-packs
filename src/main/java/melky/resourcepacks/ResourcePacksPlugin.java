package melky.resourcepacks;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.SpriteID;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Resource packs"
)
public class ResourcePacksPlugin extends Plugin
{
	private static final int ADJUSTED_TAB_WIDTH = 33;
	private static final int ORIGINAL_TAB_WIDTH = 38;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ResourcePacksConfig config;

	@Provides
	ResourcePacksConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ResourcePacksConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		if (checkIfResourcePackPathIsNotEmpty())
		{
			clientThread.invoke(this::updateAllOverrides);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invoke(() ->
		{
			adjustWidgetDimensions(ORIGINAL_TAB_WIDTH);
			removeGameframe();
		});
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		adjustWidgetDimensions(ADJUSTED_TAB_WIDTH);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("resourcepacks"))
		{
			if (event.getKey().equals("resourcePack"))
			{
				clientThread.invoke(this::removeGameframe);
				if (checkIfResourcePackPathIsNotEmpty())
				{
					clientThread.invoke(this::updateAllOverrides);
				}
			}
		}
	}

	private void restoreSprites()
	{
		client.getWidgetSpriteCache().reset();

		for (SpriteOverride spriteOverride : SpriteOverride.values())
		{
			client.getSpriteOverrides().remove(spriteOverride.getSpriteID());
		}
	}

	private String getSpritePath(SpriteOverride spriteOverride)
	{
		String folder = spriteOverride.getFolder().name().toLowerCase();
		String name = spriteOverride.name().toLowerCase();
		if (!folder.equals("other"))
		{
			name = name.replaceFirst(folder + "_", "");
		}

		switch (config.resourcePack())
		{
			case FIRST:
				return config.resourcePackPath() + "/" + folder + "/" + name + ".png";
			case SECOND:
				return config.resourcePack2Path() + "/" + folder + "/" + name + ".png";
			case THIRD:
				return config.resourcePack3Path() + "/" + folder + "/" + name + ".png";
		}
		return config.resourcePackPath() + "/" + folder + "/" + name + ".png";
	}

	private SpritePixels getFileSpritePixels(String file)
	{
		try
		{
			log.debug("Loading: {}", file);
			BufferedImage image = ImageIO.read(new File(file));
			return ImageUtil.getImageSpritePixels(image, client);
		}
		catch (RuntimeException | IOException ex)
		{
			log.debug("Unable to find image: ", ex);
		}

		return null;
	}

	private void overrideSprites()
	{
		for (SpriteOverride spriteOverride : SpriteOverride.values())
		{
			String file = getSpritePath(spriteOverride);
			SpritePixels spritePixels = getFileSpritePixels(file);
			if (spritePixels == null)
			{
				continue;
			}

			if (spriteOverride.getSpriteID() == SpriteID.COMPASS_TEXTURE)
			{
				client.setCompass(spritePixels);
			}
			else
			{
				client.getSpriteOverrides().put(spriteOverride.getSpriteID(), spritePixels);
			}
		}
	}

	private void removeGameframe()
	{
		restoreSprites();

		BufferedImage compassImage = spriteManager.getSprite(SpriteID.COMPASS_TEXTURE, 0);

		if (compassImage != null)
		{
			SpritePixels compass = ImageUtil.getImageSpritePixels(compassImage, client);
			client.setCompass(compass);
		}
	}

	private void updateAllOverrides()
	{
		removeGameframe();
		overrideSprites();
		adjustWidgetDimensions(ORIGINAL_TAB_WIDTH);
		adjustWidgetDimensions(ADJUSTED_TAB_WIDTH);
	}

	// Adjust certain tabs to match other tabs because of Jagex's inconsistent tab sizes
	private void adjustWidgetDimensions(int width)
	{
		for (WidgetResize widgetResize : WidgetResize.values())
		{
			Widget widget = client.getWidget(widgetResize.getWidgetInfo());

			if (widget != null)
			{
				widget.setOriginalWidth(width);
				widget.revalidate();
			}
		}
	}

	private boolean checkIfResourcePackPathIsNotEmpty()
	{
		switch (config.resourcePack())
		{
			case FIRST:
				if (config.resourcePackPath().equals(""))
				{
					return false;
				}
				break;
			case SECOND:
				if (config.resourcePack2Path().equals(""))
				{
					return false;
				}
				break;
			case THIRD:
				if (config.resourcePack3Path().equals(""))
				{
					return false;
				}
				break;
		}
		return true;
	}
}
