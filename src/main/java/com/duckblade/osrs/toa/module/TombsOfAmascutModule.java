package com.duckblade.osrs.toa.module;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.AdrenalineCooldown;
import com.duckblade.osrs.toa.features.CameraShakeDisabler;
import com.duckblade.osrs.toa.features.DepositBoxFilter;
import com.duckblade.osrs.toa.features.DepositBoxFilterOverlay;
import com.duckblade.osrs.toa.features.FadeDisabler;
import com.duckblade.osrs.toa.features.InvocationScreenshot;
import com.duckblade.osrs.toa.features.LeftClickBankAll;
import com.duckblade.osrs.toa.features.PathLevelTracker;
import com.duckblade.osrs.toa.features.QuickProceedSwaps;
import com.duckblade.osrs.toa.features.SmellingSaltsCooldown;
import com.duckblade.osrs.toa.features.apmeken.ApmekenBaboonIndicator;
import com.duckblade.osrs.toa.features.apmeken.ApmekenBaboonIndicatorOverlay;
import com.duckblade.osrs.toa.features.apmeken.ApmekenWaveInstaller;
import com.duckblade.osrs.toa.features.boss.akkha.AkkhaShadowHealth;
import com.duckblade.osrs.toa.features.boss.akkha.AkkhaShadowHealthOverlay;
import com.duckblade.osrs.toa.features.boss.baba.BabaSarcophagusWarning;
import com.duckblade.osrs.toa.features.boss.kephri.swarmer.SwarmerDataManager;
import com.duckblade.osrs.toa.features.boss.kephri.swarmer.SwarmerOverlay;
import com.duckblade.osrs.toa.features.boss.kephri.swarmer.SwarmerPanelManager;
import com.duckblade.osrs.toa.features.het.beamtimer.BeamTimerOverlay;
import com.duckblade.osrs.toa.features.het.beamtimer.BeamTimerTracker;
import com.duckblade.osrs.toa.features.het.pickaxe.DepositPickaxeOverlay;
import com.duckblade.osrs.toa.features.het.pickaxe.DepositPickaxePreventEntry;
import com.duckblade.osrs.toa.features.het.pickaxe.DepositPickaxeSwap;
import com.duckblade.osrs.toa.features.het.solver.HetSolver;
import com.duckblade.osrs.toa.features.het.solver.HetSolverOverlay;
import com.duckblade.osrs.toa.features.hporbs.HpOrbManager;
import com.duckblade.osrs.toa.features.invocationpresets.InvocationPresetsManager;
import com.duckblade.osrs.toa.features.pointstracker.PartyPointsTracker;
import com.duckblade.osrs.toa.features.pointstracker.PointsOverlay;
import com.duckblade.osrs.toa.features.pointstracker.PointsTracker;
import com.duckblade.osrs.toa.features.pointstracker.PurpleWeightingManager;
import com.duckblade.osrs.toa.features.pointstracker.PurpleWeightingPartyBoardManager;
import com.duckblade.osrs.toa.features.scabaras.SkipObeliskOverlay;
import com.duckblade.osrs.toa.features.scabaras.overlay.AdditionPuzzleSolver;
import com.duckblade.osrs.toa.features.scabaras.overlay.LightPuzzleSolver;
import com.duckblade.osrs.toa.features.scabaras.overlay.MatchingPuzzleSolver;
import com.duckblade.osrs.toa.features.scabaras.overlay.ObeliskPuzzleSolver;
import com.duckblade.osrs.toa.features.scabaras.overlay.ScabarasOverlayManager;
import com.duckblade.osrs.toa.features.scabaras.overlay.SequencePuzzleSolver;
import com.duckblade.osrs.toa.features.scabaras.panel.ScabarasPanelManager;
import com.duckblade.osrs.toa.features.timetracking.SplitsInfoBox;
import com.duckblade.osrs.toa.features.timetracking.SplitsOverlay;
import com.duckblade.osrs.toa.features.timetracking.SplitsTracker;
import com.duckblade.osrs.toa.features.timetracking.TargetTimeManager;
import com.duckblade.osrs.toa.features.tomb.CursedPhalanxDetector;
import com.duckblade.osrs.toa.features.tomb.DryStreakTracker;
import com.duckblade.osrs.toa.features.tomb.SarcophagusOpeningSoundPlayer;
import com.duckblade.osrs.toa.features.tomb.SarcophagusRecolorer;
import com.duckblade.osrs.toa.features.updatenotifier.UpdateNotifier;
import com.duckblade.osrs.toa.util.RaidCompletionTracker;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class TombsOfAmascutModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(ComponentManager.class);
	}


	@Provides
	Set<PluginLifecycleComponent> lifecycleComponents(
		AdditionPuzzleSolver additionPuzzleSolver,
		AdrenalineCooldown adrenalineCooldown,
		AkkhaShadowHealth akkhaShadowHealth,
		AkkhaShadowHealthOverlay akkhaShadowHealthOverlay,
		ApmekenBaboonIndicator apmekenBaboonIndicator,
		ApmekenBaboonIndicatorOverlay apmekenBaboonIndicatorOverlay,
		ApmekenWaveInstaller apmekenWaveInstaller,
		BabaSarcophagusWarning babaSarcophagusWarning,
		BeamTimerOverlay beamTimerOverlay,
		BeamTimerTracker beamTimerTracker,
		CameraShakeDisabler cameraShakeDisabler,
		CursedPhalanxDetector cursedPhalanxDetector,
		DepositBoxFilter depositBoxFilter,
		DepositBoxFilterOverlay depositBoxFilterOverlay,
		DepositPickaxeOverlay depositPickaxeOverlay,
		DepositPickaxePreventEntry depositPickaxePreventEntry,
		DepositPickaxeSwap depositPickaxeSwap,
		DryStreakTracker dryStreakTracker,
		FadeDisabler fadeDisabler,
		HetSolver hetSolver,
		HetSolverOverlay hetSolverOverlay,
		HpOrbManager hpOrbManager,
		InvocationPresetsManager invocationPresetsManager,
		InvocationScreenshot invocationScreenshot,
		LeftClickBankAll leftClickBankAll,
		LightPuzzleSolver lightPuzzleSolver,
		MatchingPuzzleSolver matchingPuzzleSolver,
		ObeliskPuzzleSolver obeliskPuzzleSolver,
		PartyPointsTracker partyPointsTracker,
		PathLevelTracker pathLevelTracker,
		PointsOverlay pointsOverlay,
		PointsTracker pointsTracker,
		PurpleWeightingManager purpleWeightingManager,
		PurpleWeightingPartyBoardManager purpleWeightingPartyBoardManager,
		QuickProceedSwaps quickProceedSwaps,
		RaidCompletionTracker raidCompletionTracker,
		RaidStateTracker raidStateTracker,
		SarcophagusOpeningSoundPlayer sarcophagusOpeningSoundPlayer,
		SarcophagusRecolorer sarcophagusRecolorer,
		ScabarasOverlayManager scabarasOverlayManager,
		ScabarasPanelManager scabarasPanelManager,
		SequencePuzzleSolver sequencePuzzleSolver,
		SkipObeliskOverlay skipObeliskOverlay,
		SmellingSaltsCooldown smellingSaltsCooldown,
		SplitsInfoBox splitsInfoBox,
		SplitsOverlay splitsOverlay,
		SplitsTracker splitsTracker,
		SwarmerOverlay swarmerOverlay,
		SwarmerPanelManager swarmerPanelManager,
		SwarmerDataManager swarmerDataManager,
		TargetTimeManager targetTimeManager,
		UpdateNotifier updateNotifier
	)
	{
		return ImmutableSet.of(
			additionPuzzleSolver,
			adrenalineCooldown,
			akkhaShadowHealth,
			akkhaShadowHealthOverlay,
			apmekenBaboonIndicator,
			apmekenBaboonIndicatorOverlay,
			apmekenWaveInstaller,
			babaSarcophagusWarning,
			beamTimerOverlay,
			beamTimerTracker,
			cameraShakeDisabler,
			cursedPhalanxDetector,
			depositBoxFilter,
			depositBoxFilterOverlay,
			depositPickaxeOverlay,
			depositPickaxePreventEntry,
			depositPickaxeSwap,
			dryStreakTracker,
			fadeDisabler,
			hetSolver,
			hetSolverOverlay,
			hpOrbManager,
			invocationPresetsManager,
			invocationScreenshot,
			leftClickBankAll,
			lightPuzzleSolver,
			matchingPuzzleSolver,
			obeliskPuzzleSolver,
			partyPointsTracker,
			pathLevelTracker,
			pointsOverlay,
			pointsTracker,
			purpleWeightingManager,
			purpleWeightingPartyBoardManager,
			quickProceedSwaps,
			raidCompletionTracker,
			raidStateTracker,
			sarcophagusOpeningSoundPlayer,
			sarcophagusRecolorer,
			scabarasOverlayManager,
			scabarasPanelManager,
			sequencePuzzleSolver,
			skipObeliskOverlay,
			smellingSaltsCooldown,
			splitsInfoBox,
			splitsOverlay,
			splitsTracker,
			swarmerOverlay,
			swarmerPanelManager,
			swarmerDataManager,
			targetTimeManager,
			updateNotifier
		);
	}

	@Provides
	@Singleton
	TombsOfAmascutConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TombsOfAmascutConfig.class);
	}

}
