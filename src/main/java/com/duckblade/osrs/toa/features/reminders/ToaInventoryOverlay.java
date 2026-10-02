package com.duckblade.osrs.toa.features.reminders;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.annotations.VisibleForTesting;
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
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

@Singleton
public class ToaInventoryOverlay
	extends WidgetItemOverlay
	implements PluginLifecycleComponent
{
	@VisibleForTesting
	static final Color READY_COLOR = new Color(0, 255, 80);
	@VisibleForTesting
	static final Color NOT_READY_COLOR = new Color(255, 40, 40);

	private static final int MIN_PRAYER_POINTS = 50;
	private static final int MIN_SPEC_ENERGY = 750; // varp is in tenths of a percent

	private static final long FLASH_PERIOD_MS = 1_000L;

	private final Client client;
	private final TombsOfAmascutConfig config;

	@Inject
	public ToaInventoryOverlay(Client client, TombsOfAmascutConfig config)
	{
		this.client = client;
		this.config = config;
		showOnInventory();
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.isInRaid() &&
			(config.flashKeris() || config.flashAmbrosia());
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		final Color color = highlightColor(itemId);
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

	@VisibleForTesting
	Color highlightColor(int itemId)
	{
		switch (itemId)
		{
			case ItemID.TOA_SUPPLY_PANICHEAL_1:
			case ItemID.TOA_SUPPLY_PANICHEAL_2:
				if (!config.flashAmbrosia())
				{
					return null;
				}
				return READY_COLOR;

			case ItemID.KERIS_PARTISAN_SUN:
				if (!config.flashKeris())
				{
					return null;
				}

				boolean lowPrayer = client.getBoostedSkillLevel(Skill.PRAYER) < MIN_PRAYER_POINTS;
				boolean lowSpec = client.getVarpValue(VarPlayerID.SA_ENERGY) < MIN_SPEC_ENERGY;
				return (lowPrayer || lowSpec) ? NOT_READY_COLOR : READY_COLOR;
		}

		return null;
	}

	private static float flashAlpha(long now)
	{
		final double phase = (now % FLASH_PERIOD_MS) / (double) FLASH_PERIOD_MS;
		return (float) (0.14 + 0.24 * ((Math.sin(phase * Math.PI * 2) + 1) / 2));
	}
}
