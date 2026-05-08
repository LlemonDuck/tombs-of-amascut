package com.duckblade.osrs.toa.features.nexus;

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
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
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
	private static final int SOUND_EFFECT_DENIED = 2277;
	private static final String ACTION_NO_BUNDLE_SELECT = "ToA Plugin Info";

	private final EventBus eventBus;
	private final Client client;
	private final TombsOfAmascutConfig config;
	private final OverlayManager overlayManager;
	private final RaidCompletionTracker raidCompletionTracker;

	private boolean enableHighlightBundles;
	private boolean deprioritizeOtherBundles;

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
		this.enableHighlightBundles = config.enableHighlightBundles();
		this.deprioritizeOtherBundles = config.deprioritizeOtherBundles();

		return (this.enableHighlightBundles || this.deprioritizeOtherBundles) &&
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
		if (!enableHighlightBundles)
		{
			return null;
		}

		Widget button = client.getWidget(this.getDesiredBundleType().getWidgetId());
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
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		MenuEntry me = event.getMenuEntry();
		if (!deprioritizeOtherBundles ||
			me.getType() != MenuAction.CC_OP)
		{
			return;
		}

		Widget widget = me.getWidget();
		if (widget == null)
		{
			return;
		}

		BundleType desiredBundleType = getDesiredBundleType();
		BundleType actualBundleType = BundleType.byWidgetId(widget.getId());
		if (actualBundleType == null ||
			actualBundleType == desiredBundleType)
		{
			return;
		}

		// deprioritize allows the user to still right-click select, but not left-click
		me.setDeprioritized(true);

		// add a feedback action so users aren't confused why they can't select it
		client.getMenu()
			.createMenuEntry(-1)
			.setType(MenuAction.RUNELITE_WIDGET)
			.setOption(ACTION_NO_BUNDLE_SELECT)
			.setTarget(me.getTarget())
			.onClick(_clicked ->
			{
				client.playSoundEffect(SOUND_EFFECT_DENIED);
				client.addChatMessage(
					ChatMessageType.GAMEMESSAGE,
					"",
					"Bundle selection prevented by config. " +
						"To select this bundle, reconfigure options in Tombs of Amascut -> Helpful Spirit, " +
						"or use the right-click menu.",
					"RL/Tombs of Amascut"
				);
			});
	}

	private BundleType getDesiredBundleType()
	{
		boolean isFirstPass = raidCompletionTracker.getCompletedBosses().size() <= 4;
		return isFirstPass ? config.firstHelpfulSpiritSelection() : config.secondHelpfulSpiritSelection();
	}

}
