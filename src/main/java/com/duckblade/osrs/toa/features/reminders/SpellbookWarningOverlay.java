package com.duckblade.osrs.toa.features.reminders;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
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

		if (config.daggerWarning() && !hasDagger(client.getItemContainer(InventoryID.INVENTORY)))
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

	static boolean hasDagger(ItemContainer inventory)
	{
		if (inventory == null)
		{
			return false;
		}

		for (Item item : inventory.getItems())
		{
			if (isDagger(item.getId()))
			{
				return true;
			}
		}
		return false;
	}

	static boolean isDagger(int itemId)
	{
		switch (itemId)
		{
			case ItemID.DRAGON_DAGGER:
			case ItemID.DRAGON_DAGGER_P:
			case ItemID.DRAGON_DAGGER_P_:
			case ItemID.DRAGON_DAGGER_P__:
			case ItemID.ABYSSAL_DAGGER:
			case ItemID.ABYSSAL_DAGGER_P:
			case ItemID.ABYSSAL_DAGGER_P_:
			case ItemID.ABYSSAL_DAGGER_P__:
			case ItemID.BR_DRAGON_DAGGER:
			case ItemID.BH_ABYSSAL_DAGGER_IMBUE:
			case ItemID.BH_ABYSSAL_DAGGER_P_IMBUE:
			case ItemID.BH_ABYSSAL_DAGGER_P__IMBUE:
			case ItemID.BH_ABYSSAL_DAGGER_P___IMBUE:
			case ItemID.BH_DRAGON_DAGGER_CORRUPTED:
			case ItemID.BH_DRAGON_DAGGER_P_CORRUPTED:
			case ItemID.BH_DRAGON_DAGGER_P__CORRUPTED:
			case ItemID.BH_DRAGON_DAGGER_P___CORRUPTED:
				return true;
			default:
				return false;
		}
	}
}
