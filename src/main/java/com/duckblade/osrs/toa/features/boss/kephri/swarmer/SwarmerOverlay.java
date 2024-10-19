package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;

import java.awt.*;
import java.util.List;
import java.util.stream.IntStream;

@Singleton
public class SwarmerOverlay extends Overlay implements PluginLifecycleComponent
{
	private final TombsOfAmascutConfig config;
	private final OverlayManager overlayManager;
	private final Swarmer swarmer;

	public ArrayListMultimap<WorldPoint, SwarmNpc> renderedSwarms;

	@Inject
	protected SwarmerOverlay(
			final TombsOfAmascutConfig config,
			final OverlayManager overlayManager,
			final Swarmer swarmer
	)
	{
		this.config = config;
		this.overlayManager = overlayManager;
		this.swarmer = swarmer;
		this.renderedSwarms = ArrayListMultimap.create();

		setPriority(Overlay.PRIORITY_HIGH);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return swarmer.isEnabled(config, raidState);
	}

	@Override
	public void startUp()
	{
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(this);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		this.renderedSwarms.clear();
		List<SwarmNpc> aliveSwarms = this.swarmer.getAliveSwarms();

		for (SwarmNpc swarm : aliveSwarms)
		{
			WorldPoint worldPoint = swarm.getNpc().getWorldLocation();
			this.renderedSwarms.put(worldPoint, swarm);
		}

		if (!this.renderedSwarms.isEmpty())
		{
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			this.renderedSwarms.asMap().forEach(
					(worldPoint, npcs) ->
					{
						int offset = 0;
						for (SwarmNpc swarm : npcs)
						{
							this.draw(graphics, swarm, offset);
							offset += graphics.getFontMetrics().getHeight();
						}
					});
		}
		return null;
	}

	private void draw(Graphics2D graphics, SwarmNpc swarmer, int offset)
	{
		String text = String.valueOf(swarmer.getWaveSpawned());

		Point canvasTextLocation = swarmer.getNpc().getCanvasTextLocation(graphics, text, 0);
		if (canvasTextLocation == null)
		{
			return;
		}
		int x = canvasTextLocation.getX();
		int y = canvasTextLocation.getY() + offset;

		graphics.setFont(new Font(config.swarmerFontType().toString(), config.useBoldFont() ? Font.BOLD : Font.PLAIN, config.swarmerFontSize()));

		if (config.swarmerOverlay())
		{
			graphics.setColor(Color.BLACK); // outline color
			IntStream.range(-1, 2).forEachOrdered(ex ->
			{
				IntStream.range(-1, 2).forEachOrdered(ey ->
				{
					if (ex != 0 && ey != 0)
					{
						graphics.drawString(text, x + ex, y + ey);
					}
				});
			});
		}

		graphics.setColor(config.swarmerFontColor());

		graphics.drawString(text, x, y);
	}
}
