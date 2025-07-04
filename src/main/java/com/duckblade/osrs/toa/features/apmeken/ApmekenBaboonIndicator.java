package com.duckblade.osrs.toa.features.apmeken;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ApmekenBaboonIndicator implements PluginLifecycleComponent
{
	private final EventBus eventBus;

	@Getter(AccessLevel.PACKAGE)
	private final List<NPC> volatileBaboons = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private final List<NPC> baboons = new ArrayList<>();

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return raidState.getCurrentRoom() == RaidRoom.APMEKEN;
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
		volatileBaboons.clear();
		baboons.clear();
	}

	@Subscribe
	public void onNpcSpawned(final NpcSpawned event)
	{
		final NPC npc = event.getNpc();

		final int id = npc.getId();

		switch (id)
		{
			case NpcID.TOA_PATH_APMEKEN_BABOON_ZOMBIE:
				volatileBaboons.add(npc);
			case NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_1:
			case NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_2:
			case NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_1:
			case NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_2:
			case NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_1:
			case NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_2:
			case NpcID.TOA_PATH_APMEKEN_BABOON_SHAMAN:
			case NpcID.TOA_PATH_APMEKEN_BABOON_CURSED:
			case NpcID.TOA_PATH_APMEKEN_BABOON_THRALL:
				baboons.add(npc);
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onNpcDespawned(final NpcDespawned event)
	{
		removeNpc(event.getNpc());
	}

	@Subscribe
	public void onActorDeath(final ActorDeath event)
	{
		final Actor actor = event.getActor();

		if (!(actor instanceof NPC))
		{
			return;
		}

		removeNpc((NPC) actor);
	}

	private void removeNpc(final NPC npc)
	{
		final int id = npc.getId();

		switch (id)
		{
			case NpcID.TOA_PATH_APMEKEN_BABOON_ZOMBIE:
				volatileBaboons.remove(npc);
			case NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_1:
			case NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_2:
			case NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_1:
			case NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_2:
			case NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_1:
			case NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_2:
			case NpcID.TOA_PATH_APMEKEN_BABOON_SHAMAN:
			case NpcID.TOA_PATH_APMEKEN_BABOON_CURSED:
			case NpcID.TOA_PATH_APMEKEN_BABOON_THRALL:
				baboons.remove(npc);
				break;
			default:
				break;
		}
	}

}
