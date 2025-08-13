package com.duckblade.osrs.toa.features.scabaras.overlay;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.scabaras.ScabarasHelperMode;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ObeliskPuzzleSolver implements PluginLifecycleComponent
{

	private static final int OBELISK_ID_INACTIVE = 11698;
	private static final int OBELISK_ID_ACTIVE = 11699;

	private final EventBus eventBus;

	@Getter
	private int activeObelisks = 0;

	@Getter
	private final List<LocalPoint> obeliskOrder = new ArrayList<>(6);

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.scabarasHelperMode() == ScabarasHelperMode.OVERLAY &&
			raidState.getCurrentRoom() == RaidRoom.SCABARAS;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		activeObelisks = 0;
		obeliskOrder.clear();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onNpcChanged(NpcChanged e)
	{
		if (e.getNpc().getId() == OBELISK_ID_ACTIVE)
		{
			LocalPoint obeliskTile = e.getNpc().getLocalLocation();
			if (!obeliskOrder.contains(obeliskTile))
			{
				obeliskOrder.add(obeliskTile);
			}

			activeObelisks++;
		}
		else if (e.getNpc().getId() == OBELISK_ID_INACTIVE)
		{
			activeObelisks = 0;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getMessage().startsWith("Your party failed to complete the challenge"))
		{
			activeObelisks = 0;
			obeliskOrder.clear();
		}
	}
}
