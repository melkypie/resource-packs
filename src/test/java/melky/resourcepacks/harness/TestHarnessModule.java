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
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package melky.resourcepacks.harness;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provider;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import lombok.Builder;
import lombok.Getter;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.features.packs.PacksService;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;

/**
 * Guice module for test harness that binds mock/simple instances.
 * Uses providers to avoid member injection on pre-built instances.
 */
@Builder
@Getter
public class TestHarnessModule extends AbstractModule
{
	private final ConfigManager configManager;
	private final PacksService packsService;
	private final ResourcePacksConfig config;
	private final EventBus eventBus;
	private final Gson gson;
	private final Client client;
	private final ClientThread clientThread;

	@Override
	protected void configure()
	{
		// Use providers to bind instances without triggering member injection
		// This is important for mocks which may have @Inject fields that Guice
		// would try to resolve even when using toInstance()
		if (configManager != null)
		{
			bind(ConfigManager.class).toProvider(() -> configManager);
		}
		if (packsService != null)
		{
			bind(PacksService.class).toProvider(() -> packsService);
		}
		if (config != null)
		{
			bind(ResourcePacksConfig.class).toProvider(() -> config);
		}
		if (eventBus != null)
		{
			bind(EventBus.class).toProvider(() -> eventBus);
		}
		if (gson != null)
		{
			bind(Gson.class).toProvider(() -> gson);
		}
		if (client != null)
		{
			bind(Client.class).toProvider(() -> client);
		}
		if (clientThread != null)
		{
			bind(ClientThread.class).toProvider(() -> clientThread);
		}
	}

	public void createInjector(Object target)
	{
		Guice.createInjector(this, BoundFieldModule.of(target))
			.injectMembers(target);
	}
}
