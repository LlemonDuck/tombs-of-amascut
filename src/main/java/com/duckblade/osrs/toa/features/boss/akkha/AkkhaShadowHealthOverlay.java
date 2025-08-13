package com.duckblade.osrs.toa.features.boss.akkha;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.OverlayUtil;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class AkkhaShadowHealthOverlay extends Overlay implements PluginLifecycleComponent
{
	private final TombsOfAmascutConfig config;
	private final OverlayManager overlayManager;
	private final AkkhaShadowHealth akkhaShadowHealth;

	@Inject
	protected AkkhaShadowHealthOverlay(
		final TombsOfAmascutConfig config,
		final OverlayManager overlayManager,
		final AkkhaShadowHealth akkhaShadowHealth
	)
	{
		this.config = config;
		this.overlayManager = overlayManager;
		this.akkhaShadowHealth = akkhaShadowHealth;

		setPriority(Overlay.PRIORITY_HIGH);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return akkhaShadowHealth.isEnabled(config, raidState) && config.akkhaShadowHpOverlay();
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
		akkhaShadowHealth.getAkkhasShadows().forEach((npc, hp) ->
		{
			if (hp > 0)
			{
				final String text = Integer.toString(hp);

				final Point point = npc.getCanvasTextLocation(graphics2D, text, 0);

				if (point != null)
				{
					OverlayUtil.renderTextLocation(graphics2D, point, text, Color.WHITE, config.akkhaFontSize(),
						config.akkhaFontStyle().getFont(), true);
				}
			}
		});

		return null;
	}
}