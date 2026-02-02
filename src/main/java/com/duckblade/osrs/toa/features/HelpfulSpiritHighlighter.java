package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.events.ChatMessage;
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
import java.util.regex.Pattern;

@Singleton
@Slf4j
public class HelpfulSpiritHighlighter extends Overlay implements PluginLifecycleComponent {
    private static final String ROOM_COMPLETE_PREFIX = "Challenge complete";
    private static final Pattern ROOM_COMPLETE_PATTERN =
            Pattern.compile("Challenge complete: ([A-Za-z-]+).*Total:.*?([0-9]+:[.0-9]+).*");

    private final EventBus eventBus;
    private final Client client;
    private final TombsOfAmascutConfig config;
    private final OverlayManager overlayManager;

    // Tracks the number of rooms completed; boss rooms and puzzle rooms are counted separately,
    // so the helpful spirit appears at a pathCompleteCount of 4 and 8.
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
        if (pathCompleteCount == 4) {
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
        if (pathCompleteCount == 4) {
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
     * Track completion of each room to increment pathCompleteCount
     */
    @Subscribe
    public void onChatMessage(ChatMessage e)
    {
        if (e.getType() != ChatMessageType.GAMEMESSAGE)
        {
            return;
        }

        String msg = e.getMessage();
        if (!msg.startsWith(ROOM_COMPLETE_PREFIX))
        {
            return;
        }

        if (ROOM_COMPLETE_PATTERN.matcher(msg).matches())
        {
            pathCompleteCount += 1;
        }
    }

    @Override
    public void startUp() {
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
