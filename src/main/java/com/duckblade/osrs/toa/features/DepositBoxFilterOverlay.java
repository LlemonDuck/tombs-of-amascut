package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

@Singleton
public class DepositBoxFilterOverlay extends Overlay implements PluginLifecycleComponent
{
	private final OverlayManager overlayManager;

	private final Client client;
	private final DepositBoxFilter depositBoxFilter;

	@Inject
	public DepositBoxFilterOverlay(
		final OverlayManager overlayManager,
		final Client client,
		final DepositBoxFilter depositBoxFilter
	)
	{
		this.overlayManager = overlayManager;
		this.client = client;
		this.depositBoxFilter = depositBoxFilter;

		setPriority(Overlay.PRIORITY_HIGH);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState currentState)
	{
		return currentState.getCurrentRoom() == RaidRoom.NEXUS &&
			config.depositBoxInterfaceOverlay();
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
		for (int depositBoxSlotId : DepositBoxFilter.DEPOSIT_BOX_SLOT_IDS)
		{
			Widget slotWidget = client.getWidget(depositBoxSlotId);
			if (slotWidget == null)
			{
				continue;
			}

			// the inventory slots are dynamic children (worn are not), so we need to actually render per-child for those
			if (slotWidget.getId() == InterfaceID.BankDepositbox.INVENTORY)
			{
				Widget[] children = slotWidget.getChildren();
				if (children == null || children.length == 0)
				{
					continue;
				}

				for (Widget child : children)
				{
					renderOnItemSlot(graphics, child);
				}
			}
			else
			{
				renderOnItemSlot(graphics, slotWidget);
			}
		}

		return null;
	}

	private void renderOnItemSlot(Graphics2D graphics, Widget slotWidget)
	{
		// quiver doesn't use hidden=true for some reason, it just sets the bg widget spriteid to -1, but skip if it's hidden
		if (slotWidget.getId() == InterfaceID.BankDepositbox.EXTRA_QUIVER_AMMO &&
			isQuiverSlotHidden(slotWidget))
		{
			return;
		}

		// ignore empty slots
		if (slotWidget.isHidden() ||
			slotWidget.getItemId() == ItemID.BLANKOBJECT ||
			slotWidget.getName() == null ||
			slotWidget.getActions() == null ||
			slotWidget.getActions().length == 0)
		{
			return;
		}

		Color color = depositBoxFilter.isDepositAllowed(slotWidget.getName())
			? Color.GREEN
			: Color.RED;

		Shape bounds = slotWidget.getBounds().getBounds2D();
		graphics.setColor(ColorUtil.colorWithAlpha(color, 127));
		graphics.fill(bounds);
		graphics.setColor(color);
		graphics.draw(bounds);
	}

	private boolean isQuiverSlotHidden(Widget slotWidget)
	{
		Widget quiverAmmoGraphicChild = slotWidget.getChild(0);
		return quiverAmmoGraphicChild == null ||
			quiverAmmoGraphicChild.getItemId() == -1;
	}

}
