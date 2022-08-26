package com.duckblade.osrs.toa;

import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import java.awt.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

// to be used in more performant sections to avoid hitting slower config manager impl
// only includes definitions for values actually used
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class TombsOfAmascutConfigCache implements TombsOfAmascutConfig, PluginLifecycleComponent
{

	private final EventBus eventBus;
	private final TombsOfAmascutConfig configManagerProxy;

	private Color invocationPresetsEnableColor;
	private Color invocationPresetsDisableColor;

	@Override
	public void startUp()
	{
		eventBus.register(this);
		loadConfig();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals(TombsOfAmascutConfig.CONFIG_GROUP))
		{
			loadConfig();
		}
	}

	private void loadConfig()
	{
		this.invocationPresetsEnableColor = configManagerProxy.invocationPresetsEnableColor();
		this.invocationPresetsDisableColor = configManagerProxy.invocationPresetsDisableColor();
	}

	@Override
	public Color invocationPresetsEnableColor()
	{
		return this.invocationPresetsEnableColor;
	}

	@Override
	public Color invocationPresetsDisableColor()
	{
		return this.invocationPresetsDisableColor;
	}
}
