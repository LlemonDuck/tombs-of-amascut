package com.duckblade.osrs.toa.util;

import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RaidCompletionTracker implements PluginLifecycleComponent
{

	private final EventBus eventBus;

	private final Set<String> completedBosses = new HashSet<>();

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

	@Subscribe(priority = Float.MAX_VALUE)
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		if (e.getMessage().startsWith("Challenge complete: "))
		{
			String bossName = e.getMessage()
				.substring("Challenge complete: ".length(), e.getMessage().indexOf('.'));
			completedBosses.add(bossName);
		}
	}

	@Subscribe
	public void onRaidStateChanged(RaidStateChanged e)
	{
		if (!e.getNewState().isInRaid())
		{
			completedBosses.clear();
		}
	}

	public Set<String> getCompletedBosses()
	{
		return Collections.unmodifiableSet(completedBosses);
	}

}
