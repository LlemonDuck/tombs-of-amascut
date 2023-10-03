package com.duckblade.osrs.toa;

import static com.duckblade.osrs.toa.TombsOfAmascutConfig.CONFIG_GROUP;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_HET_PICKAXE_MENU_SWAP;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_HET_PICKAXE_PREVENT_EXIT;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_HP_ORB_MODE;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_QUICK_PROCEED_ENABLE_MODE;
import com.duckblade.osrs.toa.features.QuickProceedSwaps;
import com.duckblade.osrs.toa.features.het.pickaxe.DepositPickaxeMode;
import com.duckblade.osrs.toa.features.hporbs.HpOrbMode;
import net.runelite.client.config.ConfigManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfigMigrationServiceTest
{

	@Mock
	private ConfigManager configManager;

	@InjectMocks
	private ConfigMigrationService configMigrationService;

	@Test
	void quickProceedFalse()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "leftClickProceedEnable", Boolean.class)).thenReturn(false);
		configMigrationService.migrateQuickProceedEnable();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "leftClickProceedEnable");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_QUICK_PROCEED_ENABLE_MODE, QuickProceedSwaps.QuickProceedEnableMode.NONE);
		verifyNoMoreInteractions(configManager);
	}

	@Test
	void quickProceedTrue()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "leftClickProceedEnable", Boolean.class)).thenReturn(true);
		configMigrationService.migrateQuickProceedEnable();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "leftClickProceedEnable");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_QUICK_PROCEED_ENABLE_MODE, QuickProceedSwaps.QuickProceedEnableMode.ALL);
		verifyNoMoreInteractions(configManager);
	}

	@Test
	void quickProceedNull()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "leftClickProceedEnable", Boolean.class)).thenReturn(null);
		configMigrationService.migrateQuickProceedEnable();

		verifyNoMoreInteractions(configManager);
	}

	@Test
	void hideHpOrbsFalse()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "hideHpOrbs", Boolean.class)).thenReturn(false);
		configMigrationService.migrateHideHpOrbs();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "hideHpOrbs");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_HP_ORB_MODE, HpOrbMode.ORBS);
		verifyNoMoreInteractions(configManager);
	}

	@Test
	void hideHpOrbsTrue()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "hideHpOrbs", Boolean.class)).thenReturn(true);
		configMigrationService.migrateHideHpOrbs();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "hideHpOrbs");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_HP_ORB_MODE, HpOrbMode.HIDDEN);
		verifyNoMoreInteractions(configManager);
	}

	@Test
	void hideHpOrbsNull()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "hideHpOrbs", Boolean.class)).thenReturn(null);
		configMigrationService.migrateHideHpOrbs();

		verifyNoMoreInteractions(configManager);
	}

	@SuppressWarnings("deprecation")
	@Test
	void pickaxeReminderBoth()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "depositPickaxeMode", DepositPickaxeMode.class)).thenReturn(DepositPickaxeMode.BOTH);
		configMigrationService.migratePickaxeReminder();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "depositPickaxeMode");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_HET_PICKAXE_MENU_SWAP, true);
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_HET_PICKAXE_PREVENT_EXIT, true);
		verifyNoMoreInteractions(configManager);
	}

	@SuppressWarnings("deprecation")
	@Test
	void pickaxeReminderStatueSwap()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "depositPickaxeMode", DepositPickaxeMode.class)).thenReturn(DepositPickaxeMode.STATUE_SWAP);
		configMigrationService.migratePickaxeReminder();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "depositPickaxeMode");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_HET_PICKAXE_MENU_SWAP, true);
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_HET_PICKAXE_PREVENT_EXIT, false);
		verifyNoMoreInteractions(configManager);
	}

	@SuppressWarnings("deprecation")
	@Test
	void pickaxeReminderPreventExit()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "depositPickaxeMode", DepositPickaxeMode.class)).thenReturn(DepositPickaxeMode.PREVENT_EXIT);
		configMigrationService.migratePickaxeReminder();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "depositPickaxeMode");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_HET_PICKAXE_MENU_SWAP, false);
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_HET_PICKAXE_PREVENT_EXIT, true);
		verifyNoMoreInteractions(configManager);
	}

	@SuppressWarnings("deprecation")
	@Test
	void pickaxeReminderOff()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "depositPickaxeMode", DepositPickaxeMode.class)).thenReturn(DepositPickaxeMode.OFF);
		configMigrationService.migratePickaxeReminder();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "depositPickaxeMode");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_HET_PICKAXE_MENU_SWAP, false);
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_HET_PICKAXE_PREVENT_EXIT, false);
		verifyNoMoreInteractions(configManager);
	}

	@SuppressWarnings("deprecation")
	@Test
	void pickaxeReminderNull()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "depositPickaxeMode", DepositPickaxeMode.class)).thenReturn(null);
		configMigrationService.migratePickaxeReminder();

		verifyNoMoreInteractions(configManager);
	}

}