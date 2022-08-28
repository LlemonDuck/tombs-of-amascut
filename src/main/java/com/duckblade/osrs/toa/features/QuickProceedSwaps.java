package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NpcID;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class QuickProceedSwaps implements PluginLifecycleComponent
{

	private static final Set<Integer> OSMUMTEN_IDS = ImmutableSet.of(
		NpcID.OSMUMTEN, // post-demi-boss
		NpcID.OSMUMTEN_11690, // pre-warden
		NpcID.OSMUMTEN_11693 // loot room
	);

	private final EventBus eventBus;
	private final Client client;

	@Override
	public boolean isConfigEnabled(TombsOfAmascutConfig config)
	{
		return config.leftClickProceedEnable();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		// easier to just deprioritize talk-to rather than prioritizing the other options
		MenuEntry me = e.getMenuEntry();

		if (me.getType() != MenuAction.NPC_FIRST_OPTION || !me.getOption().equals("Talk-to"))
		{
			return;
		}

		int npcId = client.getCachedNPCs()[me.getIdentifier()].getId();
		if (OSMUMTEN_IDS.contains(npcId))
		{
			e.getMenuEntry().setDeprioritized(true);
		}
	}
}
