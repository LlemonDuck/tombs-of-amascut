package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CameraShakeDisabler implements PluginLifecycleComponent
{

	private final Client client;

	private boolean wasDisabled;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.disableCameraShake() && raidState.getCurrentRoom() == RaidRoom.WARDENS;
	}

	@Override
	public void startUp()
	{
		wasDisabled = client.isCameraShakeDisabled();
		client.setCameraShakeDisabled(true);
	}

	@Override
	public void shutDown()
	{
		if (wasDisabled)
		{
			client.setCameraShakeDisabled(false);
		}
	}
}
