/*
 * Copyright (c) 2026, stadtehrsoftware
 * All rights reserved.
 * BSD 2-Clause License; see LICENSE.
 */
package com.duckblade.osrs.toa.features.reminders;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

@Singleton
public class ToaInventoryOverlay extends WidgetItemOverlay implements PluginLifecycleComponent
{
	private static final Color READY_COLOR = new Color(0, 255, 80);
	private static final Color LOW_PRAYER_COLOR = new Color(255, 40, 40);
	private static final long FLASH_PERIOD_MS = 1_000L;

	private final OverlayManager overlayManager;
	private final Client client;
	private final TombsOfAmascutConfig config;

	@Inject
	public ToaInventoryOverlay(Client client, TombsOfAmascutConfig config, OverlayManager overlayManager)
	{
		this.client = client;
		this.overlayManager = overlayManager;
		this.config = config;
		showOnInventory();
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return (raidState.isInLobby() || raidState.isInRaid()) && (config.flashKeris() || config.flashAmbrosia());
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
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		final Color color = highlightColor(
			itemId,
			client.getBoostedSkillLevel(Skill.PRAYER),
			config.flashKeris(),
			config.flashAmbrosia());
		if (color == null)
		{
			return;
		}

		final Rectangle bounds = widgetItem.getCanvasBounds();
		final Composite oldComposite = graphics.getComposite();
		final Color oldColor = graphics.getColor();
		final float alpha = flashAlpha(System.currentTimeMillis());

		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		graphics.setColor(color);
		graphics.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha + .35f)));
		graphics.drawRoundRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 8, 8);

		graphics.setComposite(oldComposite);
		graphics.setColor(oldColor);
	}

	static Color highlightColor(int itemId, int prayerLevel, boolean flashKeris, boolean flashAmbrosia)
	{
		if (flashKeris && itemId == ItemID.KERIS_PARTISAN_SUN)
		{
			return prayerLevel > 50 ? READY_COLOR : LOW_PRAYER_COLOR;
		}

		if (flashAmbrosia && (itemId == ItemID.TOA_SUPPLY_PANICHEAL_1 || itemId == ItemID.TOA_SUPPLY_PANICHEAL_2))
		{
			return READY_COLOR;
		}

		return null;
	}

	private static float flashAlpha(long now)
	{
		final double phase = (now % FLASH_PERIOD_MS) / (double) FLASH_PERIOD_MS;
		return (float) (0.14 + 0.24 * ((Math.sin(phase * Math.PI * 2) + 1) / 2));
	}
}
