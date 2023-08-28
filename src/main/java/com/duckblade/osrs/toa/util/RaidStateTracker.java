package com.duckblade.osrs.toa.util;

import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RaidStateTracker implements PluginLifecycleComponent
{

	private static final int REGION_LOBBY = 13454;
	private static final int WIDGET_PARENT_ID = 481;
	private static final int WIDGET_CHILD_ID = 40;

	private final Client client;
	private final EventBus eventBus;

	@Getter
	private RaidState currentState = new RaidState(false, false, null, 0);

	// delay inRaid = false by 3 ticks to alleviate any unexpected delays between rooms
	private int raidLeaveTicks = 0;

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

	@Subscribe(priority = 5)
	public void onGameTick(GameTick e)
	{
		LocalPoint lp = client.getLocalPlayer().getLocalLocation();
		int region = lp == null ? -1 : WorldPoint.fromLocalInstance(client, lp).getRegionID();

		Widget w = client.getWidget(WIDGET_PARENT_ID, WIDGET_CHILD_ID);

		boolean inLobby = region == REGION_LOBBY;
		RaidRoom currentRoom = RaidRoom.forRegionId(region);
		boolean inRaidRaw = currentRoom != null || (w != null && !w.isHidden());

		raidLeaveTicks = inRaidRaw ? 3 : raidLeaveTicks - 1;
		boolean inRaid = raidLeaveTicks > 0;

		RaidState previousState = this.currentState;
		RaidState newState = new RaidState(inLobby, inRaid, currentRoom, countPlayers());
		if (!previousState.equals(newState))
		{
			this.currentState = newState;
			eventBus.post(new RaidStateChanged(previousState, newState));
		}
	}

	public int getPlayerCount()
	{
		return this.currentState.getPlayerCount();
	}

	private int countPlayers()
	{
		return 1 +
			(client.getVarbitValue(Varbits.TOA_MEMBER_1_HEALTH) != 0 ? 1 : 0) +
			(client.getVarbitValue(Varbits.TOA_MEMBER_2_HEALTH) != 0 ? 1 : 0) +
			(client.getVarbitValue(Varbits.TOA_MEMBER_3_HEALTH) != 0 ? 1 : 0) +
			(client.getVarbitValue(Varbits.TOA_MEMBER_4_HEALTH) != 0 ? 1 : 0) +
			(client.getVarbitValue(Varbits.TOA_MEMBER_5_HEALTH) != 0 ? 1 : 0) +
			(client.getVarbitValue(Varbits.TOA_MEMBER_6_HEALTH) != 0 ? 1 : 0) +
			(client.getVarbitValue(Varbits.TOA_MEMBER_7_HEALTH) != 0 ? 1 : 0);
	}
}