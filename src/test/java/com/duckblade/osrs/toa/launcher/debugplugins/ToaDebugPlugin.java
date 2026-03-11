package com.duckblade.osrs.toa.launcher.debugplugins;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Singleton
@PluginDescriptor(
	name = "ToA Debug"
)
public class ToaDebugPlugin extends Plugin
{

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HetSolverDebugOverlay hetSolverDebugOverlay;

	@Inject
	private AkkhaShadowOverridesOverlay shadowOverridesOverlay;

	@Inject
	private MenuEntryDumper menuEntryDumper;

	@Inject
	private StateOverlay stateOverlay;

	@Inject
	private EventBus eventBus;

	@Override
	protected void startUp()
	{
		overlayManager.add(hetSolverDebugOverlay);
		overlayManager.add(shadowOverridesOverlay);
		overlayManager.add(stateOverlay);
		eventBus.register(shadowOverridesOverlay);
		eventBus.register(menuEntryDumper);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(hetSolverDebugOverlay);
		overlayManager.remove(shadowOverridesOverlay);
		overlayManager.remove(stateOverlay);
		eventBus.unregister(shadowOverridesOverlay);
		eventBus.unregister(menuEntryDumper);
	}

	@Provides
	public ToaDebugConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ToaDebugConfig.class);
	}
}
