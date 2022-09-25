package com.duckblade.osrs.toa.features.scabaras.overlay;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.scabaras.ScabarasHelperMode;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayManager;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ScabarasOverlayManager implements PluginLifecycleComponent
{

	private final EventBus eventBus;
	private final OverlayManager overlayManager;
	private final ScabarasOverlay scabarasOverlay;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState currentState)
	{
		return config.scabarasHelperMode() == ScabarasHelperMode.OVERLAY &&
			currentState.getCurrentRoom() == RaidRoom.SCABARAS;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		installOverlay();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		removeOverlay();
	}

	private void installOverlay()
	{
		overlayManager.add(scabarasOverlay);
	}

	private void removeOverlay()
	{
		overlayManager.remove(scabarasOverlay);
	}
}
