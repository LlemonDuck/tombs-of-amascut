package com.duckblade.osrs.toa.features.het.beamtimer;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.NpcChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BeamTimerTracker implements PluginLifecycleComponent
{

	private static final String CHALLENGE_START_MESSAGE = "Challenge started: Path of Het.";
	private static final String CHALLENGE_COMPLETE_MESSAGE = "Challenge complete: Path of Het.";

	private static final int BEAM_FIRE_RATE_TICKS = 9;

	// at least one of these three is guaranteed to happen
	// there are others for each corner direction
	private static final Set<Integer> BEAM_GRAPHICS_OBJECT_IDS = ImmutableSet.of(
		2114, // horizontal
		2064, // vertical
		2120 // crash (into an object)
	);

	private final EventBus eventBus;
	private final Client client;

	@Getter
	private GameObject casterStatue;

	private int nextFireTick = -1;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.hetBeamTimerEnable() &&
			raidState.getCurrentRoom() == RaidRoom.HET;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		if (e.getMessage().equals(CHALLENGE_START_MESSAGE))
		{
			this.nextFireTick = client.getTickCount() + BEAM_FIRE_RATE_TICKS + 1;
		}
		else if (e.getMessage().equals(CHALLENGE_COMPLETE_MESSAGE))
		{
			this.nextFireTick = -1;
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		if (e.getGameObject().getId() == ObjectID.CASTER_STATUE)
		{
			casterStatue = e.getGameObject();
		}
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated e)
	{
		if (BEAM_GRAPHICS_OBJECT_IDS.contains(e.getGraphicsObject().getId()))
		{
			this.nextFireTick = client.getTickCount() + BEAM_FIRE_RATE_TICKS;
		}
	}

	@Subscribe
	public void onNpcChanged(NpcChanged e)
	{
		if (e.getOld().getId() == NpcID.HETS_SEAL_WEAKENED && e.getNpc().getId() == NpcID.HETS_SEAL_PROTECTED)
		{
			this.nextFireTick = client.getTickCount() + BEAM_FIRE_RATE_TICKS + 1;
		}
		else if (e.getOld().getId() == NpcID.HETS_SEAL_PROTECTED && e.getNpc().getId() == NpcID.HETS_SEAL_WEAKENED)
		{
			this.nextFireTick = -1;
		}
	}

	public double getProgress()
	{
		return (double) (this.nextFireTick - client.getTickCount()) / BEAM_FIRE_RATE_TICKS;
	}

}
