package com.duckblade.osrs.toa.features.timetracking;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.ColorUtil;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SplitsTracker implements PluginLifecycleComponent
{

	private static final String ROOM_COMPLETE_PREFIX = "Challenge complete";
	private static final Pattern ROOM_COMPLETE_PATTERN =
		Pattern.compile("Challenge complete: (?:Path of )?([A-Za-z-]+).*Total:.*?([0-9]+:[.0-9]+).*");
	private static final Pattern WARDENS_COMPLETE_PATTERN =
		Pattern.compile("Challenge complete: The (Wardens).*?completion time:.*?([0-9]+:[.0-9]+).*");

	private final EventBus eventBus;

	private final TombsOfAmascutConfig config;
	private final Client client;

	private final List<Split> splits = new ArrayList<>(9);

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		// always track, conditionally display
		return raidState.isInRaid();
	}

	@Override
	public void startUp()
	{
		splits.clear();
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String msg = e.getMessage();
		if (!msg.startsWith(ROOM_COMPLETE_PREFIX))
		{
			return;
		}

		Matcher m;
		if (!(m = WARDENS_COMPLETE_PATTERN.matcher(msg)).matches() &&
			!(m = ROOM_COMPLETE_PATTERN.matcher(msg)).matches())
		{
			return;
		}

		RaidRoom room = RaidRoom.forString(m.group(1));
		String split = m.group(2);
		if (room == null)
		{
			log.warn("Failed to find room {} for completion string {}", m.group(1), e.getMessage());
			return;
		}

		splits.add(new Split(room, split));

		if (room == RaidRoom.WARDENS && config.splitsMessage() != SplitsMode.OFF)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", buildSplitsMessages(), "", false);
		}
	}

	public List<Split> getSplits()
	{
		return Collections.unmodifiableList(splits);
	}

	private String buildSplitsMessages()
	{
		SplitsMode splitsMode = config.splitsMessage();

		StringBuilder sb = new StringBuilder();
		sb.append("ToA Splits<br>");
		for (Split s : getSplits())
		{
			if (splitsMode.includesRoom(s.getRoom()))
			{
				sb.append(s.getRoom());
				sb.append(": ");
				sb.append(ColorUtil.wrapWithColorTag(s.getSplit(), Color.red));
				sb.append("<br>");
			}
		}

		return sb.toString();
	}

}
