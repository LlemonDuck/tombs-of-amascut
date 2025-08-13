package com.duckblade.osrs.toa.util;

import net.runelite.api.Client;

public enum TimerMode
{

	PRECISE,
	LAX,
	;

	private static final int VARBIT_PRECISE_TIMING = 11866;

	public static TimerMode fromClient(Client c)
	{
		assert c.isClientThread();
		return c.getVarbitValue(VARBIT_PRECISE_TIMING) == 1 ? PRECISE : LAX;
	}

}
