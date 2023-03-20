package com.duckblade.osrs.toa;

import static com.duckblade.osrs.toa.TombsOfAmascutConfig.CONFIG_GROUP;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_HP_ORB_MODE;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_QUICK_PROCEED_ENABLE_MODE;
import com.duckblade.osrs.toa.features.QuickProceedSwaps;
import com.duckblade.osrs.toa.features.hporbs.HpOrbMode;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class ConfigMigrationService
{

	@Inject
	private ConfigManager configManager;

	void migrate()
	{
		migrateQuickProceedEnable();
	}

	private void migrateQuickProceedEnable()
	{
		migrateBoolean(
			"leftClickProceedEnable",
			KEY_QUICK_PROCEED_ENABLE_MODE,
			enabled -> enabled ? QuickProceedSwaps.QuickProceedEnableMode.ALL : QuickProceedSwaps.QuickProceedEnableMode.NONE
		);
	}

	private void migrateHideHpOrbs()
	{
		migrateBoolean(
			"hideHpOrbs",
			KEY_HP_ORB_MODE,
			enabled -> enabled ? HpOrbMode.HIDDEN : HpOrbMode.ORBS
		);
	}

	private <T> void migrateBoolean(String oldKey, String newKey, Function<Boolean, T> supplier)
	{
		String previousEntry = configManager.getConfiguration(CONFIG_GROUP, oldKey);
		if (previousEntry != null)
		{
			boolean wasEnabled = Boolean.parseBoolean(previousEntry);
			configManager.setConfiguration(CONFIG_GROUP, newKey, supplier.apply(wasEnabled));
			configManager.unsetConfiguration(CONFIG_GROUP, oldKey);
		}
	}

}
