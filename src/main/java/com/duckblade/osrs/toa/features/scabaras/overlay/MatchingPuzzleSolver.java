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
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MatchingPuzzleSolver implements PluginLifecycleComponent
{

	private static final Map<Integer, String> TILE_NAMES = ImmutableMap.<Integer, String>builder()
		.put(45356, "Line") // line
		.put(45357, "Knives") // knives
		.put(45358, "Crook") // crook
		.put(45359, "Diamond") // diamond
		.put(45360, "Hand") // hand
		.put(45361, "Star") // star
		.put(45362, "Bird") // bird
		.put(45363, "W") // wiggle
		.put(45364, "Boot") // boot
		.build();

	private static final Map<Integer, Color> TILE_COLORS = ImmutableMap.<Integer, Color>builder()
		.put(45356, Color.black) // line
		.put(45357, Color.red) // knives
		.put(45358, Color.magenta) // crook
		.put(45359, Color.blue) // diamond
		.put(45360, Color.lightGray) // hand
		.put(45361, Color.cyan) // star
		.put(45362, Color.pink) // bird
		.put(45363, Color.yellow) // wiggle
		.put(45364, Color.green) // boot
		.build();

	private static final Map<Integer, Integer> TILE_NUMBER = ImmutableMap.<Integer, Integer>builder()
		// these are intentionally out of id order since line, crook, hand, bird are always auto-completed in solos
		.put(45356, 1) // line
		.put(45358, 2) // crook
		.put(45360, 3) // hand
		.put(45362, 4) // bird
		.put(45357, 5) // knives
		.put(45359, 6) // diamond
		.put(45361, 7) // star
		.put(45363, 8) // wiggle
		.put(45364, 9) // boot
		.build();

	private static final Map<Integer, Integer> MATCHED_OBJECT_IDS = ImmutableMap.<Integer, Integer>builder().put(45388, 1) // line
		.put(45389, 45357) // knives
		.put(45386, 45358) // crook
		.put(45391, 45359) // diamond
		.put(45392, 45360) // hand
		.put(45387, 45361) // star
		.put(45393, 45362) // bird
		.put(45394, 45363) // wiggle
		.put(45395, 45364) // boot
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
