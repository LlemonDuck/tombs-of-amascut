package com.duckblade.osrs.toa.module;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;

public interface PluginLifecycleComponent
{

	default boolean isConfigEnabled(TombsOfAmascutConfig config)
	{
		return true;
	}

	void startUp();

	void shutDown();

}
