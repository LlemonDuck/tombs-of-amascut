package com.duckblade.osrs.toa.features.timers.splits;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.client.eventbus.EventBus;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SplitsManager implements PluginLifecycleComponent
{

	private final EventBus eventBus;

	private final Map<RaidRoom, Duration> splits = new LinkedHashMap<>(); // LHM keeps insertion order for iteration

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.splitsDisplayMode() != SplitsDisplayMode.OFF &&
			raidState.isInRaid();
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
}
