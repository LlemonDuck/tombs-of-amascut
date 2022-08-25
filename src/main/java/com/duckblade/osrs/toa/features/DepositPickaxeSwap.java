package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.InventoryUtil;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DepositPickaxeSwap implements PluginLifecycleComponent
{

	private static final Set<Integer> PICKAXE_IDS = ImmutableSet.of(
		ItemID.BRONZE_PICKAXE,
		ItemID.IRON_PICKAXE,
		ItemID.STEEL_PICKAXE,
		ItemID.BLACK_PICKAXE,
		ItemID.MITHRIL_PICKAXE,
		ItemID.ADAMANT_PICKAXE,
		ItemID.RUNE_PICKAXE,
		ItemID.DRAGON_PICKAXE,
		ItemID.DRAGON_PICKAXE_12797,
		ItemID.DRAGON_PICKAXE_OR,
		ItemID.DRAGON_PICKAXE_OR_25376,
		ItemID.INFERNAL_PICKAXE,
		ItemID.INFERNAL_PICKAXE_OR,
		ItemID.INFERNAL_PICKAXE_UNCHARGED,
		ItemID.INFERNAL_PICKAXE_UNCHARGED_25369,
		ItemID.CRYSTAL_PICKAXE,
		ItemID.CRYSTAL_PICKAXE_23863,
		ItemID.CRYSTAL_PICKAXE_INACTIVE,
		ItemID._3RD_AGE_PICKAXE
	);

	private final EventBus eventBus;

	private final Client client;
	private final RaidStateTracker raidStateTracker;

	@Override
	public boolean isConfigEnabled(TombsOfAmascutConfig config)
	{
		return config.contextualSwapPickaxe();
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
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (!raidStateTracker.isInRaid() || raidStateTracker.getCurrentRoom() != RaidRoom.MIRRORS)
		{
			return;
		}

		if (isTakePickaxe(e.getMenuEntry()) && InventoryUtil.containsAny(client, PICKAXE_IDS))
		{
			e.getMenuEntry().setDeprioritized(true);
		}
	}

	private static boolean isTakePickaxe(MenuEntry menuEntry)
	{
		return menuEntry.getOption().equals("Take-pickaxe") &&
			menuEntry.getTarget().contains("Statue");
	}
}
