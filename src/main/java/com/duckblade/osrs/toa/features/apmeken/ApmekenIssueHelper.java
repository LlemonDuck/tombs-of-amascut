package com.duckblade.osrs.toa.features.apmeken;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.Player;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ApmekenIssueHelper implements PluginLifecycleComponent
{
	private static final int ANIMATION_PILLAR_HAMMER = 3676;
	private static final int ANIMATION_VENT_POUR = 2295;

	private static final int MODEL_SKULL_SOUTH = 45327;
	private static final int MODEL_SKULL_NORTH = 45330;

	private static final String MESSAGE_ISSUE_SENSED = "You sense an issue somewhere in the room.";
	private static final String MESSAGE_FUMES_SENSED = "You sense some strange fumes coming from holes in the floor.";
	private static final String MESSAGE_ROOF_SENSED = "You sense an issue with the roof supports.";
	private static final String MESSAGE_FUMES_NEUTRALIZED = "Apmeken's Sight guides your group into neutralising some dangerous fumes.";
	private static final String MESSAGE_FUMES_IGNITE = "The fumes filling the room suddenly ignite!";
	private static final String MESSAGE_ROOF_REPAIRED = "Apmeken's Sight guides your group into repairing the roof supports.";
	private static final String MESSAGE_DEBRIS_FALL = "Damaged roof supports cause some debris to fall on you!";


	private final Client client;
	private final EventBus eventBus;

	private boolean issueActive = false;

	private final Map<LocalPoint, RuneLiteObject> activeSkulls = new HashMap<>();
	private final Map<WorldPoint, Boolean> fixedLocations = new HashMap<>();

	private static final List<WorldPoint> PILLAR_LOCATIONS = List.of(
		new WorldPoint(12636, 2792, 0), // north-west
		new WorldPoint(12644, 2792, 0), // north-east
		new WorldPoint(12636, 2776, 0), // south-west
		new WorldPoint(12644, 2776, 0)  // south-east
	);

	private static final List<WorldPoint> VENT_LOCATIONS = List.of(
		new WorldPoint(12632, 2788, 0), // north-west
		new WorldPoint(12648, 2788, 0), // north-east
		new WorldPoint(12632, 2780, 0), // south-west
		new WorldPoint(12648, 2780, 0)  // south-east
	);

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.apmekenIssueHelper() && raidState.getCurrentRoom() == RaidRoom.APMEKEN;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		reset();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		clearAllSkulls();
		reset();
	}

	private void reset()
	{
		issueActive = false;
		clearAllSkulls();
		fixedLocations.clear();
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = e.getMessage().replaceAll("<.*?>", "");
		if (message.equals(MESSAGE_ISSUE_SENSED) ||
			message.equals(MESSAGE_FUMES_SENSED) ||
			message.equals(MESSAGE_ROOF_SENSED))
		{
			issueActive = true;
		}
		else if (message.equals(MESSAGE_FUMES_NEUTRALIZED) ||
			message.equals(MESSAGE_FUMES_IGNITE) ||
			message.equals(MESSAGE_ROOF_REPAIRED) ||
			message.equals(MESSAGE_DEBRIS_FALL))
		{
			clearAllSkulls();
			fixedLocations.clear();
			issueActive = false;
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged e)
	{
		if (!(e.getActor() instanceof Player))
		{
			return;
		}

		Player player = (Player) e.getActor();
		int animationId = player.getAnimation();
		WorldPoint playerLocation = player.getWorldLocation();

		if (playerLocation == null)
		{
			return;
		}
		if (animationId == ANIMATION_PILLAR_HAMMER)
		{
			WorldPoint fixedLocation = findNearestPillar(playerLocation);
			if (fixedLocation != null)
			{
				handleIssueFixed(fixedLocation, PILLAR_LOCATIONS, issueActive);
				issueActive = false; // Just to be able to distinguish between drawing skulls on other locations or not
			}
		}
		else if (animationId == ANIMATION_VENT_POUR)
		{
			WorldPoint fixedLocation = findVentAtLocation(playerLocation);
			if (fixedLocation != null)
			{
				handleIssueFixed(fixedLocation, VENT_LOCATIONS, issueActive);
				issueActive = false; // Just to be able to distinguish between drawing skulls on other locations or not
			}
		}
	}

	private void handleIssueFixed(WorldPoint fixedLocation, List<WorldPoint> allLocations, boolean issueActive)
	{
		fixedLocations.put(fixedLocation, true);
		removeSkullAtTile(fixedLocation);

		if (issueActive)
		{
			showSkullsOnOtherLocations(fixedLocation, allLocations);
		}
	}

	private void showSkullsOnOtherLocations(WorldPoint fixedLocation, List<WorldPoint> allLocations)
	{
		for (WorldPoint location : allLocations)
		{
			if (!location.equals(fixedLocation) && !fixedLocations.containsKey(location))
			{
				showSkullOnTile(location);
			}
		}
	}

	private WorldPoint findVentAtLocation(WorldPoint playerLocation)
	{
		for (WorldPoint vent : VENT_LOCATIONS)
		{
			if (vent.equals(playerLocation))
			{
				return vent;
			}
		}
		return null;
	}

	private WorldPoint findNearestPillar(WorldPoint playerLocation)
	{
		WorldPoint nearest = null;
		double minDistance = Double.MAX_VALUE;
		for (WorldPoint pillar : PILLAR_LOCATIONS)
		{
			double distance = playerLocation.distanceTo(pillar);
			if (distance < minDistance)
			{
				minDistance = distance;
				nearest = pillar;
			}
		}

		return nearest;
	}

	private void showSkullOnTile(WorldPoint worldPoint)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
		if (localPoint == null)
		{
			return;
		}

		if (activeSkulls.containsKey(localPoint))
		{
			return;
		}

		boolean isNorth = worldPoint.getY() >= 2788;
		int modelId = isNorth ? MODEL_SKULL_SOUTH : MODEL_SKULL_NORTH;

		Model model = client.loadModel(modelId);
		if (model == null)
		{
			return;
		}


		RuneLiteObject rlObject = client.createRuneLiteObject();
		rlObject.setLocation(localPoint, client.getPlane());
		rlObject.setModel(model);
		rlObject.setActive(true);
		client.registerRuneLiteObject(rlObject);
		activeSkulls.put(localPoint, rlObject);
	}

	private void clearAllSkulls()
	{
		for (RuneLiteObject rlObject : activeSkulls.values())
		{
			if (rlObject != null && client.isRuneLiteObjectRegistered(rlObject))
			{
				client.removeRuneLiteObject(rlObject);
			}
		}
		activeSkulls.clear();
	}

	private void removeSkullAtTile(WorldPoint worldPoint)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
		if (localPoint == null)
		{
			return;
		}


		RuneLiteObject rlObject = activeSkulls.remove(localPoint);
		if (rlObject != null && client.isRuneLiteObjectRegistered(rlObject))
		{
			client.removeRuneLiteObject(rlObject);
		}
	}

}

