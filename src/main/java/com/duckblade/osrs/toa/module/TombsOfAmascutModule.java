package com.duckblade.osrs.toa.module;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.InvocationScreenshot;
import com.duckblade.osrs.toa.features.LeftClickBankAll;
import com.duckblade.osrs.toa.features.het.pickaxe.DepositPickaxeOverlay;
import com.duckblade.osrs.toa.features.het.pickaxe.DepositPickaxePreventEntry;
import com.duckblade.osrs.toa.features.het.solver.HetSolver;
import com.duckblade.osrs.toa.features.het.solver.HetSolverOverlay;
import com.duckblade.osrs.toa.features.hporbs.HpOrbManager;
import com.duckblade.osrs.toa.features.QuickProceedSwaps;
import com.duckblade.osrs.toa.features.apmeken.ApmekenWaveInstaller;
import com.duckblade.osrs.toa.features.het.beamtimer.BeamTimerOverlay;
import com.duckblade.osrs.toa.features.het.beamtimer.BeamTimerTracker;
import com.duckblade.osrs.toa.features.het.pickaxe.DepositPickaxeSwap;
import com.duckblade.osrs.toa.features.invocationpresets.InvocationPresetsManager;
import com.duckblade.osrs.toa.features.pointstracker.PartyPointsTracker;
import com.duckblade.osrs.toa.features.pointstracker.PointsOverlay;
import com.duckblade.osrs.toa.features.pointstracker.PointsTracker;
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
import com.duckblade.osrs.toa.features.tomb.SarcophagusRecolorer;
import com.duckblade.osrs.toa.features.tomb.SarcophagusOpeningSoundPlayer;
import com.duckblade.osrs.toa.features.updatenotifier.UpdateNotifier;
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
		lifecycleComponents.addBinding().to(ApmekenWaveInstaller.class);
		lifecycleComponents.addBinding().to(BeamTimerOverlay.class);
		lifecycleComponents.addBinding().to(BeamTimerTracker.class);
		lifecycleComponents.addBinding().to(DepositPickaxeOverlay.class);
		lifecycleComponents.addBinding().to(DepositPickaxePreventEntry.class);
		lifecycleComponents.addBinding().to(DepositPickaxeSwap.class);
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
		lifecycleComponents.addBinding().to(PointsOverlay.class);
		lifecycleComponents.addBinding().to(PointsTracker.class);
		lifecycleComponents.addBinding().to(QuickProceedSwaps.class);
		lifecycleComponents.addBinding().to(RaidStateTracker.class);
		lifecycleComponents.addBinding().to(SarcophagusOpeningSoundPlayer.class);
		lifecycleComponents.addBinding().to(ScabarasOverlayManager.class);
		lifecycleComponents.addBinding().to(ScabarasPanelManager.class);
		lifecycleComponents.addBinding().to(SequencePuzzleSolver.class);
		lifecycleComponents.addBinding().to(SkipObeliskOverlay.class);
		lifecycleComponents.addBinding().to(SplitsOverlay.class);
		lifecycleComponents.addBinding().to(SplitsTracker.class);
		lifecycleComponents.addBinding().to(TargetTimeManager.class);
		lifecycleComponents.addBinding().to(UpdateNotifier.class);
		lifecycleComponents.addBinding().to(SarcophagusRecolorer.class);
	}

	@Provides
	@Singleton
	TombsOfAmascutConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TombsOfAmascutConfig.class);
	}

}
