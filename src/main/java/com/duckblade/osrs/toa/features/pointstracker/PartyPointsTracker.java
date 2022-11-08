package com.duckblade.osrs.toa.features.pointstracker;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.events.UserJoin;
import net.runelite.client.party.events.UserPart;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PartyPointsTracker implements PluginLifecycleComponent
{

	private final WSClient wsClient;
	private final EventBus eventBus;
	private final PartyService partyService;

	private final Map<Long, Integer> partyPoints = new HashMap<>();

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

	public void sendPointsUpdate(int points)
	{
		if (!isInParty())
		{
			return;
		}

		PointsMessage message = new PointsMessage(points);
		partyService.send(message);
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
