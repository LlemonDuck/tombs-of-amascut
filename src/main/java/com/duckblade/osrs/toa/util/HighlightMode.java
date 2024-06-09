package com.duckblade.osrs.toa.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HighlightMode
{
	OFF("Off"),
	OUTLINE("Outline"),
	TILE("Tile");

	private final String name;

	@Override
	public String toString()
	{
		return this.name;
	}
}
