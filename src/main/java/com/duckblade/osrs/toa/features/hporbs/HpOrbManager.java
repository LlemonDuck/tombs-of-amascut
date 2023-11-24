package com.duckblade.osrs.toa.features.hporbs;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;

@Singleton
public class HpOrbManager implements PluginLifecycleComponent
{

	private static final int BUILD_ORBS_WIDGET_SCRIPT_ID = 6579;
	private static final int WIDGET_ID_ORBS = WidgetUtil.packComponentId(481, 4);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HealthBarsOverlay healthBarsOverlay;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.hpOrbsMode() != HpOrbMode.ORBS && raidState.isInRaid();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		clientThread.invokeLater(this::hideOrbs);
		overlayManager.add(healthBarsOverlay);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		overlayManager.removeIf(o -> o instanceof HealthBarsOverlay);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() == BUILD_ORBS_WIDGET_SCRIPT_ID)
		{
			hideOrbs();
		}
	}

	private void hideOrbs()
	{
		Widget orbW = client.getWidget(WIDGET_ID_ORBS);
		if (orbW != null)
		{
			orbW.setHidden(true);

			orbW.getParent().setOriginalHeight(95);
			orbW.getParent().revalidate();
		}
	}
}
