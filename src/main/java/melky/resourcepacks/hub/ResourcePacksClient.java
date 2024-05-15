package melky.resourcepacks.hub;

import com.google.common.reflect.TypeToken;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import static melky.resourcepacks.ResourcePacksPlugin.BRANCH;
import static melky.resourcepacks.ResourcePacksPlugin.RAW_GITHUB;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class ResourcePacksClient
{

	private final OkHttpClient cachingClient;

	@Inject
	public ResourcePacksClient(OkHttpClient cachingClient)
	{
		this.cachingClient = cachingClient;
	}

	public List<ResourcePackManifest> downloadManifest() throws IOException
	{
		HttpUrl manifest = RAW_GITHUB
			.newBuilder()
			.addPathSegment(BRANCH)
			.addPathSegment("manifest.js")
			.build();

		try (Response res = cachingClient.newCall(new Request.Builder().url(manifest).build()).execute())
		{
			if (res.code() != 200)
			{
				throw new IOException("Non-OK response code: " + res.code());
			}

			String data = res.body().string();

			return RuneLiteAPI.GSON.fromJson(data,
				new TypeToken<List<ResourcePackManifest>>()
				{
				}.getType());
		}
	}

	public BufferedImage downloadIcon(ResourcePackManifest plugin) throws IOException
	{
		if (!plugin.isHasIcon())
		{
			return null;
		}

		HttpUrl url = RAW_GITHUB
			.newBuilder()
			.addPathSegment(plugin.getCommit())
			.addPathSegment("icon.png")
			.build();

		try (Response res = cachingClient.newCall(new Request.Builder().url(url).build()).execute())
		{
			byte[] bytes = res.body().bytes();
			// We don't stream so the lock doesn't block the edt trying to load something at the same time
			synchronized (ImageIO.class)
			{
				return ImageIO.read(new ByteArrayInputStream(bytes));
			}
		}
	}
}
