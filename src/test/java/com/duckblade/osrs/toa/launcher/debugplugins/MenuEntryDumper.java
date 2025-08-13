package com.duckblade.osrs.toa.launcher.debugplugins;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MenuEntryDumper
{

	private final ToaDebugConfig config;

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (!config.menuEntryDumper())
		{
			return;
		}

		if (e.getOption().equals("Walk here") ||
			e.getOption().equals("Cancel"))
		{
			return;
		}

		log.debug(
			"{} | '{}' on '{}' ({}, {})",
			e.getMenuEntry().getType(),
			e.getOption(),
			e.getTarget(),
			e.getActionParam0(),
			e.getActionParam1()
		);
	}

}
