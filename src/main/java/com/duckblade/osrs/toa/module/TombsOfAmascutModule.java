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
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Slf4j
public class TombsOfAmascutModule extends AbstractModule
{

	@Override
	protected void configure()
	{
		Multibinder<PluginLifecycleComponent> lifecycleComponents = Multibinder.newSetBinder(binder(), PluginLifecycleComponent.class);
		lifecycleComponents.addBinding().to(AdditionPuzzleSolver.class);
		lifecycleComponents.addBinding().to(AdrenalineCooldown.class);
		lifecycleComponents.addBinding().to(AkkhaShadowHealth.class);
		lifecycleComponents.addBinding().to(AkkhaShadowHealthOverlay.class);
		lifecycleComponents.addBinding().to(ApmekenBaboonIndicator.class);
		lifecycleComponents.addBinding().to(ApmekenBaboonIndicatorOverlay.class);
		lifecycleComponents.addBinding().to(ApmekenWaveInstaller.class);
		lifecycleComponents.addBinding().to(BabaSarcophagusWarning.class);
		lifecycleComponents.addBinding().to(BeamTimerOverlay.class);
		lifecycleComponents.addBinding().to(BeamTimerTracker.class);
		lifecycleComponents.addBinding().to(CameraShakeDisabler.class);
		lifecycleComponents.addBinding().to(CursedPhalanxDetector.class);
		lifecycleComponents.addBinding().to(DepositBoxFilter.class);
		lifecycleComponents.addBinding().to(DepositBoxFilterOverlay.class);
		lifecycleComponents.addBinding().to(DepositPickaxeOverlay.class);
		lifecycleComponents.addBinding().to(DepositPickaxePreventEntry.class);
		lifecycleComponents.addBinding().to(DepositPickaxeSwap.class);
		lifecycleComponents.addBinding().to(DryStreakTracker.class);
		lifecycleComponents.addBinding().to(FadeDisabler.class);
		lifecycleComponents.addBinding().to(HetSolver.class);
		lifecycleComponents.addBinding().to(HetSolverOverlay.class);
		lifecycleComponents.addBinding().to(HpOrbManager.class);
		lifecycleComponents.addBinding().to(InvocationPresetsManager.class);
		lifecycleComponents.addBinding().to(InvocationScreenshot.class);
		lifecycleComponents.addBinding().to(LeftClickBankAll.class);
		lifecycleComponents.addBinding().to(LightPuzzleSolver.class);
		lifecycleComponents.addBinding().to(MatchingPuzzleSolver.class);
		lifecycleComponents.addBinding().to(ObeliskPuzzleSolver.class);
		lifecycleComponents.addBinding().to(PartyPointsTracker.class);
		lifecycleComponents.addBinding().to(PathLevelTracker.class);
		lifecycleComponents.addBinding().to(PointsOverlay.class);
		lifecycleComponents.addBinding().to(PointsTracker.class);
		lifecycleComponents.addBinding().to(PurpleWeightingManager.class);
		lifecycleComponents.addBinding().to(PurpleWeightingPartyBoardManager.class);
		lifecycleComponents.addBinding().to(QuickProceedSwaps.class);
		lifecycleComponents.addBinding().to(RaidCompletionTracker.class);
		lifecycleComponents.addBinding().to(RaidStateTracker.class);
		lifecycleComponents.addBinding().to(SarcophagusOpeningSoundPlayer.class);
		lifecycleComponents.addBinding().to(SarcophagusRecolorer.class);
		lifecycleComponents.addBinding().to(ScabarasOverlayManager.class);
		lifecycleComponents.addBinding().to(ScabarasPanelManager.class);
		lifecycleComponents.addBinding().to(SequencePuzzleSolver.class);
		lifecycleComponents.addBinding().to(SkipObeliskOverlay.class);
		lifecycleComponents.addBinding().to(SmellingSaltsCooldown.class);
		lifecycleComponents.addBinding().to(SplitsOverlay.class);
		lifecycleComponents.addBinding().to(SplitsTracker.class);
		lifecycleComponents.addBinding().to(SwarmerOverlay.class);
		lifecycleComponents.addBinding().to(SwarmerPanelManager.class);
		lifecycleComponents.addBinding().to(SwarmerDataManager.class);
		lifecycleComponents.addBinding().to(TargetTimeManager.class);
		lifecycleComponents.addBinding().to(UpdateNotifier.class);
	}

	@Provides
	@Singleton
	TombsOfAmascutConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TombsOfAmascutConfig.class);
	}

}
