package com.duckblade.osrs.toa.util;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RaidRoom
{

	NEXUS(new int[]{14160}),
	CRONDIS(new int[]{15698}),
	ZEBAK(new int[]{15700}),
	SCABARAS(new int[]{14162}),
	KEPHRI(new int[]{14164}),
	APMEKEN(new int[]{15186}),
	BABA(new int[]{15188}),
	HET(new int[]{14674}),
	AKKHA(new int[]{14676}),
	WARDENS(new int[]{15184, 15696}),
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
