package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidCompletionTracker;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
@Slf4j
public class HelpfulSpiritHighlighter extends Overlay implements PluginLifecycleComponent
{
	private final EventBus eventBus;
	private final Client client;
	private final TombsOfAmascutConfig config;
	private final OverlayManager overlayManager;
	private final RaidCompletionTracker raidCompletionTracker;

	private boolean isFirstPass;

	@Inject
	public HelpfulSpiritHighlighter(
		EventBus eventBus,
		Client client,
		TombsOfAmascutConfig config,
		OverlayManager overlayManager,
		RaidCompletionTracker raidCompletionTracker
	)
	{
		this.eventBus = eventBus;
		this.client = client;
		this.config = config;
		this.overlayManager = overlayManager;
		this.raidCompletionTracker = raidCompletionTracker;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		this.isFirstPass = raidCompletionTracker.getCompletedBosses().size() <= 4;

		return config.enableHelpfulSpiritHighlight() &&
			raidState.getCurrentRoom() == RaidRoom.NEXUS;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		overlayManager.remove(this);
	}

	/**
	 * Draws an outline around the correct helpful spirit bundle
	 */
	@Override
	public Dimension render(Graphics2D graphics)
	{
		Widget button = client.getWidget(this.getCurrentBundleType().widgetId);
		if (button != null && !button.isHidden())
		{
			Rectangle answerRect = button.getBounds();
			graphics.setColor(Color.CYAN);
			graphics.draw(answerRect);
		}
		return null;
	}

	/**
	 * Prevents unwanted Helpful Spirit bundles from being left-clicked
	 *
	 * @param event Click event
	 */
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuAction() != MenuAction.CC_OP)
		{
			return;
		}

		Widget widget = event.getWidget();
		if (widget == null)
		{
			return;
		}

		int widgetId = widget.getId();
		// If button being clicked isn't one of the Helpful Spirit bundle buttons, let the click go through as normal
		if (!(widgetId == BundleType.CHAOS.widgetId ||
			widgetId == BundleType.POWER.widgetId ||
			widgetId == BundleType.LIFE.widgetId))
		{
			return;
		}

		// If user is attempting to click the wrong bundle option, stop the click
		if (widgetId != this.getCurrentBundleType().widgetId)
		{
			event.consume();
		}
	}

	private BundleType getCurrentBundleType()
	{
		boolean isFirstPass = raidCompletionTracker.getCompletedBosses().size() <= 4;
		return isFirstPass ? config.firstHelpfulSpiritSelection() : config.secondHelpfulSpiritSelection();
	}

	public enum BundleType
	{
		LIFE(50921478),
		CHAOS(50921481),
		POWER(50921484);

		private final int widgetId;

		BundleType(int widgetId)
		{
			this.widgetId = widgetId;
		}
	}
}
