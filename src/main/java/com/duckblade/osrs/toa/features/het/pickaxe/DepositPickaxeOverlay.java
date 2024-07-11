package com.duckblade.osrs.toa.features.het.pickaxe;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
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
import net.runelite.api.NullObjectID;
import net.runelite.api.ObjectID;
import net.runelite.api.Point;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class DepositPickaxeOverlay extends Overlay implements PluginLifecycleComponent
{

	private static final int OBJECT_AKKHA_ENTRY = ObjectID.ENTRY_45131;
	private static final int OBJECT_RAID_ENTRY = ObjectID.ENTRY_46089;
	private static final int OBJECT_EMPTY_CAVITY = NullObjectID.NULL_49566;
	private static final int OBJECT_STATUE = NullObjectID.NULL_45468;
	private static final Color OVERLAY_COLOR = new Color(225, 163, 12);
	private static final String WARNING_TEXT = "Deposit a pickaxe.";

	private final EventBus eventBus;
	private final OverlayManager overlayManager;
	private final Client client;
	private final RaidStateTracker raidStateTracker;

	private IdentityHashMap<GameObject, Boolean> highlightObjects;
	private GameObject entry;
	private boolean roomComplete;

	@Inject
	public DepositPickaxeOverlay(
		EventBus eventBus,
		OverlayManager overlayManager,
		Client client,
		RaidStateTracker raidStateTracker
	)
	{
		this.eventBus = eventBus;
		this.overlayManager = overlayManager;
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
		eventBus.register(this);
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		overlayManager.remove(this);
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
			case OBJECT_EMPTY_CAVITY:
			case OBJECT_STATUE:
				highlightObjects.put(e.getGameObject(), true);
				break;

			case OBJECT_RAID_ENTRY:
			case OBJECT_AKKHA_ENTRY:
				entry = e.getGameObject();
				break;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned e)
	{
		switch (e.getGameObject().getId())
		{
			case OBJECT_EMPTY_CAVITY:
			case OBJECT_STATUE:
				highlightObjects.remove(e.getGameObject());
				break;

			case OBJECT_RAID_ENTRY:
			case OBJECT_AKKHA_ENTRY:
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

}
