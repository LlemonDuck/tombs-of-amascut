package com.duckblade.osrs.toa.util;

import java.awt.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RaidMode
{

	ENTRY_MODE(0, 149, new Color(255, 208, 73)),
	NORMAL(150, 299, new Color(60, 79, 144)),
	EXPERT_MODE(300, Integer.MAX_VALUE, new Color(190, 38, 51)),
	;

	private final int minRaidLevel;
	private final int maxRaidLevel;

	@Getter
	private final Color color;

	public static RaidMode forRaidLevel(int raidLevel)
	{
		for (RaidMode mode : RaidMode.values())
		{
			if (mode.minRaidLevel <= raidLevel && raidLevel <= mode.maxRaidLevel)
			{
				return mode;
			}
		}

		throw new IllegalArgumentException("No raid mode exists for raid level " + raidLevel);
	}

}
