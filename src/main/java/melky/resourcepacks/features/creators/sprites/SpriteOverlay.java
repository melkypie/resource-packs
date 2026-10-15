/*
 * Copyright (c) 2025, Ron Young <https://github.com/raiyni>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

package melky.resourcepacks.features.creators.sprites;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.features.state.LocalConfig;
import melky.resourcepacks.module.PluginLifecycleComponent;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
@Slf4j
public class SpriteOverlay extends Overlay implements PluginLifecycleComponent
{
	private EventBus eventBus;
	private Client client;
	private SpriteTracker spriteTracker;

	@Inject
	public SpriteOverlay(
		Client client, EventBus eventBus, SpriteTracker spriteTracker
	)
	{
		this.client = client;
		this.eventBus = eventBus;
		this.spriteTracker = spriteTracker;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_HIGHEST);
		drawAfterInterface(InterfaceID.TOPLEVEL_DISPLAY);
	}

	@Override
	public boolean isEnabled(ResourcePacksConfig config, LocalConfig localConfig)
	{
		return config.developerTools() && localConfig.getState(this.getClass());
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (spriteTracker.isEmpty())
		{
			return null;
		}

		for (var w : spriteTracker.getWidgets())
		{
			if (w.isHidden() || w.isHidden())
			{
				continue;
			}

			renderWidget(g, w);
		}

		return null;
	}


	private void renderWidget(Graphics2D g, Widget w)
	{
		var highlight = spriteTracker.getHighlight(w.getSpriteId());
		if (highlight == null || !highlight.isVisible())
		{
			return;
		}

		var p = w.getBounds();
		if (p == null)
		{
			return;
		}

		g.setColor(highlight.getColor());
		var parent = w.getParent();
		if (parent != null)
		{
			var b = parent.getBounds();
			if (b != null && b.intersects(p))
			{
				g.draw(w.getBounds());
				renderName(g, w, highlight);
			}
		}
		else
		{
			g.draw(w.getBounds());
			renderName(g, w, highlight);
		}
	}

	private void renderName(Graphics2D g, Widget w, SpriteTracker.HighlightOptions opt)
	{
		var p = w.getCanvasLocation();
		var o = opt.getOverride();

		g.drawString(o.toString(), p.getX(), p.getY());
	}

	@Override
	public void startUp()
	{

	}

	@Override
	public void shutDown()
	{

	}
}
