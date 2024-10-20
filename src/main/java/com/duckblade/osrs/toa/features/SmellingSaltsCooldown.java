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
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
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
	private int lastSaltVarb;
	private boolean clickConsumeQueued;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.isInRaid()
			&& config.smellingSaltsCooldown() > 0;
	}

	@Override
	public void startUp()
	{
		clickConsumeQueued = false;
		lastSalt = 0;
		eventBus.register(this);
		lastSaltVarb = client.getVarbitValue(Varbits.BUFF_STAT_BOOST);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged e)
	{
		if (e.getVarbitId() == Varbits.BUFF_STAT_BOOST)
		{
			if (e.getValue() > lastSaltVarb)
			{
				log.debug("Detected salt consumption");
				lastSalt = e.getValue();
				lastSalt = System.currentTimeMillis();
			}
			lastSaltVarb = e.getValue();
		}
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		if (clickConsumeQueued)
		{
			client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"You are already boosted by smelling salts!",
				null
			);
			clickConsumeQueued = false;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e)
	{
		if (e.getMenuTarget().contains("Smelling salts")
			&& e.getMenuOption().equals("Crush"))
		{
			long now = System.currentTimeMillis();
			if (now - lastSalt < (config.smellingSaltsCooldown() * 1000L))
			{
				// delay the blocking chat message until the next game tick to feel a bit more authentic (and less spammy)
				clickConsumeQueued = true;
				e.consume();
			}
		}
	}
}
