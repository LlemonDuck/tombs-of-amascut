package com.duckblade.osrs.toa.features.apmeken;

import java.awt.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Baboon
{

	BRAWLER("Brawler", new Color(169, 107, 94)),
	THROWER("Thrower", new Color(99, 114, 90)),
	MAGE("Mage", new Color(65, 134, 186)),
	SHAMAN("Shaman", new Color(164, 152, 131)),
	THRALL("Thrall", new Color(92, 99, 115)),
	VOLATILE("Volatile", new Color(118, 107, 79)),
	CURSED("Cursed", new Color(64, 149, 126)),
	;

	private final String displayName;
	private final Color color;

}
