package com.duckblade.osrs.toa.features.reminders;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.LobbyEntranceWarning;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.InventoryUtil;
import com.duckblade.osrs.toa.util.MenuEntryUtil;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LobbyDaggerWarning
	implements PluginLifecycleComponent, LobbyEntranceWarning.Check
{

	private static final Set<Integer> DAGGER_IDS = ImmutableSet.of(
		ItemID.DRAGON_DAGGER,
		ItemID.DRAGON_DAGGER_P,
		ItemID.DRAGON_DAGGER_P_,
		ItemID.DRAGON_DAGGER_P__,
		ItemID.ABYSSAL_DAGGER,
		ItemID.ABYSSAL_DAGGER_P,
		ItemID.ABYSSAL_DAGGER_P_,
		ItemID.ABYSSAL_DAGGER_P__,
		ItemID.BR_DRAGON_DAGGER,
		ItemID.BH_ABYSSAL_DAGGER_IMBUE,
		ItemID.BH_ABYSSAL_DAGGER_P_IMBUE,
		ItemID.BH_ABYSSAL_DAGGER_P__IMBUE,
		ItemID.BH_ABYSSAL_DAGGER_P___IMBUE,
		ItemID.BH_DRAGON_DAGGER_CORRUPTED,
		ItemID.BH_DRAGON_DAGGER_P_CORRUPTED,
		ItemID.BH_DRAGON_DAGGER_P__CORRUPTED,
		ItemID.BH_DRAGON_DAGGER_P___CORRUPTED
	);

	private static final Color WARNING_COLOR = new Color(0x9A3535);
	private static final String WARNING_TEXT = "You are missing a dragon/abyssal dagger.";

	private final EventBus eventBus;
	private final Client client;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		// todo split
		return config.daggerWarning();
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

	@Override
	public boolean isTriggered()
	{
		return !InventoryUtil.containsAny(client, DAGGER_IDS);
	}

	@Override
	public String getText()
	{
		return WARNING_TEXT;
	}

	@Override
	public Color getColor()
	{
		return WARNING_COLOR;
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (isTriggered() && MenuEntryUtil.isEntry(e.getMenuEntry()))
		{
			e.getMenuEntry().setDeprioritized(true);
		}
	}
}
