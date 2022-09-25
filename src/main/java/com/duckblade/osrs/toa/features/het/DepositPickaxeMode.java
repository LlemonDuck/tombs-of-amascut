package com.duckblade.osrs.toa.features.het;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
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
