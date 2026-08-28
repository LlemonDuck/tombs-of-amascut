package com.duckblade.osrs.toa.features.het.pickaxe;

import com.duckblade.osrs.toa.util.InventoryUtil;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.experimental.UtilityClass;
import net.runelite.api.Client;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;

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
		ItemID.DRAGON_PICKAXE_PRETTY,
		ItemID.ZALCANO_PICKAXE,
		ItemID.TRAILBLAZER_PICKAXE_NO_INFERNAL,
		ItemID.INFERNAL_PICKAXE,
		ItemID.INFERNAL_PICKAXE_EMPTY,
		ItemID.TRAILBLAZER_PICKAXE,
		ItemID.TRAILBLAZER_PICKAXE_EMPTY,
		ItemID.CRYSTAL_PICKAXE,
		ItemID.CRYSTAL_PICKAXE_INACTIVE,
		ItemID._3A_PICKAXE,
		ItemID.TRAIL_GILDED_PICKAXE,
		ItemID.LEAGUE_TRAILBLAZER_PICKAXE,
		ItemID.TRAILBLAZER_RELOADED_PICKAXE,
		ItemID.TRAILBLAZER_RELOADED_PICKAXE_EMPTY,
		ItemID.TRAILBLAZER_RELOADED_PICKAXE_NO_INFERNAL
	);

	public static boolean hasPickaxe(Client client)
	{
		ItemContainer inv = client.getItemContainer(InventoryID.INV);
		ItemContainer equip = client.getItemContainer(InventoryID.WORN);
		if (inv == null && equip == null)
		{
			return false;
		}

		return (InventoryUtil.containsAny(inv, PICKAXE_IDS)) ||
			(InventoryUtil.containsAny(equip, PICKAXE_IDS));
	}

	public static boolean pickaxeIsInStorage(Client client)
	{
		return client.getVarbitValue(VarbitID.TOA_PICKAXE_STORED) != 0;
	}

}
