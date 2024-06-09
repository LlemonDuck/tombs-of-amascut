package com.duckblade.osrs.toa.features.apmeken;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
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
public class ApmekenOverlay extends Overlay implements PluginLifecycleComponent
{

	private static final int ANIMATION_ID_VOLATILE_BABOON = 9756;
	private static final int AREA_SIZE_3X3 = 3;

	private final OverlayManager overlayManager;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final Client client;
	private final TombsOfAmascutConfig config;
	private final Apmeken apmeken;

	@Inject
	public ApmekenOverlay(final OverlayManager overlayManager,
						  final ModelOutlineRenderer modelOutlineRenderer,
						  final Client client, final TombsOfAmascutConfig config,
						  final Apmeken apmeken)
	{
		this.overlayManager = overlayManager;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.client = client;
		this.config = config;
		this.apmeken = apmeken;

		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return apmeken.isEnabled(config, raidState);
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
		if (config.apmekenBaboonOutline())
		{
			renderBaboonOutline();
		}

		if (config.apmekenVolatileBaboonTiles())
		{
			renderVolatileBaboonTiles(graphics2D);
		}

		return null;
	}

	private void renderBaboonOutline()
	{
		for (final NPC npc : apmeken.getBaboons())
		{
			final Color color;

			switch (npc.getId())
			{
				case NpcID.BABOON_BRAWLER:
				case NpcID.BABOON_BRAWLER_11712:
					color = Color.RED;
					break;
				case NpcID.BABOON_MAGE:
				case NpcID.BABOON_MAGE_11714:
					color = Color.BLUE;
					break;
				case NpcID.BABOON_THROWER:
				case NpcID.BABOON_THROWER_11713:
					color = Color.GREEN;
					break;
				case NpcID.BABOON_SHAMAN:
					color = Color.CYAN;
					break;
				case NpcID.CURSED_BABOON:
					color = Color.MAGENTA;
					break;
				case NpcID.VOLATILE_BABOON:
				case NpcID.BABOON_THRALL:
				default:
					continue;
			}

			modelOutlineRenderer.drawOutline(npc, 1, color, 0);
		}
	}

	private void renderVolatileBaboonTiles(final Graphics2D graphics2D)
	{
		for (final NPC npc : apmeken.getVolatileBaboons())
		{
			final Color color = npc.getAnimation() == ANIMATION_ID_VOLATILE_BABOON ? Color.RED : Color.ORANGE;

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

			OverlayUtil.drawOutlineAndFill(graphics2D, color, new Color(color.getRed(), color.getGreen(),
				color.getBlue(), 20), 1, polygon);
		}
	}

}
