package com.duckblade.osrs.toa;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

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

	@ConfigSection(
		name = "Invocation Presets",
		description = "Allows for saving and restoring of invocation presets. Right-click \"Preset\" button to save/load.\n\n" +
			"In order to comply with Jagex's third party client rules, " +
			"this plugin can only render highlights over the presets to indicate which ones should be toggled," +
			"it cannot automatically swap the preset options for you.",
		position = 100
	)
	String SECTION_INVOCATION_PRESETS = "invocationpresets";

	@ConfigItem(
		keyName = "invocationPresetsEnable",
		name = "Enable Presets",
		description = "Allows for saving and restoring of invocation presets. Right-click \"Preset\" button to save/load.",
		position = 101,
		section = SECTION_INVOCATION_PRESETS
	)
	default boolean invocationPresetsEnable()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "invocationPresetsEnableColor",
		name = "Enable Color",
		description = "Color to highlight invocations that need to be enabled.",
		position = 102,
		section = SECTION_INVOCATION_PRESETS
	)
	default Color invocationPresetsEnableColor()
	{
		return new Color(0, 255, 0, 127);
	}

	@Alpha
	@ConfigItem(
		keyName = "invocationPresetsDisableColor",
		name = "Disable Color",
		description = "Color to highlight invocations that need to be disabled.",
		position = 103,
		section = SECTION_INVOCATION_PRESETS
	)
	default Color invocationPresetsDisableColor()
	{
		return new Color(255, 0, 0, 127);
	}

}
