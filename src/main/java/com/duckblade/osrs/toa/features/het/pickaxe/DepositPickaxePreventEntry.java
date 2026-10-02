package com.duckblade.osrs.toa.features.het.pickaxe;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.MenuEntryUtil;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DepositPickaxePreventEntry
	implements PluginLifecycleComponent
{

	private final Client client;
	private final RaidStateTracker raidStateTracker;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState currentState)
	{
		return (currentState.getCurrentRoom() == RaidRoom.HET && config.hetPickaxePreventExit()) ||
			(currentState.isInLobby() && config.hetPickaxePreventRaidStart());
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		// don't lift out isEntry, it's more expensive than room checks
		if (raidStateTracker.getCurrentState().isInLobby())
		{
			if (!PickaxeUtil.pickaxeIsInStorage(client) && MenuEntryUtil.isEntry(e.getMenuEntry()))
			{
				e.getMenuEntry().setDeprioritized(true);
			}
		}
		else if (raidStateTracker.getCurrentState().getCurrentRoom() == RaidRoom.HET)
		{
			if (MenuEntryUtil.isEntry(e.getMenuEntry()) && PickaxeUtil.hasPickaxe(client))
			{
				e.getMenuEntry().setDeprioritized(true);
			}
		}
	}

}
