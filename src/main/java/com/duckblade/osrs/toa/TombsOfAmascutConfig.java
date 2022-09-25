package com.duckblade.osrs.toa;

import com.duckblade.osrs.toa.features.het.DepositPickaxeMode;
import com.duckblade.osrs.toa.features.scabaras.ScabarasHelperMode;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

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
	default DepositPickaxeMode contextualSwapPickaxe()
	{
		return DepositPickaxeMode.STATUE_SWAP;
	}

	@ConfigItem(
		keyName = "scabarasHelperMode",
		name = "Scabaras Helpers",
		description = "Puzzle helpers for the Path of Scabaras (leading to Kephri).",
		position = 2
	)
	default ScabarasHelperMode scabarasHelperMode()
	{
		return ScabarasHelperMode.OVERLAY;
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
		return false;
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
		name = "Quick Proceed Swaps",
		description = "Left click proceed/begin/leave on Osmumten and quick-enter/quick-use entryways and teleport crystals.",
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
		position = 100
	)
	String SECTION_INVOCATION_SCREENSHOT = "invocationScreenshotSection";

	@ConfigItem(
		keyName = "invocationScreenshotEnable",
		name = "Enable Screenshot button",
		description = "Adds a button to the ToA Invocation interface that will copy all invocations as an image to your system clipboard",
		section = SECTION_INVOCATION_SCREENSHOT,
		position = 101
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
		position = 102
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
		position = 103
	)
	default boolean useResourcePack()
	{
		return true;
	}

	@ConfigSection(
		name = "Purple Chest Jingle",
		description = "<html>Can play a custom sound file when the purple loot sarcophagus is opened." +
			"<br/>Custom sound must be provided at ~/.runelite/tombs-of-amascut/toa-chest.wav</html>",
		position = 200,
		closedByDefault = true
	)
	String SECTION_SARCOPHAGUS_SOUND = "sectionSarcophagusSound";

	@ConfigItem(
		keyName = "chestAudioEnable",
		name = "Enable",
		description = "<html>Either disables the feature or plays an audio file whenever the purple chest is opened." +
			"<br/>The custom audio file should be named toa-chest.wav inside the .runelite/tombs-of-amascut folder</html>",
		section = SECTION_SARCOPHAGUS_SOUND,
		position = 201
	)
	default boolean chestAudioEnable()
	{
		return false;
	}

	String CHEST_AUDIO_VOLUME_KEY = "chestAudioVolume";
	@Range(
		max = 200
	)
	@ConfigItem(
		keyName = CHEST_AUDIO_VOLUME_KEY,
		name = "Audio Volume",
		description = "Adjusts how loud the chest audio is when played. 100 is no change to file volume.",
		section = SECTION_SARCOPHAGUS_SOUND,
		position = 202
	)
	default int chestAudioVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "updateNotifierLastVersion",
		name = "",
		description = "",
		hidden = true
	)
	default int updateNotifierLastVersion()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "updateNotifierLastVersion",
		name = "",
		description = "",
		hidden = true
	)
	void updateNotifierLastVersion(int newVersion);
}
