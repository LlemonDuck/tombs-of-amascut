package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class LeftClickBankAll implements PluginLifecycleComponent
{

	private static final String MENU_ENTRY_OPTION = "Bank-all";

	@Inject
	private EventBus eventBus;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.leftClickBankAll() &&
			(raidState.isInLobby() || raidState.getCurrentRoom() == RaidRoom.TOMB);
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
		if (MENU_ENTRY_OPTION.equals(e.getOption()))
		{
			e.getMenuEntry().setForceLeftClick(true);
		}
	}
}
