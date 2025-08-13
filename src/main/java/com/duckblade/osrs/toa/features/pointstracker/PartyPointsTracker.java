package com.duckblade.osrs.toa.features.pointstracker;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.events.UserJoin;
import net.runelite.client.party.events.UserPart;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PartyPointsTracker implements PluginLifecycleComponent
{

	private final WSClient wsClient;
	private final EventBus eventBus;
	private final PartyService partyService;
	private final ScheduledExecutorService partySendEs;

	private final Map<Long, Integer> partyPoints = new HashMap<>();

	private final AtomicInteger partySequenceId = new AtomicInteger(0);
	private long partyDontSendUntil = -1;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		// always track even if not displaying, so that party members get points totals
		return raidState.isInRaid();
	}

	@Override
	public void startUp()
	{
		wsClient.registerMessage(PointsMessage.class);
		eventBus.register(this);
		this.clearPartyPointsMap();
	}

	@Override
	public void shutDown()
	{
		wsClient.unregisterMessage(PointsMessage.class);
		eventBus.unregister(this);
	}

	@Subscribe
	public void onPointsMessage(PointsMessage e)
	{
		partyPoints.put(e.getMemberId(), e.getPoints());
	}

	@Subscribe
	public void onUserJoin(UserJoin e)
	{
		partyPoints.remove(e.getMemberId());
	}

	@Subscribe
	public void onUserPart(UserPart e)
	{
		partyPoints.remove(e.getMemberId());
	}

	@Subscribe
	public void onPartyChanged(PartyChanged e)
	{
		clearPartyPointsMap();
	}

	public void schedulePointsUpdate(int points)
	{
		if (!isInParty())
		{
			return;
		}

		// queue up a points update at the next available deadline (30s after last message)
		final int thisReqId = this.partySequenceId.incrementAndGet();
		partySendEs.schedule(() ->
		{
			// make sure we're still the most recent message
			if (thisReqId != this.partySequenceId.get())
			{
				return;
			}

			sendPointsUpdate(points);
		}, Math.max(0, partyDontSendUntil - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
	}

	public void sendPointsUpdate(int points)
	{
		if (!isInParty())
		{
			return;
		}

		PointsMessage message = new PointsMessage(points);
		partyService.send(message);
		partyDontSendUntil = System.currentTimeMillis() + (30 * 1000);
	}

	public void clearPartyPointsMap()
	{
		partyPoints.clear();
	}

	public int getTotalPartyPoints()
	{
		return partyPoints.values()
			.stream()
			.mapToInt(Integer::intValue)
			.sum();
	}

	public boolean isInParty()
	{
		return partyService.isInParty();
	}
}
