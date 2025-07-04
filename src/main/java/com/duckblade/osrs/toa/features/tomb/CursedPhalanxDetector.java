package com.duckblade.osrs.toa.features.tomb;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.InventoryUtil;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CursedPhalanxDetector implements PluginLifecycleComponent
{
	private static final Set<Integer> CURSED_PHALANX_ITEM_IDS = ImmutableSet.of(
		ItemID.OSMUMTENS_FANG_ORNAMENT_KIT,
		ItemID.OSMUMTENS_FANG_ORNAMENT
	);

	private boolean isEligibleForKit = true;

	private final EventBus eventBus;
	private final Client client;
	private final RaidStateTracker raidStateTracker;

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return raidState.isInRaid() &&
			config.cursedPhalanxDetect();
	}

	@Override
	public void startUp()
	{
		isEligibleForKit = true;
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	private void onChatMessage(ChatMessage e)
	{
		if (e.getType() != ChatMessageType.GAMEMESSAGE || !isEligibleForKit)
		{
			return;
		}

		if (e.getMessage().contains("Total deaths"))
		{
			isEligibleForKit = false;
		}
	}

	@Subscribe
	private void onMenuOptionClicked(final MenuOptionClicked event)
	{
		if (!isEligibleForKit ||
			raidStateTracker.getCurrentState().getCurrentRoom() != RaidRoom.TOMB ||
			client.getVarbitValue(VarbitID.TOA_CLIENT_RAID_LEVEL) < 500)
		{
			return;
		}

		final MenuEntry menuEntry = event.getMenuEntry();
		if (!menuEntry.getOption().equals("Open"))
		{
			return;
		}

		boolean wearingPhalanx = InventoryUtil.containsAny(client.getItemContainer(InventoryID.WORN), CURSED_PHALANX_ITEM_IDS);
		boolean carryingPhalanx = InventoryUtil.containsAny(client.getItemContainer(InventoryID.INV), CURSED_PHALANX_ITEM_IDS);

		if (wearingPhalanx || carryingPhalanx)
		{
			event.consume();
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Remove and/or drop cursed phalanx before doing that.", null);
		}
	}
}
