package com.duckblade.osrs.toa;

import com.duckblade.osrs.toa.features.QuickProceedSwaps;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

import static com.duckblade.osrs.toa.TombsOfAmascutConfig.CONFIG_GROUP;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_QUICK_PROCEED_ENABLE_MODE;

@Singleton
public class ConfigMigrationService
{

	@Inject
	private ConfigManager configManager;

	void migrate()
	{
		migrateQuickProceedEnable();
	}

	private static final String OLD_QUICK_PROCEED_ENABLE = "leftClickProceedEnable";
	private void migrateQuickProceedEnable()
	{
		// from boolean to QuickProceedSwaps#QuickProceedEnableMode
		String previousEntry = configManager.getConfiguration(CONFIG_GROUP, OLD_QUICK_PROCEED_ENABLE);
		if (previousEntry != null)
		{
			boolean wasEnabled = Boolean.parseBoolean(previousEntry);
			configManager.setConfiguration(
				CONFIG_GROUP,
				KEY_QUICK_PROCEED_ENABLE_MODE,
				wasEnabled ? QuickProceedSwaps.QuickProceedEnableMode.ALL : QuickProceedSwaps.QuickProceedEnableMode.NONE
			);
			configManager.unsetConfiguration(CONFIG_GROUP, OLD_QUICK_PROCEED_ENABLE);
		}
	}

}
