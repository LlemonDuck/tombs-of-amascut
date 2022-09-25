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
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MatchingPuzzleSolver implements PluginLifecycleComponent
{

	private static final Map<Integer, Color> TILE_COLORS = ImmutableMap.<Integer, Color>builder()
		.put(45365, Color.black) // line
		.put(45366, Color.red) // knives
		.put(45367, Color.orange) // crook
		.put(45368, Color.blue) // diamond
		.put(45369, Color.lightGray) // hand
		.put(45370, Color.cyan) // star
		.put(45371, Color.pink) // bird
		.put(45372, Color.yellow) // wiggle
		.put(45373, Color.green) // boot
		.build();

	private final EventBus eventBus;

	@Getter
	private final Map<LocalPoint, Color> discoveredTiles = new HashMap<>();

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.scabarasHelperMode() == ScabarasHelperMode.OVERLAY &&
			raidState.getCurrentRoom() == RaidRoom.SCABARAS;
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
		if (TILE_COLORS.containsKey(e.getGroundObject().getId()))
		{
			discoveredTiles.put(e.getGroundObject().getLocalLocation(), TILE_COLORS.get(e.getGroundObject().getId()));
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
