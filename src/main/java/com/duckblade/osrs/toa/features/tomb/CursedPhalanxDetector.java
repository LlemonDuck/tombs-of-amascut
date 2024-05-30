package com.duckblade.osrs.toa.features.tomb;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.Varbits;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class CursedPhalanxDetector implements PluginLifecycleComponent
{
	@Inject
	private EventBus eventBus;
	@Inject
	private Client client;
	@Inject
	private TombsOfAmascutConfig config;

	@Override
	public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
	{
		return raidState.getCurrentRoom() == RaidRoom.TOMB && config.cursedPhalanxDetect();
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
	private void onMenuOptionClicked(final MenuOptionClicked event)
	{
		if (!config.cursedPhalanxDetect() || client.getVarbitValue(Varbits.TOA_RAID_LEVEL) < 500)
		{
			return;
		}

		final MenuEntry menuEntry = event.getMenuEntry();

		if (!menuEntry.getOption().equals("Open"))
		{
			return;
		}

		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);

		if (itemContainer != null)
		{
			final Item weapon = itemContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());

			if (weapon != null && weapon.getId() == ItemID.OSMUMTENS_FANG_OR)
			{
				event.consume();
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Remove and/or drop cursed phalanx before doing that.", null);
				return;
			}
		}

		itemContainer = client.getItemContainer(InventoryID.INVENTORY);

		if (itemContainer != null)
		{
			if (itemContainer.contains(ItemID.OSMUMTENS_FANG_OR) || itemContainer.contains(ItemID.CURSED_PHALANX))
			{
				event.consume();
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Remove and/or drop cursed phalanx before doing that.", null);
			}
		}
	}
}
