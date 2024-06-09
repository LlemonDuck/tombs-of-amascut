package com.duckblade.osrs.toa;

import com.duckblade.osrs.toa.features.QuickProceedSwaps.QuickProceedEnableMode;
import com.duckblade.osrs.toa.features.hporbs.HpOrbMode;
import com.duckblade.osrs.toa.features.scabaras.ScabarasHelperMode;
import com.duckblade.osrs.toa.features.scabaras.SkipObeliskOverlay;
import com.duckblade.osrs.toa.features.scabaras.overlay.MatchingTileDisplayMode;
import com.duckblade.osrs.toa.features.timetracking.SplitsMode;
import com.duckblade.osrs.toa.features.updatenotifier.UpdateNotifier;
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

	// Sections

	@ConfigSection(
		name = "Path of Het",
		description = "Helpers for the Path of Het.",
		position = 0,
		closedByDefault = true
	)
	String SECTION_HET = "sectionHet";

	@ConfigSection(
		name = "Path of Scabaras",
		description = "Options for the puzzles in the Path of Scabaras.",
		position = 1,
		closedByDefault = true
	)
	String SECTION_SCABARAS = "sectionScabaras";

	@ConfigSection(
		name = "Burial Tomb",
		description = "Configuration for the burial tomb.",
		position = 2,
		closedByDefault = true
	)
	String SECTION_BURIAL_TOMB = "sectionBurialTomb";

	@ConfigSection(
		name = "Invocation Presets",
		description = "Save presets of invocations to quickly restore your invocations between runs of different types.",
		position = 3,
		closedByDefault = true
	)
	String SECTION_INVOCATION_PRESETS = "invocationPresetsSection";

	@ConfigSection(
		name = "Invocation Screenshot",
		description = "All config options related to the Invocation Screenshot functionality",
		position = 4,
		closedByDefault = true
	)
	String SECTION_INVOCATION_SCREENSHOT = "invocationScreenshotSection";

	@ConfigSection(
		name = "Points Tracker",
		description = "<html>Tracks points for the raid, used in calculating drop chance." +
			"<br/>NOTE: For teams, you MUST use the RuneLite Party plugin to receive team drop chance.</html>",
		position = 5,
		closedByDefault = true
	)
	String SECTION_POINTS_TRACKER = "sectionPointsTracker";

	@ConfigSection(
		name = "Time Tracking",
		description = "Time tracking and splits.",
		position = 6,
		closedByDefault = true
	)
	String SECTION_TIME_TRACKING = "sectionTimeTracking";

	@ConfigSection(
		name = "Miscellaneous",
		description = "Miscellaneous configurations.",
		position = 7,
		closedByDefault = true
	)
	String SECTION_MISCELLANEOUS = "sectionMiscellaneous";

	// Invocation Presets

	@ConfigItem(
		keyName = "invocationPresetsEnable",
		name = "Enable Presets",
		description = "Allows for saving and restoring of invocation presets. Right-click \"Preset\" button to save/load.",
		section = SECTION_INVOCATION_PRESETS,
		position = 0
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
		position = 1
	)
	default boolean invocationPresetsScroll()
	{
		return true;
	}

	// Invocation Screenshot

	@ConfigItem(
		keyName = "invocationScreenshotEnable",
		name = "Enable Screenshot button",
		description = "Adds a button to the ToA Invocation interface that will copy all invocations as an image to your system clipboard",
		section = SECTION_INVOCATION_SCREENSHOT,
		position = 0
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
		position = 1
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
		position = 2
	)
	default boolean useResourcePack()
	{
		return true;
	}

	// Scabaras

	@ConfigItem(
		keyName = "scabarasHelperMode",
		name = "Scabaras Helpers",
		description = "Puzzle helpers for the Path of Scabaras (leading to Kephri).",
		position = 0,
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
		position = 1,
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
		position = 2,
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
		position = 3,
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
		position = 4,
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
		position = 5,
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
		position = 6,
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
		position = 7,
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
		position = 8,
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

	@ConfigItem(
		keyName = "scabarasHighlightSkipObeliskEntry",
		name = "Show Obelisk Skip",
		description = "Highlight which entrance will skip requiring the obelisk puzzle.",
		position = 9,
		section = SECTION_SCABARAS
	)
	default SkipObeliskOverlay.EnableMode scabarasHighlightSkipObeliskEntry()
	{
		return SkipObeliskOverlay.EnableMode.OFF;
	}

	// Het

	@ConfigItem(
		keyName = "hetBeamTimerEnable",
		name = "Beam Timer",
		description = "<html>Display an overlay of when the Caster Statue will fire." +
			"<br/>Click Het's Seal from one tile away when the indicator is GREEN to get an extra damage tick.</html>",
		position = 0,
		section = SECTION_HET
	)
	default boolean hetBeamTimerEnable()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hetSolverEnable",
		name = "Mirror Puzzle Solver",
		description = "Show where to place/clean mirrors for the active puzzle layout.",
		position = 1,
		section = SECTION_HET
	)
	default boolean hetSolverEnable()
	{
		return true;
	}

	String KEY_HET_PICKAXE_MENU_SWAP = "hetPickaxeMenuSwap";

	@ConfigItem(
		keyName = KEY_HET_PICKAXE_MENU_SWAP,
		name = "Deposit-Pickaxe",
		description = "Automatically swap to Deposit-pickaxe when a pickaxe is in your inventory.",
		position = 2,
		section = SECTION_HET
	)
	default boolean hetPickaxeMenuSwap()
	{
		return true;
	}

	String KEY_HET_PICKAXE_PREVENT_EXIT = "hetPickaxePreventExit";

	@ConfigItem(
		keyName = KEY_HET_PICKAXE_PREVENT_EXIT,
		name = "Prevent Room Exit",
		description = "Deprioritize the option to leave the puzzle room until you have deposited your pickaxe in the statue.",
		position = 3,
		section = SECTION_HET
	)
	default boolean hetPickaxePreventExit()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hetPickaxePreventRaidStart",
		name = "Prevent Raid Start",
		description = "Deprioritize the option to enter the raid until you have deposited your pickaxe in the lobby wall cavity.",
		position = 4,
		section = SECTION_HET
	)
	default boolean hetPickaxePreventRaidStart()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hetPickaxePuzzleOverlay",
		name = "Puzzle Room Visual Warning",
		description = "Add a visual warning reminder to deposit your pickaxe at the end of the mirror puzzle room.",
		position = 5,
		section = SECTION_HET
	)
	default boolean hetPickaxePuzzleOverlay()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hetPickaxeLobbyOverlay",
		name = "Lobby Visual Warning",
		description = "Add a visual warning reminder to deposit your pickaxe in the raid lobby.",
		position = 6,
		section = SECTION_HET
	)
	default boolean hetPickaxeLobbyOverlay()
	{
		return false;
	}

	// Points Tracker

	@ConfigItem(
		keyName = "pointsTrackerOverlayEnable",
		name = "Enable Overlay",
		description = "Show points earned within the raid.",
		position = 0,
		section = SECTION_POINTS_TRACKER
	)
	default boolean pointsTrackerOverlayEnable()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pointsTrackerShowRoomPoints",
		name = "Separate Room Points",
		description = "Show points for the current room separate from total points.",
		position = 1,
		section = SECTION_POINTS_TRACKER
	)
	default boolean pointsTrackerShowRoomPoints()
	{
		return false;
	}

	@ConfigItem(
		keyName = "pointsTrackerShowUniqueChance",
		name = "Show Unique %",
		description = "Show unique chance on the overlay.",
		position = 2,
		section = SECTION_POINTS_TRACKER
	)
	default boolean pointsTrackerShowUniqueChance()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pointsTrackerShowPetChance",
		name = "Show Pet %",
		description = "Show pet chance on the overlay.",
		position = 3,
		section = SECTION_POINTS_TRACKER
	)
	default boolean pointsTrackerShowPetChance()
	{
		return false;
	}

	@ConfigItem(
		keyName = "pointsTrackerPostRaidMessage",
		name = "Points Total Message",
		description = "Show the total points in chat after the raid, akin to the Chambers of Xeric.",
		position = 4,
		section = SECTION_POINTS_TRACKER
	)
	default boolean pointsTrackerPostRaidMessage()
	{
		return true;
	}

	// Burial Tomb

	@ConfigItem(
		keyName = "chestAudioEnable",
		name = "Purple Chest Audio",
		description = "<html>Either disables the feature or plays an audio file whenever the purple chest is opened." +
			"<br/>The custom audio file should be named toa-chest.wav inside the .runelite/tombs-of-amascut folder</html>",
		section = SECTION_BURIAL_TOMB,
		position = 0
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
		section = SECTION_BURIAL_TOMB,
		position = 1
	)
	default int chestAudioVolume()
	{
		return 100;
	}

	String SARCOPHAGUS_RECOLOR_WHITE = "sarcophagusRecolorWhite";

	@ConfigItem(
		name = "Recolour White Chest",
		description = "Recolour the white sarcophagus.",
		position = 2,
		keyName = SARCOPHAGUS_RECOLOR_WHITE,
		section = SECTION_BURIAL_TOMB
	)
	default boolean sarcophagusRecolorWhite()
	{
		return false;
	}

	String SARCOPHAGUS_WHITE_RECOLOR = "sarcophagusWhiteRecolor";

	@ConfigItem(
		name = "White Colour",
		description = "Colour to replace the white sarcophagus.",
		position = 3,
		keyName = SARCOPHAGUS_WHITE_RECOLOR,
		section = SECTION_BURIAL_TOMB
	)
	default Color sarcophagusWhiteRecolor()
	{
		return new Color(237, 177, 23);
	}

	String SARCOPHAGUS_RECOLOR_MY_PURPLE = "sarcophagusRecolorMyPurple";

	@ConfigItem(
		name = "Recolour Purple Chest (Mine)",
		description = "Recolour the purple sarcophagus." +
			"<br>When the loot is mine.",
		position = 4,
		keyName = SARCOPHAGUS_RECOLOR_MY_PURPLE,
		section = SECTION_BURIAL_TOMB
	)
	default boolean sarcophagusRecolorMyPurple()
	{
		return false;
	}

	String SARCOPHAGUS_MY_PURPLE_RECOLOR = "sarcophagusMyPurpleRecolor";

	@ConfigItem(
		name = "Purple Colour (Mine)",
		description = "Colour to replace the purple sarcophagus." +
			"<br>When the loot is mine.",
		position = 5,
		keyName = SARCOPHAGUS_MY_PURPLE_RECOLOR,
		section = SECTION_BURIAL_TOMB
	)
	default Color sarcophagusMyPurpleRecolor()
	{
		return new Color(192, 20, 124);
	}

	String SARCOPHAGUS_RECOLOR_OTHER_PURPLE = "sarcophagusRecolorOtherPurple";

	@ConfigItem(
		name = "Recolour Purple Chest (Other)",
		description = "Recolour the purple sarcophagus." +
			"<br>When the loot is NOT mine.",
		position = 6,
		keyName = SARCOPHAGUS_RECOLOR_OTHER_PURPLE,
		section = SECTION_BURIAL_TOMB
	)
	default boolean sarcophagusRecolorOtherPurple()
	{
		return false;
	}

	String SARCOPHAGUS_OTHER_PURPLE_RECOLOR = "sarcophagusOtherPurpleRecolor";

	@ConfigItem(
		name = "Purple Colour (Other)",
		description = "Colour to replace the purple sarcophagus." +
			"<br>When the loot is NOT mine.",
		position = 7,
		keyName = SARCOPHAGUS_OTHER_PURPLE_RECOLOR,
		section = SECTION_BURIAL_TOMB
	)
	default Color sarcophagusOtherPurpleRecolor()
	{
		return new Color(17, 88, 152);
	}

	@ConfigItem(
		name = "Detect Cursed Phalanx",
		description = "Prevents opening chests if player is carrying a cursed phalanx" +
			"<br>or Osmumten's fang (or).",
		position = 8,
		keyName = "cursedPhalanxDetect",
		section = SECTION_BURIAL_TOMB
	)
	default boolean cursedPhalanxDetect()
	{
		return false;
	}

	// Time Tracking

	@ConfigItem(
		keyName = "targetTimeDisplay",
		name = "Target Time in Timer",
		description = "Expand the in-raid timer to also show the target time to beat.",
		position = 0,
		section = SECTION_TIME_TRACKING
	)
	default boolean targetTimeDisplay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "splitsMessage",
		name = "Splits Post-Raid Message",
		description = "Show room splits in a chat message at the end of the raid. Path shows boss completion times, room shows each individual room (can be very long).",
		position = 1,
		section = SECTION_TIME_TRACKING
	)
	default SplitsMode splitsMessage()
	{
		return SplitsMode.OFF;
	}

	@ConfigItem(
		keyName = "splitsOverlay",
		name = "Splits Overlay",
		description = "Show room splits in an on-screen overlay. Path shows boss completion times, room shows each individual room (can be very long).",
		position = 2,
		section = SECTION_TIME_TRACKING
	)
	default SplitsMode splitsOverlay()
	{
		return SplitsMode.OFF;
	}

	// Miscellaneous

	@ConfigItem(
		keyName = "apmekenWaveHelper",
		name = "Apmeken Wave Helper",
		description = "When entering the Path of Apmeken, displays a list of the waves in the RuneLite side panel.",
		position = 801,
		section = SECTION_MISCELLANEOUS
	)
	default boolean apmekenWaveHelper()
	{
		return true;
	}

	String KEY_QUICK_PROCEED_ENABLE_MODE = "quickProceedEnableMode";

	@ConfigItem(
		keyName = KEY_QUICK_PROCEED_ENABLE_MODE,
		name = "Quick Proceed",
		description = "Left click proceed/begin/leave on Osmumten and quick-enter/quick-use entryways and teleport crystals.",
		position = 802,
		section = SECTION_MISCELLANEOUS
	)
	default QuickProceedEnableMode quickProceedEnableMode()
	{
		return QuickProceedEnableMode.ALL;
	}

	String KEY_HP_ORB_MODE = "hpOrbsMode";

	@ConfigItem(
		keyName = KEY_HP_ORB_MODE,
		name = "HP Orbs",
		description = "Removes HP orbs from the screen or replaces them with health bars.",
		position = 803,
		section = SECTION_MISCELLANEOUS
	)
	default HpOrbMode hpOrbsMode()
	{
		return HpOrbMode.ORBS;
	}

	@ConfigItem(
		keyName = "leftClickBankAll",
		name = "Bank-all Single Click",
		description = "Allows you to Bank-all loot without requiring a second click on the minimenu.",
		position = 804,
		section = SECTION_MISCELLANEOUS
	)
	default boolean leftClickBankAll()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showUpdateMessages",
		name = "Show Updates",
		description = "Opens a panel describing plugin updates after new features are added to the plugin.",
		position = 805,
		section = SECTION_MISCELLANEOUS
	)
	default boolean showUpdateMessages()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideFadeTransition",
		name = "Hide Fade Transition",
		description = "Hides the fade transition between loading zones.",
		position = 806,
		section = SECTION_MISCELLANEOUS
	)
	default boolean hideFadeTransition()
	{
		return false;
	}

	// Hidden

	@ConfigItem(
		keyName = "updateNotifierLastVersion",
		name = "",
		description = "",
		hidden = true
	)
	default int updateNotifierLastVersion()
	{
		return UpdateNotifier.TARGET_VERSION - 1;
	}

	@ConfigItem(
		keyName = "updateNotifierLastVersion",
		name = "",
		description = "",
		hidden = true
	)
	void updateNotifierLastVersion(int newVersion);

}
