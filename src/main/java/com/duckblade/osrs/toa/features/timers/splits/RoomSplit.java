package com.duckblade.osrs.toa.features.timers.splits;

import com.duckblade.osrs.toa.util.RaidRoom;
import lombok.Value;

@Value
public class RoomSplit
{

	private final RaidRoom room;
	private final int overallTicks;
	private final int challengeTicks;

}
