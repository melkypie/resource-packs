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
		clientThread.invokeLater(this::updateAllOverrides);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invoke(() ->
		{
			adjustWidgetDimensions(false);
			removeGameframe();
		});
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		adjustWidgetDimensions(true);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("resourcepacks") && event.getKey().equals("resourcePack"))
		{
			clientThread.invoke(this::updateAllOverrides);
		}
		else if (event.getGroup().equals("banktags") && event.getKey().equals("useTabs"))
		{
			clientThread.invoke(this::updateAllOverrides);
		}
	}

	private void restoreSprites()
	{
		client.getWidgetSpriteCache().reset();

		for (SpriteOverride spriteOverride : SpriteOverride.values())
		{
			client.getSpriteOverrides().remove(spriteOverride.getSpriteID());
		}
		for (TabSprites tabSprite : TabSprites.values())
		{
			BufferedImage image = ImageUtil.getResourceStreamFromClass(getClass(), tabSprite.getFileName());
			SpritePixels sp = ImageUtil.getImageSpritePixels(image, client);
			client.getSpriteOverrides().put(tabSprite.getSpriteId(), sp);
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
				if (spriteOverride.getSpriteID() < -200)
				{
					client.getSpriteOverrides().remove(spriteOverride.getSpriteID());
				}
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
		if (!checkIfResourcePackPathIsNotEmpty())
		{
			return;
		}
		removeGameframe();
		overrideSprites();
		adjustWidgetDimensions(false);
		adjustWidgetDimensions(true);
	}

	private void adjustWidgetDimensions(boolean modify)
	{
		for (WidgetResize widgetResize : WidgetResize.values())
		{
			Widget widget = client.getWidget(widgetResize.getGroup(), widgetResize.getChild());

			if (widget != null)
			{
				if (widgetResize.getOriginalX() != null)
				{
					if (modify)
					{
						widget.setOriginalX(widgetResize.getModifiedX());
					}
					else
					{
						widget.setOriginalX(widgetResize.getOriginalX());
					}
				}

				if (widgetResize.getOriginalY() != null)
				{
					if (modify)
					{
						widget.setOriginalY(widgetResize.getModifiedY());
					}
					else
					{
						widget.setOriginalY(widgetResize.getOriginalY());
					}
				}

				if (widgetResize.getOriginalWidth() != null)
				{
					if (modify)
					{
						widget.setOriginalWidth(widgetResize.getModifiedWidth());
					}
					else
					{
						widget.setOriginalWidth(widgetResize.getOriginalWidth());
					}
				}

				if (widgetResize.getOriginalHeight() != null)
				{
					if (modify)
					{
						widget.setOriginalWidth(widgetResize.getModifiedHeight());
					}
					else
					{
						widget.setOriginalWidth(widgetResize.getOriginalHeight());
					}
				}
			}
			if (widget != null)
			{
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
