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

	private final EventBus eventBus;
	private final Client client;
	private final ClientThread clientThread;
	private final TombsOfAmascutConfig config;
	private final ChatMessageManager chatMessageManager;
	private final RaidStateTracker raidStateTracker;

	private boolean chestOpened;
	private boolean purple;
	private int previousCount;

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return config.trackPurpleDryCount() && (raidState.getCurrentRoom() == RaidRoom.TOMB || raidState.isInLobby());
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
				purple = client.getVarbitValue(VARBIT_ID_SARCOPHAGUS) % 2 != 0;
				previousCount = config.getPurpleDryStreakCount();
				config.setPurpleDryStreakCount(purple ? 0 : previousCount + 1);
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

			final String msg;

			if (purple)
			{
				msg = String.format("<col=800080>Purple</col> Dry Streak Ended: <col=ff0000>%d</col>", previousCount);
			}
			else
			{
				msg = String.format("<col=800080>Purple</col> Dry Streak: <col=ff0000>%d</col>", config.getPurpleDryStreakCount());
			}

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.GAMEMESSAGE)
				.runeLiteFormattedMessage(msg)
				.build());
		}
	}
}
