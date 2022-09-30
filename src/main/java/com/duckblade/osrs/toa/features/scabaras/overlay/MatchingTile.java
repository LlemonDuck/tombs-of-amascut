package com.duckblade.osrs.toa.features.scabaras.overlay;

import java.awt.Color;
import lombok.Data;
import net.runelite.api.coords.LocalPoint;

@Data
public class MatchingTile
{

	private final LocalPoint localPoint;
	private final String name;
	private final Color color;
	private boolean matched;

}
