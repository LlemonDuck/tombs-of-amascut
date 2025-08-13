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
public class AdrenalineCooldown implements PluginLifecycleComponent
{

	private final EventBus eventBus;
	private final Client client;
	private final TombsOfAmascutConfig config;

	private long lastAdrenaline;
	private int lastAdrenalineVarb;
	private boolean clickConsumeQueued;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.isInRaid()
			&& config.adrenalineCooldown() > 0;
	}

	@Override
	public void startUp()
	{
		clickConsumeQueued = false;
		lastAdrenaline = 0;
		eventBus.register(this);
		lastAdrenalineVarb = client.getVarbitValue(Varbits.BUFF_STAT_BOOST);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged e)
	{
		if (e.getVarbitId() == Varbits.LIQUID_ADERNALINE_ACTIVE)
		{
			if (e.getValue() > lastAdrenalineVarb)
			{
				log.debug("Detected adrenaline consumption");
				lastAdrenaline = e.getValue();
				lastAdrenaline = System.currentTimeMillis();
			}
			lastAdrenalineVarb = e.getValue();
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
				"You are already boosted by liquid adrenaline!",
				null
			);
			clickConsumeQueued = false;
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e)
	{
		if (e.getMenuTarget().contains("Liquid adrenaline")
			&& e.getMenuOption().equals("Drink"))
		{
			long now = System.currentTimeMillis();
			if (now - lastAdrenaline < (config.adrenalineCooldown() * 1000L))
			{
				// delay the blocking chat message until the next game tick to feel a bit more authentic (and less spammy)
				clickConsumeQueued = true;
				e.consume();
			}
		}
	}
}
