package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class FadeDisabler implements PluginLifecycleComponent
{
	@Inject
	private EventBus eventBus;

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return config.hideFadeTransition() && (raidState.isInLobby() || raidState.isInRaid());
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
	private void onScriptPreFired(final ScriptPreFired event)
	{
		// https://github.com/RuneStar/cs2-scripts/blob/master/scripts/%5Bclientscript%2Cfade_overlay%5D.cs2
		if (event.getScriptId() == 948)
		{
			event.getScriptEvent().getArguments()[4] = 255; // transparency (default=0)
			event.getScriptEvent().getArguments()[5] = 0; // duration? (default=50)
		}
	}
}
