package com.duckblade.osrs.toa.features.apmeken;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Baboon
{

	BRAWLER("Brawler", new Color(169, 107, 94), Color.RED),
	THROWER("Thrower", new Color(99, 114, 90), Color.GREEN),
	MAGE("Mage", new Color(65, 134, 186), Color.BLUE),
	SHAMAN("Shaman", new Color(164, 152, 131), Color.CYAN),
	THRALL("Thrall", new Color(92, 99, 115), new Color(92, 99, 115)),
	VOLATILE("Volatile", new Color(118, 107, 79), Color.YELLOW),
	CURSED("Cursed", new Color(64, 149, 126), new Color(59, 131, 40)),
	SPECIAL("Special", new Color(211, 201, 134), Color.WHITE)
	;

	static final List<Map<Baboon, Integer>> WAVES = ImmutableList.of(
		ImmutableMap.of(BRAWLER, 2, SHAMAN, 1), // 1
		ImmutableMap.of(THROWER, 2, VOLATILE, 1), // 2
		ImmutableMap.of(MAGE, 2, CURSED, 1), // 3
		ImmutableMap.of(THROWER, 2, SPECIAL, 3), // 4
		ImmutableMap.of(MAGE, 2, SHAMAN, 1, SPECIAL, 2), // 5
		ImmutableMap.of(BRAWLER, 2, SHAMAN, 1, SPECIAL, 2), // 6
		ImmutableMap.of(BRAWLER, 1, THROWER, 1, SHAMAN, 1, CURSED, 2), // 7
		ImmutableMap.of(SHAMAN, 1, VOLATILE, 2, SPECIAL, 2) // 8
	);

	private final String displayName;
	private final Color sidePanelColor;
	private final Color overlayColor;

}
