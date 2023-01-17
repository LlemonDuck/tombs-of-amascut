package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.MenuEntry;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.events.MenuEntryAdded;
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
		NpcID.OSMUMTEN, // post-demi-boss
		NpcID.OSMUMTEN_11690, // pre-warden
		NpcID.OSMUMTEN_11693 // loot room
	);

	private static final Set<Integer> OBJECT_IDS = ImmutableSet.of(
		ObjectID.PATH_OF_CRONDIS,
		ObjectID.PATH_OF_SCABARAS,
		ObjectID.PATH_OF_HET,
		ObjectID.PATH_OF_APMEKEN,
		ObjectID.BARRIER_45135,
		ObjectID.TELEPORT_CRYSTAL_45505, // kephri
		ObjectID.TELEPORT_CRYSTAL_45506, // zebak
		ObjectID.TELEPORT_CRYSTAL_45866, // akkha
		ObjectID.TELEPORT_CRYSTAL_45754, // ba-ba // Quick-Use
		ObjectID.ENTRY_45131, // het
		ObjectID.ENTRY_45337, // scabaras
		ObjectID.ENTRY_45397, // crondis
		ObjectID.ENTRY_45500, // apmeken
		ObjectID.ENTRY_46168 // wardens
	);

	private final EventBus eventBus;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		switch (config.quickProceedEnableMode())
		{
			case ALL:
				return raidState.isInRaid();

			case NOT_CRONDIS:
				return raidState.isInRaid() && raidState.getCurrentRoom() != RaidRoom.CRONDIS;

			default:
				return false;
		}
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
				return (me.getOption().equals("Enter") || me.getOption().equals("Use") || me.getOption().equals("Pass")) &&
					OBJECT_IDS.contains(me.getIdentifier());

			default:
				return false;
		}
	}
}
