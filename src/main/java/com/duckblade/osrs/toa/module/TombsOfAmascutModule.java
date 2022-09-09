package com.duckblade.osrs.toa.module;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.DepositPickaxeSwap;
import com.duckblade.osrs.toa.features.InvocationScreenshot;
import com.duckblade.osrs.toa.features.QuickProceedSwaps;
import com.duckblade.osrs.toa.features.SarcophagusOpeningSoundPlayer;
import com.duckblade.osrs.toa.features.apmeken.ApmekenWaveInstaller;
import com.duckblade.osrs.toa.features.invocationpresets.InvocationPresetsManager;
import com.duckblade.osrs.toa.features.scabaras.ScabarasHelperInstaller;
import com.duckblade.osrs.toa.features.targettime.TargetTimeManager;
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
		lifecycleComponents.addBinding().to(ApmekenWaveInstaller.class);
		lifecycleComponents.addBinding().to(DepositPickaxeSwap.class);
		lifecycleComponents.addBinding().to(InvocationPresetsManager.class);
		lifecycleComponents.addBinding().to(InvocationScreenshot.class);
		lifecycleComponents.addBinding().to(QuickProceedSwaps.class);
		lifecycleComponents.addBinding().to(RaidStateTracker.class);
		lifecycleComponents.addBinding().to(SarcophagusOpeningSoundPlayer.class);
		lifecycleComponents.addBinding().to(ScabarasHelperInstaller.class);
		lifecycleComponents.addBinding().to(TargetTimeManager.class);
	}

	@Provides
	@Singleton
	TombsOfAmascutConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TombsOfAmascutConfig.class);
	}

}
