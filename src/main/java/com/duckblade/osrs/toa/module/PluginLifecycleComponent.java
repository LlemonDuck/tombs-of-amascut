package com.duckblade.osrs.toa.module;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.util.RaidState;

public interface PluginLifecycleComponent
{

	default boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return true;
	}

	void startUp();

	void shutDown();

}
