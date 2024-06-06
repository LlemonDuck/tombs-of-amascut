package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PathLevelTracker implements PluginLifecycleComponent
{
	private static final int CHILD_ID_DEFAULT = 45;
	private static final int CHILD_ID_KEPHRI = 49;
	private static final int CHILD_ID_AKKHA = 51;
	private static final int CHILD_ID_BABA = 53;
	private static final int CHILD_ID_ZEBAK = 55;

	private final EventBus eventBus;
	private final Client client;
	private final RaidStateTracker raidStateTracker;

	@Getter
	private int kephriPathLevel;
	@Getter
	private int akkhaPathLevel;
	@Getter
	private int babaPathLevel;
	@Getter
	private int zebakPathLevel;

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return raidState.isInRaid();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		readPathLevels();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onWidgetLoaded(final WidgetLoaded event)
	{
		final int groupId = event.getGroupId();

		if (groupId == InterfaceID.TOA_RAID)
		{
			readPathLevels();
		}
	}

	private void readPathLevels()
	{
		final RaidRoom raidRoom = raidStateTracker.getCurrentState().getCurrentRoom();

		Widget widget;

		if (raidRoom == RaidRoom.NEXUS)
		{
			if ((widget = client.getWidget(InterfaceID.TOA_RAID, CHILD_ID_KEPHRI)) != null)
			{
				kephriPathLevel = Integer.parseInt(widget.getText());
			}

			if ((widget = client.getWidget(InterfaceID.TOA_RAID, CHILD_ID_AKKHA)) != null)
			{
				akkhaPathLevel = Integer.parseInt(widget.getText());
			}

			if ((widget = client.getWidget(InterfaceID.TOA_RAID, CHILD_ID_BABA)) != null)
			{
				babaPathLevel = Integer.parseInt(widget.getText());
			}

			if ((widget = client.getWidget(InterfaceID.TOA_RAID, CHILD_ID_ZEBAK)) != null)
			{
				zebakPathLevel = Integer.parseInt(widget.getText());
			}
		}
		else
		{
			if ((widget = client.getWidget(InterfaceID.TOA_RAID, CHILD_ID_DEFAULT)) == null)
			{
				return;
			}

			final int pathLevel = Integer.parseInt(widget.getText());

			switch (raidRoom)
			{
				case CRONDIS:
				case ZEBAK:
					zebakPathLevel = pathLevel;
					break;
				case SCABARAS:
				case KEPHRI:
					kephriPathLevel = pathLevel;
					break;
				case APMEKEN:
				case BABA:
					babaPathLevel = pathLevel;
					break;
				case HET:
				case AKKHA:
					akkhaPathLevel = pathLevel;
					break;
				default:
					break;
			}
		}
	}

}
