package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidStateChanged;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
@Slf4j
public class HelpfulSpiritHighlighter extends Overlay implements PluginLifecycleComponent {
    private final EventBus eventBus;
    private final Client client;
    private final TombsOfAmascutConfig config;
    private final OverlayManager overlayManager;

    // Tracks the number of bosses completed; the helpful spirit appears at a count of 2 and 4
    private int pathCompleteCount = 0;

    @Inject
    public HelpfulSpiritHighlighter(
            EventBus eventBus, Client client,
            TombsOfAmascutConfig config,
            OverlayManager overlayManager
    ) {
        this.eventBus = eventBus;
        this.client = client;
        this.config = config;
        this.overlayManager = overlayManager;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    /**
     * Draws an outline around the correct helpful spirit bundle
     */
    @Override
    public Dimension render(Graphics2D graphics) {
        HelpfulSpiritBundleType selection;
        if (pathCompleteCount == 2) {
            selection = config.firstHelpfulSpiritSelection();
        } else {
            selection = config.secondHelpfulSpiritSelection();
        }
        Widget button = client.getWidget(selection.widgetId);
        if (button != null && !button.isHidden()) {
            Rectangle answerRect = button.getBounds();
            graphics.setColor(Color.CYAN);
            graphics.draw(answerRect);
        }

        return null;
    }

    /**
     * Prevents unwanted Helpful Spirit bundles from being left-clicked
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
        if (widget == null) {
            return;
        }

        int widgetId = widget.getId();
        // If button being clicked isn't one of the Helpful Spirit bundle buttons, let the click go through as normal
        if (!(widgetId == HelpfulSpiritBundleType.CHAOS.widgetId ||
                widgetId == HelpfulSpiritBundleType.POWER.widgetId ||
                widgetId == HelpfulSpiritBundleType.LIFE.widgetId)) {
            return;
        }

        HelpfulSpiritBundleType selection;
        if (pathCompleteCount == 2) {
            selection = config.firstHelpfulSpiritSelection();
        } else {
            selection = config.secondHelpfulSpiritSelection();
        }

        // If user is attempting to click the wrong bundle option, stop the click
        if (widgetId != selection.widgetId)
        {
            event.consume();
        }
    }

    /**
     * Keep track of player's progress through the raid so that the correct helpful spirit bundle is highlighted at the
     * correct time.
     */
    @Subscribe
    public void onRaidStateChanged(RaidStateChanged e) {
        RaidRoom prevRoom = e.getPreviousState().getCurrentRoom();
        RaidRoom newRoom = e.getNewState().getCurrentRoom();
        log.debug("Raid State Changed: Previous room was " + prevRoom + ", new room is " + newRoom);
        // Upon starting a new raid, reset number of paths complete.
        // Increment once player has returned to Nexus after killing a boss.
        if (prevRoom == null) {
            pathCompleteCount = 0;
            log.debug("Reset path complete count to 0");
        } else if (newRoom == RaidRoom.NEXUS) {
            pathCompleteCount += 1;
            log.debug("Path complete count is now: " + pathCompleteCount);
        }
    }

    @Override
    public void startUp() {
        pathCompleteCount = 0;
        eventBus.register(this);
        overlayManager.add(this);
    }

    @Override
    public void shutDown() {
        eventBus.unregister(this);
        overlayManager.remove(this);
    }

    public enum HelpfulSpiritBundleType {
        LIFE(50921478),
        CHAOS(50921481),
        POWER(50921484);

        private final int widgetId;
        HelpfulSpiritBundleType(int widgetId) {
            this.widgetId = widgetId;
        }
    }
}
