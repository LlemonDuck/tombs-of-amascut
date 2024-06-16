package com.duckblade.osrs.toa.features.tomb;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DryStreakTracker implements PluginLifecycleComponent
{
	private static final int VARBIT_ID_SARCOPHAGUS = 14373;
	private static final int VARBIT_VALUE_CHEST_KEY = 2;

	private static final int[] VARBIT_MULTILOC_IDS_CHEST = new int[]{
		14356, 14357, 14358, 14359, 14360, 14370, 14371, 14372
	};

	private final EventBus eventBus;
	private final Client client;
	private final ClientThread clientThread;
	private final TombsOfAmascutConfig config;
	private final ChatMessageManager chatMessageManager;
	private final RaidStateTracker raidStateTracker;

	private boolean chestOpened;
	private boolean purple;
	private boolean purpleIsMine;

	private int previousCount;

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return config.trackPurpleDryCountMode() != PurpleTrackingMode.OFF &&
			(raidState.getCurrentRoom() == RaidRoom.TOMB || raidState.isInLobby());
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);

		// TODO: prevent arbitrarily increasing counter by toggling config option
		if (raidStateTracker.getCurrentState().getCurrentRoom() == RaidRoom.TOMB)
		{
			clientThread.invokeLater(() ->
			{
				chestOpened = false;
				purple = purpleIsMine = client.getVarbitValue(VARBIT_ID_SARCOPHAGUS) % 2 != 0;

				for (final int varbitId : VARBIT_MULTILOC_IDS_CHEST)
				{
					if (client.getVarbitValue(varbitId) == VARBIT_VALUE_CHEST_KEY)
					{
						purpleIsMine = false;
						break;
					}
				}

				previousCount = config.getPurpleDryStreakCount();

				final int count;

				switch (config.trackPurpleDryCountMode())
				{
					case ANY:
						count = purple ? 0 : previousCount + 1;
						break;
					case MINE:
						count = purpleIsMine ? 0 : previousCount + 1;
						break;
					default:
						return;
				}

				config.setPurpleDryStreakCount(count);
			});
		}
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onWidgetLoaded(final WidgetLoaded widgetLoaded)
	{
		if (chestOpened)
		{
			return;
		}

		if (widgetLoaded.getGroupId() == InterfaceID.TOA_REWARD)
		{
			chestOpened = true;

			final String fmt = "<col=800080>Purple</col> Dry Streak%s: <col=ff0000>%d</col>";
			final String msg;

			switch (config.trackPurpleDryCountMode())
			{
				case ANY:
					msg = String.format(fmt,
						purple ? " Ended" : "",
						purple ? previousCount : config.getPurpleDryStreakCount());
					break;
				case MINE:
					msg = String.format(fmt,
						purpleIsMine ? " Ended" : "",
						purpleIsMine ? previousCount : config.getPurpleDryStreakCount());
					break;
				default:
					return;
			}

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.GAMEMESSAGE)
				.runeLiteFormattedMessage(msg)
				.build());
		}
	}
}
