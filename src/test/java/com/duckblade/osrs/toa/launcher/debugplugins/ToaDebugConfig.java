package com.duckblade.osrs.toa.launcher.debugplugins;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import java.util.Collections;
import java.util.Set;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(TombsOfAmascutConfig.CONFIG_GROUP + "Debug")
public interface ToaDebugConfig extends Config
{

	@ConfigItem(
		keyName = "hetSolveDebug",
		description = "",
		name = "Het Solve Debug"
	)
	default boolean hetSolveDebug()
	{
		return false;
	}

	@ConfigItem(
		keyName = "akkhaShadowHsl",
		description = "",
		name = "Akkha Shadow HSL"
	)
	default boolean akkhaShadowHsl()
	{
		return false;
	}

	@ConfigItem(
		keyName = "menuEntryDumper",
		description = "",
		name = "Menu Entry Log"
	)
	default boolean menuEntryDumper()
	{
		return false;
	}

	@ConfigItem(
		keyName = "stateOverlay",
		description = "",
		name = "State Overlay"
	)
	default Set<StateOverlay.StateDisplays> stateOverlay()
	{
		return Collections.emptySet();
	}

}
