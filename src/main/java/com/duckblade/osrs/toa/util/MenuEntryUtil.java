package com.duckblade.osrs.toa.util;

import net.runelite.api.MenuEntry;

public class MenuEntryUtil
{
	public static boolean isEntry(MenuEntry menuEntry)
	{
		return menuEntry.getOption().contains("Enter") &&
			menuEntry.getTarget().contains("Entry");
	}
}
