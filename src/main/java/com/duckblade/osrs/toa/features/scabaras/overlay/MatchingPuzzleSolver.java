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

	private static final Map<Integer, String> TILE_NAMES = ImmutableMap.<Integer, String>builder().put(45365, "Line") // line
		.put(45366, "Knives") // knives
		.put(45367, "Crook") // crook
		.put(45368, "Diamond") // diamond
		.put(45369, "Hand") // hand
		.put(45370, "Star") // star
		.put(45371, "Bird") // bird
		.put(45372, "W") // wiggle
		.put(45373, "Boot") // boot
		.build();

	private static final Map<Integer, Color> TILE_COLORS = ImmutableMap.<Integer, Color>builder().put(45365, Color.black) // line
		.put(45366, Color.red) // knives
		.put(45367, Color.magenta) // crook
		.put(45368, Color.blue) // diamond
		.put(45369, Color.lightGray) // hand
		.put(45370, Color.cyan) // star
		.put(45371, Color.pink) // bird
		.put(45372, Color.yellow) // wiggle
		.put(45373, Color.green) // boot
		.build();

	private static final Map<Integer, Integer> MATCHED_OBJECT_IDS = ImmutableMap.<Integer, Integer>builder().put(45388, 45365) // line
		.put(45389, 45366) // knives
		.put(45386, 45367) // crook
		.put(45391, 45368) // diamond
		.put(45392, 45369) // hand
		.put(45387, 45370) // star
		.put(45393, 45371) // bird
		.put(45394, 45372) // wiggle
		.put(45395, 45373) // boot
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
			discoveredTiles.put(lp, new MatchingTile(lp, TILE_NAMES.getOrDefault(id, "Unknown"), TILE_COLORS.getOrDefault(id, Color.black)));
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
