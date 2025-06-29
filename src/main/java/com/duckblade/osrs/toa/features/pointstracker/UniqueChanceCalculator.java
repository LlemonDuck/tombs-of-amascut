package com.duckblade.osrs.toa.features.pointstracker;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UniqueChanceCalculator
{

	private static final int BASE_RATE_UNIQUE = 10_500;
	private static final int MODIFIER_RAID_LEVEL_UNIQUE = 20;

	private static final int BASE_RATE_PET = 350_000;
	private static final int MODIFIER_RAID_LEVEL_PET = 700;

	private static final int RAID_LEVEL_REDUCE_FLOOR = 400;
	private static final int RAID_LEVEL_REDUCE_FACTOR = 3;
	private static final int RAID_LEVEL_MAX = 550;

	private static final double MAX_RATE_UNIQUE = 55.0;

	public static double getUniqueChance(int raidLevel, int points)
	{
		return getChance(raidLevel, points, BASE_RATE_UNIQUE, MODIFIER_RAID_LEVEL_UNIQUE);
	}

	public static double getPetChance(int raidLevel, int points)
	{
		return getChance(raidLevel, points, BASE_RATE_PET, MODIFIER_RAID_LEVEL_PET);
	}

	private static double getChance(int raidLevel, int points, int baseRate, int modifier)
	{
		int raidLevelForPoints = Math.min(raidLevel, RAID_LEVEL_REDUCE_FLOOR);
		if (raidLevel > RAID_LEVEL_REDUCE_FLOOR)
		{
			raidLevelForPoints += Math.min(raidLevel, RAID_LEVEL_MAX) - RAID_LEVEL_REDUCE_FLOOR / RAID_LEVEL_REDUCE_FACTOR;
		}

		double denominator = baseRate - (modifier * raidLevelForPoints);
		return Math.max(0, Math.min(MAX_RATE_UNIQUE, points / denominator));
	}
}
