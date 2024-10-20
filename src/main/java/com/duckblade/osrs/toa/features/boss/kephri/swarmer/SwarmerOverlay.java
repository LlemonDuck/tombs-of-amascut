package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.stream.Collectors;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;

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
		return swarmer.isEnabled(config, raidState)
			&& config.swarmerOverlay()
			&& raidState.getCurrentRoom() == RaidRoom.KEPHRI;
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
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		swarmer.getAliveSwarms()
			.values()
			.stream()
			.collect(Collectors.groupingBy(swarm -> swarm.getNpc().getWorldLocation()))
			.values()
			.forEach(tileSwarms ->
			{
				int stackOffset = 0;
				for (SwarmNpc swarm : tileSwarms)
				{
					this.draw(graphics, swarm, stackOffset);
					stackOffset += graphics.getFontMetrics().getHeight();
				}
			});
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

		Font font = new Font(config.swarmerFontType().toString(), config.useBoldFont() ? Font.BOLD : Font.PLAIN, config.swarmerFontSize());
		FontRenderContext frc = graphics.getFontRenderContext();
		TextLayout tl = new TextLayout(text, font, frc);
		Shape outline = tl.getOutline(null);
		graphics.translate(x, y);
		graphics.setStroke(new BasicStroke(3));
		graphics.setColor(Color.BLACK);
		graphics.draw(outline);
		graphics.setColor(config.swarmerFontColor());
		graphics.fill(outline);
		graphics.translate(-x, -y);
	}
}
