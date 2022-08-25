package com.duckblade.osrs.toa.util;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RaidRoom
{

	MIRRORS(new int[]{14674}),
	;

	private final int[] regionIds;

	public static RaidRoom forRegionId(int region)
	{
		for (RaidRoom r : RaidRoom.values())
		{
			for (int regionId : r.regionIds)
			{
				if (regionId == region)
				{
					return r;
				}
			}
		}

		return null;
	}

}
