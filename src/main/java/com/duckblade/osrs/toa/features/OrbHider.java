package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class OrbHider implements PluginLifecycleComponent
{

	private static final int BUILD_ORBS_WIDGET_SCRIPT_ID = 6579;
	private static final int WIDGET_ID_ORBS = WidgetInfo.PACK(481, 4);

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.hideHpOrbs() && raidState.isInRaid();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		hideOrbs();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
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
		}
	}
}
