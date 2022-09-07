package com.duckblade.osrs.toa.util;

import lombok.Value;

@Value
public class RaidRoomChanged
{

	private final RaidRoom previous;
	private final RaidRoom current;

}
