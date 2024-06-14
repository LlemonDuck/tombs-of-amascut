package com.duckblade.osrs.toa.features.apmeken;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.HighlightMode;
import com.duckblade.osrs.toa.util.OverlayUtil;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Singleton
public class ApmekenBaboonIndicatorOverlay extends Overlay implements PluginLifecycleComponent
{

	private static final int AREA_SIZE_3X3 = 3;

	private final OverlayManager overlayManager;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final Client client;
	private final TombsOfAmascutConfig config;
	private final ApmekenBaboonIndicator apmekenBaboonIndicator;

	@Inject
	public ApmekenBaboonIndicatorOverlay(
		final OverlayManager overlayManager,
		final ModelOutlineRenderer modelOutlineRenderer,
		final Client client,
		final TombsOfAmascutConfig config,
		final ApmekenBaboonIndicator apmekenBaboonIndicator
	)
	{
		this.overlayManager = overlayManager;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.client = client;
		this.config = config;
		this.apmekenBaboonIndicator = apmekenBaboonIndicator;

		setPriority(Overlay.PRIORITY_HIGH);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return (config.apmekenBaboonOutline() != HighlightMode.OFF || config.apmekenVolatileBaboonTiles()) &&
			apmekenBaboonIndicator.isEnabled(config, raidState);
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
	public Dimension render(final Graphics2D graphics2D)
	{
		if (config.apmekenBaboonOutline() != HighlightMode.OFF)
		{
			renderBaboonOutline(graphics2D);
		}

		if (config.apmekenVolatileBaboonTiles())
		{
			renderVolatileBaboonTiles(graphics2D);
		}

		return null;
	}

	private void renderBaboonOutline(final Graphics2D graphics2D)
	{
		for (final NPC npc : apmekenBaboonIndicator.getBaboons())
		{
			final Color color;

			switch (npc.getId())
			{
				case NpcID.BABOON_BRAWLER:
				case NpcID.BABOON_BRAWLER_11712:
					color = config.apmekenBaboonColorMelee();
					break;
				case NpcID.BABOON_MAGE:
				case NpcID.BABOON_MAGE_11714:
					color = config.apmekenBaboonColorMage();
					break;
				case NpcID.BABOON_THROWER:
				case NpcID.BABOON_THROWER_11713:
					color = config.apmekenBaboonColorRange();
					break;
				case NpcID.BABOON_SHAMAN:
					color = config.apmekenBaboonColorShaman();
					break;
				case NpcID.CURSED_BABOON:
					color = config.apmekenBaboonColorCursed();
					break;
				case NpcID.VOLATILE_BABOON:
				case NpcID.BABOON_THRALL:
				default:
					continue;
			}

			final Color noAlpha = new Color((0xFF << 24) | color.getRGB(), true);

			Polygon polygon = null;
			switch (config.apmekenBaboonOutline())
			{
				case OUTLINE:
					modelOutlineRenderer.drawOutline(npc, config.apmekenBaboonOutlineWidth(), color, 0);
					break;
				case TILE:
					polygon = npc.getCanvasTilePoly();
					break;
				case TRUE_TILE:
					LocalPoint lp = LocalPoint.fromWorld(npc.getWorldView(), npc.getWorldLocation()); // centered on sw tile
					if (lp != null)
					{
						polygon = Perspective.getCanvasTileAreaPoly(client, lp, 1);
					}
				default:
					break;
			}

			if (polygon != null)
			{
				OverlayUtil.drawOutlineAndFill(graphics2D, noAlpha, color, config.apmekenBaboonOutlineWidth(), polygon);
			}
		}
	}

	private void renderVolatileBaboonTiles(final Graphics2D graphics2D)
	{
		for (final NPC npc : apmekenBaboonIndicator.getVolatileBaboons())
		{
			final LocalPoint localPoint = npc.getLocalLocation();

			if (localPoint == null)
			{
				continue;
			}

			final Polygon polygon = Perspective.getCanvasTileAreaPoly(client, localPoint, AREA_SIZE_3X3);

			if (polygon == null)
			{
				continue;
			}

			final Color color = config.apmekenBaboonColorVolatile();
			final Color noAlpha = new Color((0xFF << 24) | color.getRGB(), true);

			OverlayUtil.drawOutlineAndFill(graphics2D, noAlpha, color, config.apmekenBaboonOutlineWidth(), polygon);
		}
	}

}
