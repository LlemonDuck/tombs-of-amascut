package com.duckblade.osrs.toa.util;

import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

public enum TimerMode
{

	PRECISE,
	LAX,
	;

	public static TimerMode fromClient(Client c)
	{
		assert c.isClientThread();
		return c.getVarbitValue(VarbitID.OPTION_PRECISE_TIMING) == 1 ? PRECISE : LAX;
	}

}
