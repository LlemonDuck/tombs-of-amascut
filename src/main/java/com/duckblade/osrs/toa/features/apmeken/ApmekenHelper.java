package com.duckblade.osrs.toa.features.apmeken;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ApmekenHelper implements PluginLifecycleComponent
{

	private static final String MESSAGE_SENSE_ROOF_SUPPORTS = "you sense an issue with the roof supports.";
	private static final String MESSAGE_SENSE_FUMES = "you sense some strange fumes coming from holes in the floor.";
	private static final String MESSAGE_FIX_ROOF_SUPPORTS = "you repair the damaged roof support.";
	private static final String MESSAGE_FIX_ROOF_SUPPORTS_SIGHT = "apmeken's sight guides you into repairing the roof supports.";
	private static final String MESSAGE_FIX_FUMES = "you neutralise the fumes coming from the hole.";
	private static final String MESSAGE_FIX_FUMES_SIGHT = "apmeken's sight guides you into neutralising some dangerous fumes.";
	private static final String MESSAGE_FAIL_ROOF_SUPPORTS = "damaged roof supports cause some debris to fall on you!";
	private static final String MESSAGE_FAIL_FUMES = "the fumes filling the room suddenly ignite!";
	private static final String MESSAGE_DIED = "you have died";
	private static final String MESSAGE_CHALLENGE_COMPLETE = "challenge complete";

	private final EventBus eventBus;
	private final TombsOfAmascutConfig config;

	private ApmekenSense apmekenSense = ApmekenSense.NONE;

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return raidState.getCurrentRoom() == RaidRoom.APMEKEN;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		reset();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		reset();
	}

	private void reset()
	{
		apmekenSense = ApmekenSense.NONE;
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE) return;

		final String message = Text.standardize(event.getMessage());

		if (message.startsWith(MESSAGE_CHALLENGE_COMPLETE) || message.startsWith(MESSAGE_DIED))
		{
			apmekenSense = ApmekenSense.NONE;
			return;
		}

		switch (message)
		{
			case MESSAGE_FIX_ROOF_SUPPORTS:
			case MESSAGE_FIX_ROOF_SUPPORTS_SIGHT:
			case MESSAGE_FIX_FUMES:
			case MESSAGE_FIX_FUMES_SIGHT:
			case MESSAGE_FAIL_ROOF_SUPPORTS:
			case MESSAGE_FAIL_FUMES:
				apmekenSense = ApmekenSense.NONE;
				break;
			case MESSAGE_SENSE_FUMES:
				apmekenSense = ApmekenSense.VENTS;
				break;
			case MESSAGE_SENSE_ROOF_SUPPORTS:
				apmekenSense = ApmekenSense.ROOF;
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(final MenuEntryAdded event)
	{
		if (!config.apmekenRoofDeprioritizeEnable() || apmekenSense == ApmekenSense.ROOF) return;

		final MenuEntry menuEntry = event.getMenuEntry();

		if (!menuEntry.getOption().equals("Repair")) return;

		menuEntry.setDeprioritized(true);
	}

	private enum ApmekenSense
	{
		NONE,
		VENTS,
		ROOF
	}

}
