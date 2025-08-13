package com.duckblade.osrs.toa.features.het.pickaxe;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
/**
 * Deprecated: Former config option that has since been converted to separate booleans.
 * Remains in codebase for config migration.
 */
public enum DepositPickaxeMode
{

	OFF(false, false),
	STATUE_SWAP(true, false),
	PREVENT_EXIT(false, true),
	BOTH(true, true),
	;

	private final boolean swapStatue;
	private final boolean swapExit;

}
