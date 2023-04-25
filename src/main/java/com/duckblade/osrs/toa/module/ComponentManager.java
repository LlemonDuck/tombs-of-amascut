package com.duckblade.osrs.toa.module;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateChanged;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.GameEventManager;

/**
 * Manages all the subcomponents of the plugin
 * so they can register themselves to RuneLite resources
 * e.g. EventBus/OverlayManager/init on startup/etc
 * instead of the TombsOfAmascutPlugin class handling everything.
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class ComponentManager
{

	private final EventBus eventBus;
	private final GameEventManager gameEventManager;
	private final TombsOfAmascutConfig config;
	private final RaidStateTracker raidStateTracker;
	private final Set<PluginLifecycleComponent> components;

	private final Map<PluginLifecycleComponent, Boolean> states = new HashMap<>();

	public void onPluginStart()
	{
		eventBus.register(this);
		components.forEach(c -> states.put(c, false));
		revalidateComponentStates();
	}

	public void onPluginStop()
	{
		eventBus.unregister(this);
		components.stream()
			.filter(states::get)
			.forEach(this::tryShutDown);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (!TombsOfAmascutConfig.CONFIG_GROUP.equals(e.getGroup()))
		{
			return;
		}

		revalidateComponentStates();
	}

	@Subscribe
	public void onRaidStateChanged(RaidStateChanged e)
	{
		revalidateComponentStates();
	}

	private void revalidateComponentStates()
	{
		RaidState raidState = raidStateTracker.getCurrentState();
		components.forEach(c ->
		{
			boolean shouldBeEnabled = c.isEnabled(config, raidState);
			boolean isEnabled = states.get(c);
			if (shouldBeEnabled == isEnabled)
			{
				return;
			}

			if (shouldBeEnabled)
			{
				tryStartUp(c);
			}
			else
			{
				tryShutDown(c);
			}
		});
	}

	private void tryStartUp(PluginLifecycleComponent component)
	{
		if (states.get(component))
		{
			return;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Enabling ToA plugin component [{}]", component.getClass().getName());
		}

		try
		{
			component.startUp();
			gameEventManager.simulateGameEvents(component);
			states.put(component, true);
		}
		catch (Exception e)
		{
			log.error("Failed to start ToA plugin component [{}]", component.getClass().getName(), e);
		}
	}

	private void tryShutDown(PluginLifecycleComponent component)
	{
		if (!states.get(component))
		{
			return;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Disabling ToA plugin component [{}]", component.getClass().getName());
		}

		try
		{
			component.shutDown();
		}
		catch (Exception e)
		{
			log.error("Failed to cleanly shut down ToA plugin component [{}]", component.getClass().getName());
		}
		finally
		{
			states.put(component, false);
		}
	}

}
