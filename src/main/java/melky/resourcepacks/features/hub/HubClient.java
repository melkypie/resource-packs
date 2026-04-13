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

package melky.resourcepacks.features.hub;

import com.google.common.reflect.TypeToken;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.model.HubManifest;
import melky.resourcepacks.module.PluginLifecycleComponent;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@Singleton
public class HubClient implements PluginLifecycleComponent
{
	public static final HttpUrl RAW_GITHUB = HttpUrl.parse("https://raw.githubusercontent.com/melkypie/resource-packs");
	public static final HttpUrl API_GITHUB = HttpUrl.parse("https://api.github.com/repos/melkypie/resource-packs");
	public static final String BRANCH = "github-actions";

	private final OkHttpClient okHttpClient;

	@Inject
	public HubClient(OkHttpClient okHttpClient)
	{
		this.okHttpClient = okHttpClient;
	}

	public List<HubManifest> downloadManifest() throws IOException
	{
		HttpUrl manifest = RAW_GITHUB
			.newBuilder()
			.addPathSegment(BRANCH)
			.addPathSegment("manifest.js")
			.build();

		try (Response res = okHttpClient.newCall(new Request.Builder().url(manifest).build()).execute())
		{
			if (res.code() != 200)
			{
				throw new IOException("Non-OK response code: " + res.code());
			}

			String data = res.body().string();

			return RuneLiteAPI.GSON.fromJson(data,
				new TypeToken<List<HubManifest>>()
				{
				}.getType());
		}
	}

	public BufferedImage downloadIcon(HubManifest plugin) throws IOException
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

		try (Response res = okHttpClient.newCall(new Request.Builder().url(url).build()).execute())
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
