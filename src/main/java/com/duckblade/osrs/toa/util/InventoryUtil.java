package com.duckblade.osrs.toa.util;

import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

public class InventoryUtil
{

	public static boolean containsAny(Client client, Set<Integer> ids)
	{
		ItemContainer inv = client.getItemContainer(InventoryID.INVENTORY);
		if (inv == null)
		{
			return false;
		}

		return containsAny(inv, ids);
	}

	public static boolean containsAny(ItemContainer inv, Set<Integer> ids)
	{
		for (Item item : inv.getItems())
		{
			if (ids.contains(item.getId()))
			{
				return true;
			}
		}

		return false;
	}

}
