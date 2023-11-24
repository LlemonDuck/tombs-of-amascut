package com.duckblade.osrs.toa.features.het.pickaxe;

import com.duckblade.osrs.toa.util.InventoryUtil;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.experimental.UtilityClass;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;

@UtilityClass
public class PickaxeUtil
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
		ItemID._3RD_AGE_PICKAXE,
		ItemID.GILDED_PICKAXE,
		ItemID.TRAILBLAZER_PICKAXE
	);

	private static final int VARBIT_PICKAXE_STORAGE = 14440;

	public static boolean hasPickaxe(Client client)
	{

		ItemContainer inv = client.getItemContainer(InventoryID.INVENTORY);
		ItemContainer equip = client.getItemContainer(InventoryID.EQUIPMENT);
		if (inv == null && equip == null)
		{
			return false;
		}

		return (inv != null && InventoryUtil.containsAny(inv, PICKAXE_IDS)) ||
			(equip != null && InventoryUtil.containsAny(equip, PICKAXE_IDS));
	}

	public static boolean pickaxeIsInStorage(Client client)
	{
		return client.getVarbitValue(VARBIT_PICKAXE_STORAGE) != 0;
	}

}
