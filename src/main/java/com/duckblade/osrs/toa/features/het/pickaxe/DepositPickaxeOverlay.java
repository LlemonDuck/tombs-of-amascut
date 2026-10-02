package com.duckblade.osrs.toa.features.het.pickaxe;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.LobbyEntranceWarning;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.IdentityHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class DepositPickaxeOverlay
	extends Overlay
	implements PluginLifecycleComponent, LobbyEntranceWarning.Check
{

	private static final Color OVERLAY_COLOR = new Color(225, 163, 12);
	private static final String WARNING_TEXT = "Deposit a pickaxe.";

	private final Client client;
	private final RaidStateTracker raidStateTracker;

	private IdentityHashMap<GameObject, Boolean> highlightObjects;
	private GameObject entry;
	private boolean roomComplete;

	@Inject
	public DepositPickaxeOverlay(
		Client client,
		RaidStateTracker raidStateTracker
	)
	{
		this.client = client;
		this.raidStateTracker = raidStateTracker;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return (raidState.getCurrentRoom() == RaidRoom.HET && config.hetPickaxePuzzleOverlay()) ||
			(raidState.isInLobby() && config.hetPickaxeLobbyOverlay());
	}

	@Override
	public void startUp()
	{
		entry = null;
		highlightObjects = new IdentityHashMap<>();
		roomComplete = false;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (PickaxeUtil.pickaxeIsInStorage(client))
		{
			return null;
		}

		if (raidStateTracker.getCurrentState().getCurrentRoom() == RaidRoom.HET)
		{
			if (!roomComplete || !PickaxeUtil.hasPickaxe(client))
			{
				return null;
			}
		}

		for (GameObject toHighlight : highlightObjects.keySet())
		{
			Shape hull;
			if ((hull = toHighlight.getConvexHull()) != null)
			{
				OverlayUtil.renderPolygon(graphics, hull, OVERLAY_COLOR);
			}
		}

		Point textPoint;
		if (entry != null && (textPoint = entry.getCanvasTextLocation(graphics, WARNING_TEXT, 0)) != null)
		{
			OverlayUtil.renderTextLocation(graphics, textPoint, WARNING_TEXT, OVERLAY_COLOR);
		}

		return null;
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		switch (e.getGameObject().getId())
		{
			case ObjectID.TOA_LOBBY_WALL02_CAVITY_PARENT:
			case ObjectID.TOA_HET_STATUE_PARENT:
				highlightObjects.put(e.getGameObject(), true);
				break;

			case ObjectID.TOA_DOOR_CONTINUE:
				entry = e.getGameObject();
				break;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned e)
	{
		switch (e.getGameObject().getId())
		{
			case ObjectID.TOA_LOBBY_WALL02_CAVITY_PARENT:
			case ObjectID.TOA_HET_STATUE_PARENT:
				highlightObjects.remove(e.getGameObject());
				break;

			case ObjectID.TOA_DOOR_CONTINUE:
				entry = null;
				break;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() == ChatMessageType.GAMEMESSAGE && e.getMessage().startsWith("Challenge complete"))
		{
			roomComplete = true;
		}
	}

	@Override
	public boolean isTriggered()
	{
		return !PickaxeUtil.hasPickaxe(client);
	}

	@Override
	public String getText()
	{
		return WARNING_TEXT;
	}

	@Override
	public Color getColor()
	{
		return OVERLAY_COLOR;
	}
}
