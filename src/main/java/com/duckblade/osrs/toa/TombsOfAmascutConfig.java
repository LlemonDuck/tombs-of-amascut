package com.duckblade.osrs.toa;

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

	@ConfigItem(
		keyName = "scabarasTileHelper",
		name = "Scabaras Tile Helper",
		description = "When entering the Path of Scabaras, displays a tile puzzle helper in the RuneLite side panel.",
		position = 2
	)
	default boolean scabarasTileHelper()
	{
		return true;
	}

	@ConfigItem(
		keyName = "apmekenWaveHelper",
		name = "Apmeken Wave Helper",
		description = "When entering the Path of Apmeken, displays a list of the waves in the RuneLite side panel.",
		position = 3
	)
	default boolean apmekenWaveHelper()
	{
		return true;
	}

	@ConfigItem(
		keyName = "targetTimeDisplay",
		name = "Target Time in Timer",
		description = "Expand the in-raid timer to also show the target time to beat.",
		position = 4
	)
	default boolean targetTimeDisplay()
	{
		return true;
	}

	@ConfigSection(
		name = "Invocation Presets",
		description = "Save presets of invocations to quickly restore your invocations between runs of different types.",
		position = 100
	)
	String SECTION_INVOCATION_PRESETS = "invocationPresetsSection";

	@ConfigItem(
		keyName = "invocationPresetsEnable",
		name = "Enable Presets",
		description = "Allows for saving and restoring of invocation presets. Right-click \"Preset\" button to save/load.",
		section = SECTION_INVOCATION_PRESETS,
		position = 101
	)
	default boolean invocationPresetsEnable()
	{
		return true;
	}

	@ConfigItem(
		keyName = "invocationPresetsScroll",
		name = "Auto-Scroll",
		description = "Automatically scroll to invocations which need to be changed for the current preset.",
		section = SECTION_INVOCATION_PRESETS,
		position = 102
	)
	default boolean invocationPresetsScroll()
	{
		return true;
	}

	@ConfigItem(
		keyName = "leftClickProceedEnable",
		name = "Proceed/Begin/Leave Swap",
		description = "Left click proceed/begin/leave on Osmumten after completing rooms and before the Wardens fight.",
		position = 3
	)
	default boolean leftClickProceedEnable()
	{
		return true;
	}

	@ConfigSection(
		name = "Invocation Screenshot",
		description = "All config options related to the Invocation Screenshot functionality",
		closedByDefault = true,
		position = 200
	)
	String SECTION_INVOCATION_SCREENSHOT = "invocationScreenshotSection";

	@ConfigItem(
		keyName = "invocationScreenshotEnable",
		name = "Enable Screenshot button",
		description = "Adds a button to the ToA Invocation interface that will copy all invocations as an image to your system clipboard",
		section = SECTION_INVOCATION_SCREENSHOT,
		position = 201
	)
	default boolean invocationScreenshotEnable()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showRewardsSection",
		name = "Show Rewards Section",
		description = "<html>Should the rewards section be included<br/>(requires the Reward button to be selected within the interface)</html>",
		section = SECTION_INVOCATION_SCREENSHOT,
		position = 202
	)
	default boolean showRewardsSection()
	{
		return true;
	}

	@ConfigItem(
		keyName = "useResourcePack",
		name = "Use Resource Pack",
		description = "Use Resource Pack Theme for screenshot background",
		section = SECTION_INVOCATION_SCREENSHOT,
		position = 203
	)
	default boolean useResourcePack()
	{
		return true;
	}
}
