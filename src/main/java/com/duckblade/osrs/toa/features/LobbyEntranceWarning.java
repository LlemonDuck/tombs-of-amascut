package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

/**
 * Exists to ensure that multiple different raid-entrance
 * warning features do not render their text directly
 * on top of each other. It exclusively handles the lobby
 * entrance and not any other objects or rooms that reminders
 * would want to be rendered on/in.
 */
@Singleton
public class LobbyEntranceWarning
	extends Overlay
	implements PluginLifecycleComponent
{

	public interface Check
	{
		boolean isTriggered();

		String getText();

		Color getColor();
	}

	private final OverlayManager overlayManager;
	private final EventBus eventBus;
	private final Set<Check> checks;

	private GameObject entrance;

	@Inject
	LobbyEntranceWarning(
		OverlayManager overlayManager,
		EventBus eventBus,
		Set<Check> checks
	)
	{
		this.overlayManager = overlayManager;
		this.eventBus = eventBus;
		this.checks = checks;

		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.isInLobby();
	}

	@Override
	public void startUp()
	{
		overlayManager.add(this);
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(this);
		eventBus.unregister(this);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Set<Check> active = checks.stream()
			.filter(Check::isTriggered)
			.collect(Collectors.toSet());

		graphics.setFont(FontManager.getRunescapeBoldFont());
		int lineHeight = graphics.getFontMetrics().getHeight();

		int line = 0;
		for (Check c : active)
		{
			Point textPoint;
			if (entrance != null && (textPoint = entrance.getCanvasTextLocation(graphics, c.getText(), 0)) != null)
			{
				Point linePoint = new Point(textPoint.getX(), textPoint.getY() + (line * lineHeight));
				OverlayUtil.renderTextLocation(graphics, linePoint, c.getText(), c.getColor());
			}

			line++;
		}

		return null;
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		GameObject obj = e.getGameObject();
		if (obj.getId() == ObjectID.TOA_LOBBY_RAID_ENTRY)
		{
			entrance = obj;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned e)
	{
		if (e.getGameObject() == entrance)
		{
			entrance = null;
		}
	}
}
