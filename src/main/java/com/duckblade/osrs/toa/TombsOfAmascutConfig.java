package com.duckblade.osrs.toa;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(TombsOfAmascutConfig.CONFIG_GROUP)
public interface TombsOfAmascutConfig extends Config
{

	String CONFIG_GROUP = "tombsofamascut";

	@ConfigItem(
		keyName = "contextualSwapPickaxe",
		name = "Swap Deposit-Pickaxe",
		description = "Automatically swap to Deposit-pickaxe when a pickaxe is in your inventory.",
		position = 1
	)
	default boolean contextualSwapPickaxe()
	{
		return true;
	}

}
