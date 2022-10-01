package com.duckblade.osrs.toa;

import com.duckblade.osrs.toa.features.het.DepositPickaxeMode;
import com.duckblade.osrs.toa.features.scabaras.ScabarasHelperMode;
import com.duckblade.osrs.toa.features.scabaras.overlay.MatchingTileDisplayMode;
import java.awt.Color;
import net.runelite.client.config.Alpha;
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
		keyName = "depositPickaxeMode",
		name = "Deposit-Pickaxe",
		description = "Automatically swap to Deposit-pickaxe when a pickaxe is in your inventory.",
		position = 1
	)
	default DepositPickaxeMode depositPickaxeMode()
	{
		return DepositPickaxeMode.STATUE_SWAP;
	}

	@ConfigItem(
		keyName = "beamTimer",
		name = "Beam Timer",
		description = "Show a timer in the Path of Het to see when the beam will fire.",
		position = 2
	)
	default boolean beamTimer()
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

	@ConfigItem(
		keyName = "leftClickProceedEnable",
		name = "Quick Proceed Swaps",
		description = "Left click proceed/begin/leave on Osmumten and quick-enter/quick-use entryways and teleport crystals.",
		position = 5
	)
	default boolean leftClickProceedEnable()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showUpdateMessages",
		name = "Show Updates",
		description = "Opens a panel describing plugin updates after new features are added to the plugin.",
		position = 6
	)
	default boolean showUpdateMessages()
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

	@ConfigSection(
		name = "Path of Scabaras",
		description = "Options for the puzzles in the Path of Scabaras.",
		position = 300
	)
	String SECTION_SCABARAS = "sectionScabaras";

	@ConfigItem(
		keyName = "scabarasHelperMode",
		name = "Scabaras Helpers",
		description = "Puzzle helpers for the Path of Scabaras (leading to Kephri).",
		position = 301,
		section = SECTION_SCABARAS
	)
	default ScabarasHelperMode scabarasHelperMode()
	{
		return ScabarasHelperMode.OVERLAY;
	}

	@ConfigItem(
		keyName = "scabarasAdditionTileColor",
		name = "Addition Colour",
		description = "Highlight colour for tiles in the addition puzzle." +
			"<br/>Set alpha to 0 to disable.",
		position = 302,
		section = SECTION_SCABARAS
	)
	@Alpha
	default Color scabarasAdditionTileColor()
	{
		return Color.red;
	}

	@ConfigItem(
		keyName = "scabarasLightTileColor",
		name = "Light Flip Colour",
		description = "Highlight colour for tiles in the light flips puzzle." +
			"<br/>Set alpha to 0 to disable.",
		position = 303,
		section = SECTION_SCABARAS
	)
	@Alpha
	default Color scabarasLightTileColor()
	{
		return Color.red;
	}

	@ConfigItem(
		keyName = "scabarasObeliskColor1",
		name = "Obelisk Start",
		description = "Start colour for highlighting the obelisks in the obelisk puzzle." +
			"<br/>Set alpha to 0 to disable.",
		position = 304,
		section = SECTION_SCABARAS
	)
	@Alpha
	default Color scabarasObeliskColorStart()
	{
		return Color.cyan;
	}

	@ConfigItem(
		keyName = "scabarasObeliskColor2",
		name = "Obelisk End",
		description = "End colour for highlighting the obelisks in the obelisk puzzle." +
			"<br/>Set alpha to 0 to disable.",
		position = 305,
		section = SECTION_SCABARAS
	)
	@Alpha
	default Color scabarasObeliskColorEnd()
	{
		return Color.blue;
	}

	@ConfigItem(
		keyName = "scabarasSequenceColor1",
		name = "Sequence Start",
		description = "Start colour for highlighting the tiles in the sequence (simon says) puzzle." +
			"<br/>Set alpha to 0 to disable.",
		position = 306,
		section = SECTION_SCABARAS
	)
	@Alpha
	default Color scabarasSequenceColorStart()
	{
		return Color.cyan;
	}

	@ConfigItem(
		keyName = "scabarasSequenceColor2",
		name = "Sequence End",
		description = "End colour for highlighting the tiles in the sequence (simon says) puzzle." +
			"<br/>Set alpha to 0 to disable.",
		position = 307,
		section = SECTION_SCABARAS
	)
	@Alpha
	default Color scabarasSequenceColorEnd()
	{
		return Color.blue;
	}

	@ConfigItem(
		keyName = "scabarasMatchingDisplayMode",
		name = "Matching Display",
		description = "Whether to show highlight tiles, show names of tiles, or both for the matching puzzle.",
		position = 308,
		section = SECTION_SCABARAS
	)
	default MatchingTileDisplayMode scabarasMatchingDisplayMode()
	{
		return MatchingTileDisplayMode.BOTH;
	}

	@ConfigItem(
		keyName = "scabarasMatchingCompletedOpacity",
		name = "Matched Opacity",
		description = "Opacity (transparency) of completed tiles in the matching puzzle." +
			"<br/>Set to 0 to hide completed tiles completely.",
		position = 309,
		section = SECTION_SCABARAS
	)
	@Range(
		min = 0,
		max = 255
	)
	default int scabarasMatchingCompletedOpacity()
	{
		return 64;
	}

	@ConfigSection(
		name = "Purple Chest Jingle",
		description = "<html>Can play a custom sound file when the purple loot sarcophagus is opened." +
			"<br/>Custom sound must be provided at ~/.runelite/tombs-of-amascut/toa-chest.wav</html>",
		position = 400,
		closedByDefault = true
	)
	String SECTION_SARCOPHAGUS_SOUND = "sectionSarcophagusSound";

	@ConfigItem(
		keyName = "chestAudioEnable",
		name = "Enable",
		description = "<html>Either disables the feature or plays an audio file whenever the purple chest is opened." +
			"<br/>The custom audio file should be named toa-chest.wav inside the .runelite/tombs-of-amascut folder</html>",
		section = SECTION_SARCOPHAGUS_SOUND,
		position = 401
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
		position = 402
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
