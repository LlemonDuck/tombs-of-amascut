package com.duckblade.osrs.toa.launcher;

import com.duckblade.osrs.toa.TombsOfAmascutPlugin;
import com.duckblade.osrs.toa.launcher.debugplugins.ToaDebugPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TombsOfAmascutPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TombsOfAmascutPlugin.class, ToaDebugPlugin.class);
		RuneLite.main(args);
	}
}