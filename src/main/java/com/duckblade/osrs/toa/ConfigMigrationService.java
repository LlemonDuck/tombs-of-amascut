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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
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
		migrateHideHpOrbs();
		migratePickaxeReminder();
		migrateScabarasMatchingDisplayMode();
		migrateDepositBoxFilterString();
	}

	@VisibleForTesting
	void migrateQuickProceedEnable()
	{
		migrate(
			"leftClickProceedEnable",
			KEY_QUICK_PROCEED_ENABLE_MODE,
			Boolean.class,
			enabled -> enabled ? QuickProceedSwaps.QuickProceedEnableMode.ALL : QuickProceedSwaps.QuickProceedEnableMode.NONE
		);
	}

	@VisibleForTesting
	void migrateHideHpOrbs()
	{
		migrate(
			"hideHpOrbs",
			KEY_HP_ORB_MODE,
			Boolean.class,
			enabled -> enabled ? HpOrbMode.HIDDEN : HpOrbMode.ORBS
		);
	}

	@SuppressWarnings("deprecation")
	@VisibleForTesting
	void migratePickaxeReminder()
	{
		migrateMany(
			"depositPickaxeMode",
			DepositPickaxeMode.class,
			mode -> ImmutableMap.of(
				KEY_HET_PICKAXE_MENU_SWAP, mode.isSwapStatue(),
				KEY_HET_PICKAXE_PREVENT_EXIT, mode.isSwapExit()
			)
		);
	}

	@SuppressWarnings("deprecation")
	@VisibleForTesting
	void migrateScabarasMatchingDisplayMode()
	{
		migrateMany(
			"scabarasMatchingDisplayMode",
			MatchingTileDisplayMode.class,
			mode -> ImmutableMap.of(
				KEY_SCABARAS_MATCHING_DISPLAY_MODE_TILE, mode == MatchingTileDisplayMode.TILE || mode == MatchingTileDisplayMode.BOTH,
				KEY_SCABARAS_MATCHING_DISPLAY_MODE_NAME, mode == MatchingTileDisplayMode.NAME || mode == MatchingTileDisplayMode.BOTH
			)
		);
	}

	@VisibleForTesting
	void migrateDepositBoxFilterString()
	{
		migrateMany(
			"depositBoxFilterString",
			String.class,
			content -> ImmutableMap.of(
				KEY_DEPOSIT_BOX_FILTER_STRING_FIRST, content,
				KEY_DEPOSIT_BOX_FILTER_STRING_SECOND, content
			)
		);
	}

	private <Source, Dest> void migrate(String oldKey, String newKey, Class<Source> sourceType, Function<Source, Dest> migration)
	{
		migrateMany(oldKey, sourceType, s -> Collections.singletonMap(newKey, migration.apply(s)));
	}

	private <Source> void migrateMany(String oldKey, Class<Source> sourceType, Function<Source, Map<String, Object>> migration)
	{
		Source previousEntry = configManager.getConfiguration(CONFIG_GROUP, oldKey, sourceType);
		if (previousEntry != null)
		{
			configManager.unsetConfiguration(CONFIG_GROUP, oldKey);
			Map<String, Object> newEntries = migration.apply(previousEntry);
			newEntries.forEach((k, v) -> configManager.setConfiguration(CONFIG_GROUP, k, v));
		}
	}

}
