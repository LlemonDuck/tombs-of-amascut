package com.duckblade.osrs.toa.features.pointstracker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ItemID;

@RequiredArgsConstructor
@Getter
public enum Purple
{

	OSMUMTENS_FANG(ItemID.OSMUMTENS_FANG, "Osmumten's Fang", "Fang"),
	LIGHTBEARER(ItemID.LIGHTBEARER, "Lightbearer", "Ring"),
	ELIDINIS_WARD(ItemID.ELIDINIS_WARD, "Elidinis' Ward", "Ward"),
	MASORI_MASK(ItemID.MASORI_MASK, "Masori Mask", "Mask"),
	MASORI_BODY(ItemID.MASORI_BODY, "Masori Body", "Body"),
	MASORI_CHAPS(ItemID.MASORI_CHAPS, "Masori Chaps", "Chaps"),
	TUMEKENS_SHADOW(ItemID.TUMEKENS_SHADOW_UNCHARGED, "Tumeken's Shadow", "Shadow"),
	;

	private final int itemId;
	private final String itemName;
	private final String shortName;

	public static Purple forItemId(int itemId)
	{
		for (Purple purple : Purple.values())
		{
			if (purple.getItemId() == itemId)
			{
				return purple;
			}
		}

		return null;
	}

}
