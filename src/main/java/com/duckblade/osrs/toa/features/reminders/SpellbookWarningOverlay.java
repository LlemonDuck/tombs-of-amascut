package com.duckblade.osrs.toa.features.reminders;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.InventoryUtil;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class SpellbookWarningOverlay extends Overlay implements PluginLifecycleComponent
{

	private static final Set<Integer> DAGGER_IDS = ImmutableSet.of(
		ItemID.DRAGON_DAGGER,
		ItemID.DRAGON_DAGGER_P,
		ItemID.DRAGON_DAGGER_P_,
		ItemID.DRAGON_DAGGER_P__,
		ItemID.ABYSSAL_DAGGER,
		ItemID.ABYSSAL_DAGGER_P,
		ItemID.ABYSSAL_DAGGER_P_,
		ItemID.ABYSSAL_DAGGER_P__,
		ItemID.BR_DRAGON_DAGGER,
		ItemID.BH_ABYSSAL_DAGGER_IMBUE,
		ItemID.BH_ABYSSAL_DAGGER_P_IMBUE,
		ItemID.BH_ABYSSAL_DAGGER_P__IMBUE,
		ItemID.BH_ABYSSAL_DAGGER_P___IMBUE,
		ItemID.BH_DRAGON_DAGGER_CORRUPTED,
		ItemID.BH_DRAGON_DAGGER_P_CORRUPTED,
		ItemID.BH_DRAGON_DAGGER_P__CORRUPTED,
		ItemID.BH_DRAGON_DAGGER_P___CORRUPTED
	);

	private static final Color WARNING_TEXT = new Color(190, 80, 255);
	private static final Color MISSING_DAGGER_TEXT = new Color(255, 50, 50);

	private final OverlayManager overlayManager;
	private final Client client;
	private final TombsOfAmascutConfig config;

	@Inject
	public SpellbookWarningOverlay(Client client, TombsOfAmascutConfig config, OverlayManager overlayManager)
	{
		this.client = client;
		this.overlayManager = overlayManager;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.isInLobby() && (config.spellbookWarning() || config.daggerWarning());
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
		final Player player = client.getLocalPlayer();
		final Widget bank = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
		if ((!config.spellbookWarning() && !config.daggerWarning())
			|| player == null
			|| (bank != null && !bank.isHidden()))
		{
			return null;
		}

		final String spellbookText = "CURRENT SPELLBOOK: " + spellbookName(client.getVarbitValue(VarbitID.SPELLBOOK));
		final Font originalFont = graphics.getFont();
		graphics.setFont(FontManager.getRunescapeBoldFont());

		final Point spellbookLocation = player.getCanvasTextLocation(
			graphics, spellbookText, player.getLogicalHeight() + 60);
		if (config.spellbookWarning() && spellbookLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, spellbookLocation, spellbookText, WARNING_TEXT);
		}

		if (config.daggerWarning() && !InventoryUtil.containsAny(client, DAGGER_IDS))
		{
			final String daggerText = "dagger not in inventory";
			final Point daggerLocation = player.getCanvasTextLocation(
				graphics, daggerText, player.getLogicalHeight() + 40);
			if (daggerLocation != null)
			{
				OverlayUtil.renderTextLocation(graphics, daggerLocation, daggerText, MISSING_DAGGER_TEXT);
			}
		}

		graphics.setFont(originalFont);

		return null;
	}

	static String spellbookName(int spellbook)
	{
		switch (spellbook)
		{
			case 0:
				return "STANDARD";
			case 1:
				return "ANCIENT";
			case 2:
				return "LUNAR";
			case 3:
				return "ARCEUUS";
			default:
				return "UNKNOWN";
		}
	}
}
