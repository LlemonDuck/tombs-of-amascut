package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SmellingSaltsCooldown implements PluginLifecycleComponent
{

	private final EventBus eventBus;
	private final Client client;
	private final TombsOfAmascutConfig config;

	private long lastSalt;
	private boolean clickQueued;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.smellingSaltsCooldown() > 0;
	}

	@Override
	public void startUp()
	{
		clickQueued = false;
		lastSalt = 0;
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		if (clickQueued)
		{
			client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"You are already boosted by smelling salts!",
				null
			);
			clickQueued = false;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e)
	{
		if (e.getMenuTarget().contains("Smelling salts")
			&& e.getMenuOption().equals("Crush"))
		{
			long now = System.currentTimeMillis();
			if (now - lastSalt > (config.smellingSaltsCooldown() * 1000L))
			{
				lastSalt = now;
				return;
			}

			// delay the blocking chat message until the next game tick to feel a bit more authentic (and less spammy)
			clickQueued = true;
			e.consume();
		}
	}
}
