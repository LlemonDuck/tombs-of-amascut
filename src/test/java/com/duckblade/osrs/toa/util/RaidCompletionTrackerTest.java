package com.duckblade.osrs.toa.util;

import java.util.Collections;
import java.util.Set;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RaidCompletionTrackerTest
{

	@InjectMocks
	private RaidCompletionTracker raidCompletionTracker;

	@Test
	void tracksCompletedBosses()
	{
		assertEquals(raidCompletionTracker.getCompletedBosses(), Set.of());

		raidCompletionTracker.onChatMessage(message("Challenge complete: Path of Crondis. Duration: 12"));
		raidCompletionTracker.onChatMessage(message("Challenge complete: Zebak. Duration: 12"));
		assertEquals(raidCompletionTracker.getCompletedBosses(), Set.of("Path of Crondis", "Zebak"));
	}

	@Test
	void resetsWhenNotInRaid()
	{
		assertEquals(raidCompletionTracker.getCompletedBosses(), Collections.emptySet());

		raidCompletionTracker.onChatMessage(message("Challenge complete: Path of Crondis. Duration: 12"));
		raidCompletionTracker.onChatMessage(message("Challenge complete: Zebak. Duration: 12"));
		raidCompletionTracker.onRaidStateChanged(new RaidStateChanged(null, new RaidState(true, false, null, 1)));
		assertEquals(raidCompletionTracker.getCompletedBosses(), Set.of());

		raidCompletionTracker.onChatMessage(message("Challenge complete: Path of Het. Duration: 12"));
		assertEquals(raidCompletionTracker.getCompletedBosses(), Set.of("Path of Het"));
	}

	private static ChatMessage message(String content)
	{
		return new ChatMessage(
			null,
			ChatMessageType.GAMEMESSAGE,
			"",
			content,
			"",
			123
		);
	}


}