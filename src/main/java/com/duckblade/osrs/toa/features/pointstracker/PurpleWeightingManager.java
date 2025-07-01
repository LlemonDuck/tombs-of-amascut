package com.duckblade.osrs.toa.features.pointstracker;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PurpleWeightingManager implements PluginLifecycleComponent
{

	private static final Map<Purple, Integer> BASE_WEIGHTS = new EnumMap<>(Map.of(
		Purple.OSMUMTENS_FANG, 70,
		Purple.LIGHTBEARER, 70,
		Purple.ELIDINIS_WARD, 30,
		Purple.MASORI_MASK, 20,
		Purple.MASORI_BODY, 20,
		Purple.MASORI_CHAPS, 20,
		Purple.TUMEKENS_SHADOW, 10
	));

	public static class PurpleWeightChanged
	{
	}

	@Value
	static class PurpleWeighting
	{
		int weight;
		double purplePercent;
		double pointsAdjustedPercent;
	}

	private final EventBus eventBus;
	private final Client client;
	private final RaidStateTracker raidStateTracker;
	private final PointsTracker pointsTracker;

	private final Map<Purple, Integer> weights = new EnumMap<>(BASE_WEIGHTS);

	private int raidLevel = 0;
	private int lastWeightedRaidLevel = 0;
	private int weightSum = 0;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		// always on so we can detect raid level from party board, even if the overlays are currently off
		return true;
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
	public void onGameTick(GameTick e)
	{
		lastWeightedRaidLevel = -1;
		updateRaidLevel();
		reweight();
	}

	PurpleWeighting getWeighting(Purple purple)
	{
		int weight = weights.get(purple);
		double rate = (double) weight / weightSum;
		double ptsRate = rate * pointsTracker.getUniqueChance();

		return new PurpleWeighting(weight, rate, ptsRate);
	}

	private void updateRaidLevel()
	{
		int raidLevel = -1;
		if (raidStateTracker.getCurrentState().isInRaid())
		{
			raidLevel = client.getVarbitValue(VarbitID.TOA_CLIENT_RAID_LEVEL);
		}
		else
		{
			Widget partyBoardWidget = client.getWidget(InterfaceID.ToaPartydetails.RAID_LEVEL);
			if (partyBoardWidget != null)
			{
				raidLevel =
					Integer.parseInt(
						partyBoardWidget.getText()
							.split(":")[1]
							.split("<")[0]
							.strip()
					);
			}
		}

		if (raidLevel >= 0 && this.raidLevel != (this.raidLevel = raidLevel))
		{
			log.debug("New raid level: {}", this.raidLevel);
		}
	}

	private void reweight()
	{
		if (this.lastWeightedRaidLevel == this.raidLevel)
		{
			return;
		}

		if (this.raidLevel >= 500)
		{
			weights.put(Purple.LIGHTBEARER, BASE_WEIGHTS.get(Purple.LIGHTBEARER) - 35);
			weights.put(Purple.OSMUMTENS_FANG, BASE_WEIGHTS.get(Purple.OSMUMTENS_FANG) - 40);
		}
		else if (this.raidLevel >= 450)
		{
			weights.put(Purple.LIGHTBEARER, BASE_WEIGHTS.get(Purple.LIGHTBEARER) - 30);
			weights.put(Purple.OSMUMTENS_FANG, BASE_WEIGHTS.get(Purple.OSMUMTENS_FANG) - 30);
		}
		else if (this.raidLevel >= 400)
		{
			weights.put(Purple.LIGHTBEARER, BASE_WEIGHTS.get(Purple.LIGHTBEARER) - 20);
			weights.put(Purple.OSMUMTENS_FANG, BASE_WEIGHTS.get(Purple.OSMUMTENS_FANG) - 30);
		}
		else if (this.raidLevel >= 350)
		{
			weights.put(Purple.LIGHTBEARER, BASE_WEIGHTS.get(Purple.LIGHTBEARER) - 10);
			weights.put(Purple.OSMUMTENS_FANG, BASE_WEIGHTS.get(Purple.OSMUMTENS_FANG) - 10);
		}
		else if (this.raidLevel >= 150)
		{
			weights.put(Purple.LIGHTBEARER, BASE_WEIGHTS.get(Purple.LIGHTBEARER));
			weights.put(Purple.OSMUMTENS_FANG, BASE_WEIGHTS.get(Purple.OSMUMTENS_FANG));
		}
		else if (this.raidLevel >= 50)
		{
			// between 50-150, ward+masori+shadow are reduced with an additional 1/50 roll
			// simulate reducing everything else by 50x by instead increasing these two by 50x
			weights.put(Purple.LIGHTBEARER, BASE_WEIGHTS.get(Purple.LIGHTBEARER) * 50);
			weights.put(Purple.OSMUMTENS_FANG, BASE_WEIGHTS.get(Purple.OSMUMTENS_FANG) * 50);
		}
		else
		{
			// below 50 everything is reduced with additional 1/50, so the proportions are normal again
			weights.put(Purple.LIGHTBEARER, BASE_WEIGHTS.get(Purple.LIGHTBEARER));
			weights.put(Purple.OSMUMTENS_FANG, BASE_WEIGHTS.get(Purple.OSMUMTENS_FANG));
		}

		this.lastWeightedRaidLevel = this.raidLevel;
		this.weightSum = weights.values()
			.stream()
			.reduce(0, Integer::sum);

		eventBus.post(new PurpleWeightChanged());
	}
}
