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

package melky.resourcepacks.features.packs;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.gson.stream.JsonReader;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ConfigKeys;
import melky.resourcepacks.ConfigKeys.InterfaceStyles;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.ResourcePacksConfig.ResourcePack;
import melky.resourcepacks.SpriteOverride;
import melky.resourcepacks.event.HubPackSelected;
import melky.resourcepacks.event.ResourcePacksChanged;
import melky.resourcepacks.event.UpdateAllOverrides;
import melky.resourcepacks.features.hub.HubClient;
import melky.resourcepacks.features.overrides.Overrides;
import melky.resourcepacks.model.HubManifest;
import melky.resourcepacks.module.PluginLifecycleComponent;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.events.SessionClose;
import net.runelite.client.events.SessionOpen;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
@Slf4j
public class PacksManager implements PluginLifecycleComponent
{
	public static final Path PACKS_BASE_DIR = Path.of(RuneLite.RUNELITE_DIR.getPath(), "resource-packs-repository");
	public static final HttpUrl GITHUB = HttpUrl.parse("https://github.com/melkypie/resource-packs");

	@Inject
	private Client client;

	@Inject
	private ResourcePacksConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private HubClient hubClient;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Overrides overrides;


	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Getter
	private long currentProfile = Long.MIN_VALUE;
	private GameState lastGameState;

	@Override
	public void startUp()
	{
		var packsDir = PACKS_BASE_DIR.toFile();
		if (!packsDir.exists())
		{
			packsDir.mkdirs();
		}

		var noticeFile = Path.of(PACKS_BASE_DIR + "", "DO_NOT_EDIT_CHANGES_WILL_BE_OVERWRITTEN").toFile();
		if (!noticeFile.exists())
		{
			try
			{
				noticeFile.createNewFile();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		touchFolder();


		executor.submit(() ->
		{
			refreshPacks();
			queueUpdateAllOverrides();
		});

		currentProfile = configManager.getProfile().getId();
	}

	@Override
	public void shutDown()
	{

		clientThread.invokeLater(() ->
		{
//			adjustWidgetDimensions(false);
//			removeGameframe();
//			resetWidgetOverrides();
		});
	}

	public void touchFolder()
	{
		var currentFolder = getLocalPath().toFile();
		if (!currentFolder.exists())
		{
			currentFolder.mkdirs();
		}
	}

	public boolean isActiveProfile()
	{
		return currentProfile == configManager.getProfile().getId();
	}

	public void refreshPacks()
	{
		HashMap<String, HubManifest> loadedPacks = new HashMap<>();
		File[] resourcePackDirectories = getLocalPath().toFile().listFiles();
		if (resourcePackDirectories != null)
		{
			for (File resourcePackDirectory : resourcePackDirectories)
			{
				if (resourcePackDirectory.isDirectory())
				{
					try
					{
						HubManifest man = getResourcePackManifest(resourcePackDirectory);
						if (man != null)
						{
							loadedPacks.put(man.getInternalName(), man);
						}
					}
					catch (IOException ignored)
					{
					}
				}
			}
		}

		List<String> installedIDs = getInstalledResourcePacks();
		if (installedIDs.isEmpty() && loadedPacks.isEmpty())
		{
			return;
		}

		Set<HubManifest> resourcePacks = new HashSet<>();
		List<HubManifest> manifestList;
		try
		{
			manifestList = hubClient.downloadManifest();
			Map<String, HubManifest> manifests = manifestList
				.stream().collect(ImmutableMap.toImmutableMap(HubManifest::getInternalName, Function.identity()));

			Set<HubManifest> needsDownload = new HashSet<>();
			Set<File> keep = new HashSet<>();
			assert resourcePackDirectories != null;
			List<File> resourcePackDirectoryList = Arrays.asList(resourcePackDirectories);

			// Check for changed commits and packs that need to be downloaded
			for (String name : installedIDs)
			{
				HubManifest manifest = manifests.get(name);
				if (manifest != null)
				{
					resourcePacks.add(manifest);
					HubManifest loadedResourcePack = loadedPacks.get(manifest.getInternalName());
					File resourcePackDirectory = getLocalPath(manifest.getInternalName()).toFile();
					if (loadedResourcePack == null || !loadedResourcePack.equals(manifest))
					{
						needsDownload.add(manifest);
					}
					else if (loadedResourcePack.getCommit().equals(manifest.getCommit()) && resourcePackDirectoryList.contains(resourcePackDirectory))
					{
						keep.add(resourcePackDirectory);
					}
				}
			}

			// delete old packs
			for (File fi : resourcePackDirectoryList)
			{
				if (!keep.contains(fi))
				{
					MoreFiles.deleteRecursively(fi.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
				}
			}

			// Download packs that need updates/install
			for (HubManifest manifest : needsDownload)
			{
				HttpUrl url = GITHUB.newBuilder()
					.addPathSegment("archive")
					.addPathSegment(manifest.getCommit() + ".zip")
					.build();

				try (Response res = okHttpClient.newCall(new Request.Builder().url(url).build()).execute())
				{
					BufferedInputStream is = new BufferedInputStream(res.body().byteStream());
					ZipInputStream zipInputStream = new ZipInputStream(is);
					ZipEntry entry;
					while ((entry = zipInputStream.getNextEntry()) != null)
					{
						var filePath = getLocalPath(entry.getName().replaceAll("resource-packs-" + manifest.getCommit(), manifest.getInternalName()));
						if (!entry.isDirectory())
						{
							// if the entry is a file, extracts it
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath + ""));
							byte[] bytesIn = new byte[2048];
							int read;
							while ((read = zipInputStream.read(bytesIn)) != -1)
							{
								bos.write(bytesIn, 0, read);
							}
							bos.close();
						}
						else
						{
							// if the entry is a directory, make the directory
							File dir = filePath.toFile();
							dir.mkdir();
						}
					}
					zipInputStream.close();
					is.close();

					File manifestFile = getLocalPath(manifest.getInternalName(), "manifest.js").toFile();
					FileWriter manifestWriter = new FileWriter(manifestFile);
					RuneLiteAPI.GSON.toJson(manifest, manifestWriter);
					manifestWriter.close();
					// In case of total resource folder nuke
					if (config.selectedHubPack().equals(manifest.getInternalName()))
					{
						clientThread.invokeLater(this::updateAllOverrides);
					}
				}
				catch (IOException e)
				{
					resourcePacks.remove(manifest);
					log.error("Unable to download resource pack \"{}\"", manifest.getInternalName(), e);
				}
			}
		}
		catch (IOException e)
		{
			log.error("Unable to download resource packs", e);
			return;
		}
		for (HubManifest ex : resourcePacks)
		{
			loadedPacks.remove(ex.getInternalName());
		}

		// list of installed packs that aren't in the manifest
		Collection<HubManifest> remove = loadedPacks.values();
		for (HubManifest rem : remove)
		{
			log.info("Removing pack \"{}\"", rem.getInternalName());
			Set<String> packs = new HashSet<>(getInstalledResourcePacks());
			if (packs.remove(rem.getInternalName()))
			{
				config.selectedHubPack(Text.toCSV(packs));
			}
		}

		eventBus.post(new ResourcePacksChanged(manifestList));
	}

	private HubManifest getResourcePackManifest(File resourcePackDirectory) throws IOException
	{
		File manifest = Path.of(resourcePackDirectory.getPath(), "manifest.js").toFile();
		JsonReader reader = new JsonReader(new FileReader(manifest));
		HubManifest packManifest = RuneLiteAPI.GSON.fromJson(reader, HubManifest.class);
		reader.close();
		return packManifest;
	}

	public HashMultimap<String, HubManifest> getCurrentManifests() throws IOException
	{
		HashMultimap<String, HubManifest> currentManifests = HashMultimap.create();
		File[] directories = getLocalPath().toFile().listFiles();
		if (directories != null)
		{
			for (File resourcePackDirectory : directories)
			{
				if (!resourcePackDirectory.isDirectory())
				{
					continue;
				}
				HubManifest hubManifest = getResourcePackManifest(resourcePackDirectory);
				currentManifests.put(hubManifest.getInternalName(), hubManifest);
			}
		}
		return currentManifests;
	}

	public void setSelectedHubPack(String internalName)
	{

		if (!internalName.equals("None"))
		{
			config.resourcePack(ResourcePack.HUB);
			config.selectedHubPack(internalName);
		}
		else
		{
			config.selectedHubPack("");
		}

		eventBus.post(HubPackSelected.of(config.selectedHubPack()));
	}

	public List<String> getInstalledResourcePacks()
	{
		String resourcePacksString = config.hubPacks();
		return Text.fromCSV(resourcePacksString == null ? "" : resourcePacksString);
	}

	public void install(String internalName)
	{
		Set<String> packs = new HashSet<>(getInstalledResourcePacks());
		if (packs.add(internalName))
		{
			log.debug("Installing: {}", internalName);
			config.hubPacks(Text.toCSV(packs));
			config.resourcePack(ResourcePack.HUB);

			executor.submit(() ->
			{
				refreshPacks();
				setSelectedHubPack(internalName);
			});
		}
	}

	public void remove(String internalName)
	{
		Set<String> packs = new HashSet<>(getInstalledResourcePacks());
		if (packs.remove(internalName))
		{
			log.debug("Removing: {}", internalName);
			config.hubPacks(Text.toCSV(packs));
			if (config.selectedHubPack() != null && config.selectedHubPack().equals(internalName))
			{
				setSelectedHubPack("None");
			}
			executor.submit(this::refreshPacks);
		}
	}

	// todo: post this as event
	public void updateAllOverrides()
	{
		if (isPackPathEmpty())
		{
			return;
		}

		log.debug("updating all overrides");
		eventBus.post(new UpdateAllOverrides());
	}


	public String getCurrentPackPath()
	{
		String path;
		switch (config.resourcePack())
		{
			case SECOND:
				path = config.resourcePack2Path();
				break;
			case THIRD:
				path = config.resourcePack3Path();
				break;
			case HUB:
				path = getLocalPath(config.selectedHubPack()) + "";
				break;
			case FIRST:
			default:
				path = config.resourcePackPath();
				break;
		}
		return path;
	}

	public boolean isPackPathEmpty()
	{
		switch (config.resourcePack())
		{
			case FIRST:
				return Strings.isNullOrEmpty(config.resourcePackPath());
			case SECOND:
				return Strings.isNullOrEmpty(config.resourcePack2Path());
			case HUB:
				return Strings.isNullOrEmpty(config.selectedHubPack());
			case THIRD:
				return Strings.isNullOrEmpty(config.resourcePack3Path());
		}

		return true;
	}

	public SpritePixels loadResourceSprite(String path)
	{
		try
		{
			BufferedImage image = ImageUtil.loadImageResource(PacksManager.class, path);
			return ImageUtil.getImageSpritePixels(image, client);
		}
		catch (RuntimeException e)
		{
			log.debug("Unable to find resource ({}): ", path);
		}

		return null;
	}

	public SpritePixels getSpritePixels(SpriteOverride spriteOverride, String currentPackPath)
	{
		String folder = spriteOverride.getFolder().name().toLowerCase();
		String name = spriteOverride.name().toLowerCase();
		if (!folder.equals("other"))
		{
			name = name.replaceFirst(folder + "_", "");
		}

		File spriteFile = Path.of(currentPackPath, folder, name + ".png").toFile();
		if (!spriteFile.exists())
		{
//			log.debug("Sprite doesn't exist ({}): ", spriteFile.getPath());
			return null;
		}
		try
		{
			BufferedImage image = ImageIO.read(spriteFile);
			if (config.allowColorPack() && config.colorPack() != null)
			{
				image = dye(image, config.colorPack());
			}
			return ImageUtil.getImageSpritePixels(image, client);
		}
		catch (RuntimeException | IOException ex)
		{
			log.debug("Unable to find image ({}): ", spriteFile.getPath());
		}
		return null;
	}


	private BufferedImage dye(BufferedImage image, Color color)
	{
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage dyed = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dyed.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.setComposite(AlphaComposite.SrcAtop);
		g.setColor(color);
		g.fillRect(0, 0, w, h);
		g.dispose();
		return dyed;
	}

	public Path getLocalPath(String... path)
	{
		var p = Path.of(PACKS_BASE_DIR + "", configManager.getProfile().getId() + "");
		return Path.of(p + "", path);
	}

	@Subscribe(priority = Float.MIN_VALUE)
	public void onConfigChanged(ConfigChanged event)
	{
		if (currentProfile != configManager.getProfile().getId())
		{
			return;
		}

		if (event.getGroup().equals(ResourcePacksConfig.GROUP_NAME))
		{
			switch (event.getKey())
			{
				case "allowSpellsPrayers":
				case "allowColorPack":
				case "colorPackOverlay":
				case "colorPack":
				case "resourcePack":
					clientThread.invokeLater(this::updateAllOverrides);
					break;
			}
		}
		else if (shouldReset(event))
		{
			// lazy reset to try and be after other plugins
			clientThread.invokeLater(() -> clientThread.invokeLater(this::updateAllOverrides));
		}
	}


	@Subscribe
	public void onSessionOpen(SessionOpen event)
	{
		executor.submit(this::refreshPacks);
	}

	@Subscribe
	public void onSessionClose(SessionClose event)
	{
		executor.submit(this::refreshPacks);
	}


	public void sendWarning(String msg)
	{
		String message = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("[")
			.append(ChatColorType.HIGHLIGHT)
			.append("Resource Packs")
			.append(ChatColorType.NORMAL)
			.append("] " + msg)
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(message)
			.build());
	}


	private void queueUpdateAllOverrides()
	{
		clientThread.invokeLater(() ->
		{
			if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState())
			{
				return false;
			}

			updateAllOverrides();
			return true;
		});
	}

	private static boolean shouldReset(ConfigChanged event)
	{
		return event.getGroup().equals(InterfaceStyles.GROUP_NAME) ||
			(event.getGroup().equals(RuneLiteConfig.GROUP_NAME) && ConfigKeys.Plugins.interfacestylesplugin.equals(event.getKey()));
	}


	@Subscribe(priority = Float.MIN_VALUE)
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			if (lastGameState == GameState.STARTING)
			{
				queueUpdateAllOverrides();
			}
		}


		lastGameState = gameStateChanged.getGameState();
	}


	@Subscribe
	public void onPluginMessage(PluginMessage event)
	{
		if (!"resource-packs".equals(event.getNamespace()))
		{
			return;
		}

		if (event.getName().equals("export"))
		{
			eventBus.post(new PluginMessage("resource-packs", "values", overrides.export()));
		}
	}


	@Subscribe(priority = Float.MIN_VALUE)
	public void onProfileChanged(ProfileChanged event)
	{
		currentProfile = configManager.getProfile().getId();
		touchFolder();

		executor.submit(() ->
		{
			refreshPacks();
			clientThread.invokeLater(this::updateAllOverrides);
		});
	}
}
