package com.duckblade.osrs.toa.features.pointstracker;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.TombsOfAmascutPlugin;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateChanged;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PointsTrackerTest
{

	@Getter
	static class TestSubscriber
	{
		private PluginMessage captured;

		@Subscribe
		public void onPluginMessage(PluginMessage evt)
		{
			this.captured = evt;
		}
	}

	private EventBus eventBus;
	private TestSubscriber subscriber;

	@Mock
	Client client;

	@Mock
	TombsOfAmascutConfig config;

	@Mock
	PartyPointsTracker partyPointsTracker;

	@Mock
	RaidStateTracker raidStateTracker;

	PointsTracker pointsTracker;

	@BeforeEach
	void setUp()
	{
		subscriber = new TestSubscriber();
		eventBus = new EventBus();
		eventBus.register(subscriber);

		pointsTracker = new PointsTracker(
			eventBus,
			client,
			config,
			partyPointsTracker,
			raidStateTracker
		);
		pointsTracker.startUp();
	}

	@Test
	void testPointsEvent()
	{
		when(partyPointsTracker.isInParty()).thenReturn(true);
		when(partyPointsTracker.getTotalPartyPoints()).thenReturn(600);
		when(config.pointsTrackerAllowExternal()).thenReturn(true);

		RaidStateChanged raidStartEvent = new RaidStateChanged(new RaidState(true, false, null, -1), new RaidState(false, true, RaidRoom.BABA, 1));
		pointsTracker.onRaidStateChanged(raidStartEvent);

		TileItem bigBanana = mock(TileItem.class);
		when(bigBanana.getId()).thenReturn(ItemID.BIG_BANANA);
		ItemSpawned earnPointsEvent = new ItemSpawned(mock(Tile.class), bigBanana);
		pointsTracker.onItemSpawned(earnPointsEvent);

		RaidStateChanged raidEndEvent = new RaidStateChanged(new RaidState(false, true, RaidRoom.WARDENS, 1), new RaidState(false, true, RaidRoom.TOMB, 1));
		pointsTracker.onRaidStateChanged(raidEndEvent);

		PluginMessage pointsEvent = subscriber.getCaptured();
		assertNotNull(pointsEvent);
		assertEquals(TombsOfAmascutPlugin.EVENT_NAMESPACE, pointsEvent.getNamespace());
		assertEquals("raidCompletedPoints", pointsEvent.getName());
		assertEquals(1, pointsEvent.getData().get("version"));
		assertEquals(600, pointsEvent.getData().get("totalPoints"));
		assertEquals(300, pointsEvent.getData().get("personalPoints"));
	}

}
