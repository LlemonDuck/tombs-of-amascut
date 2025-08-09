package com.duckblade.osrs.toa.features.scabaras.overlay;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.scabaras.ScabarasHelperMode;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ImmutableMap;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MatchingPuzzleSolver implements PluginLifecycleComponent
{

	private static final Map<Integer, String> TILE_NAMES = ImmutableMap.<Integer, String>builder()
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE1, "Line") // line
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE2, "Knives") // knives
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE3, "Crook") // crook
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE4, "Diamond") // diamond
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE5, "Hand") // hand
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE6, "Star") // star
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE7, "Bird") // bird
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE8, "W") // wiggle
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE9, "Boot") // boot
		.build();

	private static final Map<Integer, Color> TILE_COLORS = ImmutableMap.<Integer, Color>builder()
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE1, Color.black) // line
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE2, Color.red) // knives
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE3, Color.magenta) // crook
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE4, Color.blue) // diamond
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE5, Color.lightGray) // hand
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE6, Color.cyan) // star
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE7, Color.pink) // bird
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE8, Color.yellow) // wiggle
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE9, Color.green) // boot
		.build();

	private static final Map<Integer, Integer> TILE_NUMBER = ImmutableMap.<Integer, Integer>builder()
		// these are intentionally out of id order since line, crook, hand, bird are always auto-completed in solos
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE1, 1) // line
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE3, 2) // crook
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE5, 3) // hand
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE7, 4) // bird
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE2, 5) // knives
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE4, 6) // diamond
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE6, 7) // star
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE8, 8) // wiggle
		.put(ObjectID.TOA_SCABARAS_MEMORYGAME_TILE9, 9) // boot
		.build();

	private static final Map<Integer, Integer> MATCHED_OBJECT_IDS = ImmutableMap.<Integer, Integer>builder()
		.put(ObjectID.TOA_SCABARAS_FX05, ObjectID.TOA_SCABARAS_MEMORYGAME_TILE1) // line
		.put(ObjectID.TOA_SCABARAS_FX06, ObjectID.TOA_SCABARAS_MEMORYGAME_TILE2) // knives
		.put(ObjectID.TOA_SCABARAS_FX03, ObjectID.TOA_SCABARAS_MEMORYGAME_TILE3) // crook
		.put(ObjectID.TOA_SCABARAS_FX08, ObjectID.TOA_SCABARAS_MEMORYGAME_TILE4) // diamond
		.put(ObjectID.TOA_SCABARAS_FX09, ObjectID.TOA_SCABARAS_MEMORYGAME_TILE5) // hand
		.put(ObjectID.TOA_SCABARAS_FX04, ObjectID.TOA_SCABARAS_MEMORYGAME_TILE6) // star
		.put(ObjectID.TOA_SCABARAS_FX10, ObjectID.TOA_SCABARAS_MEMORYGAME_TILE7) // bird
		.put(ObjectID.TOA_SCABARAS_FX11, ObjectID.TOA_SCABARAS_MEMORYGAME_TILE8) // wiggle
		.put(ObjectID.TOA_SCABARAS_FX12, ObjectID.TOA_SCABARAS_MEMORYGAME_TILE9) // boot
		.build();

	private final EventBus eventBus;

	@Getter
	private final Map<LocalPoint, MatchingTile> discoveredTiles = new HashMap<>(18);

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.scabarasHelperMode() == ScabarasHelperMode.OVERLAY && raidState.getCurrentRoom() == RaidRoom.SCABARAS;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		discoveredTiles.clear();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned e)
	{
		int id = e.getGroundObject().getId();
		if (TILE_COLORS.containsKey(id))
		{
			LocalPoint lp = e.getGroundObject().getLocalLocation();
			discoveredTiles.put(lp, new MatchingTile(lp, TILE_NAMES.getOrDefault(id, "Unknown"), TILE_COLORS.getOrDefault(id, Color.black), TILE_NUMBER.getOrDefault(id, 0)));
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		int gameId = e.getGameObject().getId();
		if (MATCHED_OBJECT_IDS.containsKey(gameId))
		{
			MatchingTile match = discoveredTiles.get(e.getGameObject().getLocalLocation());
			if (match == null)
			{
				log.debug("Failed to find discovered tile for game object id {}!", gameId);
				return;
			}

			match.setMatched(true);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getMessage().startsWith("Your party failed to complete the challenge"))
		{
			discoveredTiles.clear();
		}
	}
}
