package com.duckblade.osrs.toa;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TombsOfAmascutPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TombsOfAmascutPlugin.class);
		RuneLite.main(args);
	}
}