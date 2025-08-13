package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class QuickProceedSwaps implements PluginLifecycleComponent
{

	public enum QuickProceedEnableMode
	{
		ALL,
		NOT_CRONDIS,
		NONE,
		;
	}

	private static final Set<Integer> NPC_IDS = ImmutableSet.of(
		NpcID.TOA_OSMUMTEN_VIS, // post-demi-boss
		NpcID.TOA_OSMUMTEN_WARDENS_VIS, // pre-warden
		NpcID.TOA_OSMUMTEN_VAULT_VIS_2 // loot room
	);

	private static final Set<Integer> OBJECT_IDS = ImmutableSet.of(
		ObjectID.TOA_NEXUS_CRONDIS_DOOR,
		ObjectID.TOA_NEXUS_SCABARAS_DOOR,
		ObjectID.TOA_NEXUS_HET_DOOR,
		ObjectID.TOA_NEXUS_APMEKEN_DOOR,
		ObjectID.TOA_PATH_BARRIER,
		ObjectID.KEPHRI_TELEPORT, // kephri
		ObjectID.ZEBAK_TELEPORT, // zebak
		ObjectID.AKKHA_TELEPORT, // akkha
		ObjectID.BABA_TELEPORT, // ba-ba // Quick-Use
		ObjectID.TOA_DOOR_CONTINUE, // het
		ObjectID.TOA_SCABARAS_CONTINUE, // scabaras
		ObjectID.TOA_PATH_CRONDIS_CONTINUE, // crondis
		ObjectID.TOA_PATH_APMEKEN_CONTINUE, // apmeken
		ObjectID.TOA_NEXUS_WARDENS_DOOR_OPEN // wardens
	);

	private final EventBus eventBus;
	private final TombsOfAmascutConfig config;
	private final RaidStateTracker raidStateTracker;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.isInRaid() && config.quickProceedEnableMode() != QuickProceedEnableMode.NONE;
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
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		// easier to just deprioritize talk-to rather than prioritizing the other options
		MenuEntry me = e.getMenuEntry();
		if (shouldDeprioritize(me))
		{
			me.setDeprioritized(true);
		}
	}

	private boolean shouldDeprioritize(MenuEntry me)
	{
		switch (me.getType())
		{
			case NPC_FIRST_OPTION:
				return me.getOption().equals("Talk-to") &&
					me.getNpc() != null &&
					NPC_IDS.contains(me.getNpc().getId());

			case GAME_OBJECT_FIRST_OPTION:
				final int id = me.getIdentifier();
				final String option = me.getOption();

				if (id == ObjectID.TOA_PATH_BARRIER &&
					raidStateTracker.getCurrentState().getCurrentRoom() == RaidRoom.CRONDIS &&
					option.equals("Pass"))
				{
					return config.quickProceedEnableMode() != QuickProceedEnableMode.NOT_CRONDIS;
				}

				return OBJECT_IDS.contains(id) &&
					(option.equals("Enter") || option.equals("Use") || option.equals("Pass"));

			default:
				return false;
		}
	}
}