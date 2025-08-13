package com.duckblade.osrs.toa.features.boss.akkha;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.PathLevelTracker;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.NpcUtil;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AkkhaShadowHealth implements PluginLifecycleComponent
{
	private static final int BASE_HP_AKKHAS_SHADOW = 70;

	private final EventBus eventBus;
	private final Client client;
	private final PathLevelTracker pathLevelTracker;
	private final RaidStateTracker raidStateTracker;

	@Getter(AccessLevel.PACKAGE)
	private final Map<NPC, Integer> akkhasShadows = new HashMap<>();

	@Getter(AccessLevel.PACKAGE)
	private int akkhasShadowMaxHp;

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return raidState.getCurrentRoom() == RaidRoom.AKKHA;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		updateAkkhasShadowMaxHp();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		reset();
	}

	private void reset()
	{
		akkhasShadows.clear();
		akkhasShadowMaxHp = 0;
	}

	@Subscribe
	private void onGameTick(final GameTick event)
	{
		akkhasShadows.entrySet().forEach(e ->
		{
			NPC npc = e.getKey();
			if (npc.getModel() != null && npc.getModel().getOverrideAmount() != 0)
			{
				// inactive state uses model overrides to "white-out" the npc
				e.setValue(-1);
				return;
			}

			final int trueHp = NpcUtil.calculateActorHp(e.getKey(), akkhasShadowMaxHp);
			if (trueHp != -1)
			{
				e.setValue(trueHp);
			}
		});
	}

	@Subscribe
	private void onNpcSpawned(final NpcSpawned event)
	{
		final NPC npc = event.getNpc();

		if (npc.getId() == NpcID.AKKHA_SHADOW)
		{
			akkhasShadows.put(npc, akkhasShadowMaxHp);
		}
	}

	@Subscribe
	private void onNpcDespawned(final NpcDespawned event)
	{
		final NPC npc = event.getNpc();
		akkhasShadows.remove(npc);
	}

	private void updateAkkhasShadowMaxHp()
	{
		// Calculate max hp manually as NPCManager does not have this information
		int hp = BASE_HP_AKKHAS_SHADOW;

		final int raidLevelFactor = 4 * client.getVarbitValue(VarbitID.TOA_CLIENT_RAID_LEVEL) / 10;
		hp += hp * raidLevelFactor / 100;

		final int pathLevel = pathLevelTracker.getAkkhaPathLevel();
		if (pathLevel > 0)
		{
			// first level is 8%, others are 5%
			final int pathLevelFactor = 3 + 5 * pathLevel;
			hp += hp * pathLevelFactor / 100;
		}

		final int partySize = raidStateTracker.getPlayerCount();
		if (partySize >= 2)
		{
			int partyFactor = 9 * (partySize == 3 ? 2 : 1);
			if (partySize >= 4)
			{
				partyFactor += 6 * (partySize - 3);
			}
			hp += hp * partyFactor / 10;
		}

		// rounding
		if (hp > 100)
		{
			final int roundTo = hp > 300 ? 10 : 5;
			hp = ((hp + (roundTo / 2)) / roundTo) * roundTo;
		}

		akkhasShadowMaxHp = hp;
	}
}
