package com.duckblade.osrs.toa.util;

import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
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

	private RaidState currentState = new RaidState(false, false, null);

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
		boolean inRaid = currentRoom != null || (w != null && !w.isHidden());

		RaidState previousState = this.currentState;
		RaidState newState = new RaidState(inLobby, inRaid, currentRoom);
		if (!previousState.equals(newState))
		{
			this.currentState = newState;
			eventBus.post(new RaidStateChanged(previousState, newState));
		}
	}

	public boolean isInLobby()
	{
		return this.currentState.isInLobby();
	}

	public boolean isInRaid()
	{
		return this.currentState.isInRaid();
	}

	public RaidRoom getCurrentRoom()
	{
		return this.currentState.getCurrentRoom();
	}

	public RaidState getCurrentState()
	{
		return this.currentState;
	}
}
