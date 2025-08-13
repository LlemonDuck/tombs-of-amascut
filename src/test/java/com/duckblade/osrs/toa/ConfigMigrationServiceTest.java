package com.duckblade.osrs.toa;

import static com.duckblade.osrs.toa.TombsOfAmascutConfig.CONFIG_GROUP;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_DEPOSIT_BOX_FILTER_STRING_FIRST;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_DEPOSIT_BOX_FILTER_STRING_SECOND;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_HET_PICKAXE_MENU_SWAP;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_HET_PICKAXE_PREVENT_EXIT;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_HP_ORB_MODE;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_QUICK_PROCEED_ENABLE_MODE;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_SCABARAS_MATCHING_DISPLAY_MODE_NAME;
import static com.duckblade.osrs.toa.TombsOfAmascutConfig.KEY_SCABARAS_MATCHING_DISPLAY_MODE_TILE;
import com.duckblade.osrs.toa.features.QuickProceedSwaps;
import com.duckblade.osrs.toa.features.het.pickaxe.DepositPickaxeMode;
import com.duckblade.osrs.toa.features.hporbs.HpOrbMode;
import com.duckblade.osrs.toa.features.scabaras.overlay.MatchingTileDisplayMode;
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
		when(configManager.getConfiguration(
			CONFIG_GROUP,
			"depositPickaxeMode",
			DepositPickaxeMode.class
		)).thenReturn(DepositPickaxeMode.PREVENT_EXIT);
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

	@SuppressWarnings("deprecation")
	@Test
	void scabarasMatchingDisplayModeDisabled()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "scabarasMatchingDisplayMode", MatchingTileDisplayMode.class))
			.thenReturn(MatchingTileDisplayMode.DISABLED);
		configMigrationService.migrateScabarasMatchingDisplayMode();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "scabarasMatchingDisplayMode");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_SCABARAS_MATCHING_DISPLAY_MODE_TILE, false);
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_SCABARAS_MATCHING_DISPLAY_MODE_NAME, false);
		verifyNoMoreInteractions(configManager);
	}

	@SuppressWarnings("deprecation")
	@Test
	void scabarasMatchingDisplayModeTile()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "scabarasMatchingDisplayMode", MatchingTileDisplayMode.class))
			.thenReturn(MatchingTileDisplayMode.TILE);
		configMigrationService.migrateScabarasMatchingDisplayMode();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "scabarasMatchingDisplayMode");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_SCABARAS_MATCHING_DISPLAY_MODE_TILE, true);
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_SCABARAS_MATCHING_DISPLAY_MODE_NAME, false);
		verifyNoMoreInteractions(configManager);
	}

	@SuppressWarnings("deprecation")
	@Test
	void scabarasMatchingDisplayModeName()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "scabarasMatchingDisplayMode", MatchingTileDisplayMode.class))
			.thenReturn(MatchingTileDisplayMode.NAME);
		configMigrationService.migrateScabarasMatchingDisplayMode();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "scabarasMatchingDisplayMode");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_SCABARAS_MATCHING_DISPLAY_MODE_TILE, false);
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_SCABARAS_MATCHING_DISPLAY_MODE_NAME, true);
		verifyNoMoreInteractions(configManager);
	}

	@SuppressWarnings("deprecation")
	@Test
	void scabarasMatchingDisplayModeBoth()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "scabarasMatchingDisplayMode", MatchingTileDisplayMode.class))
			.thenReturn(MatchingTileDisplayMode.BOTH);
		configMigrationService.migrateScabarasMatchingDisplayMode();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "scabarasMatchingDisplayMode");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_SCABARAS_MATCHING_DISPLAY_MODE_TILE, true);
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_SCABARAS_MATCHING_DISPLAY_MODE_NAME, true);
		verifyNoMoreInteractions(configManager);
	}

	@SuppressWarnings("deprecation")
	@Test
	void scabarasMatchingDisplayModeNull()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "scabarasMatchingDisplayMode", MatchingTileDisplayMode.class))
			.thenReturn(null);
		configMigrationService.migrateScabarasMatchingDisplayMode();

		verifyNoMoreInteractions(configManager);
	}

	@Test
	void migrateDepositBoxFilterStringDefined()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "depositBoxFilterString", String.class))
			.thenReturn("abc123");
		configMigrationService.migrateDepositBoxFilterString();

		verify(configManager).unsetConfiguration(CONFIG_GROUP, "depositBoxFilterString");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_DEPOSIT_BOX_FILTER_STRING_FIRST, (Object) "abc123");
		verify(configManager).setConfiguration(CONFIG_GROUP, KEY_DEPOSIT_BOX_FILTER_STRING_SECOND, (Object) "abc123");
		verifyNoMoreInteractions(configManager);
	}

	@Test
	void migrateDepositBoxFilterStringNull()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, "depositBoxFilterString", String.class))
			.thenReturn(null);
		configMigrationService.migrateDepositBoxFilterString();

		verify(configManager).getConfiguration(CONFIG_GROUP, "depositBoxFilterString", String.class);
		verifyNoMoreInteractions(configManager);
	}

}