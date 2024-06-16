package com.duckblade.osrs.toa.features.tomb;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PurpleTrackingMode
{
	OFF("Off"),
	ANY("Any Purple"),
	MINE("My Purple");

	private final String name;

	@Override
	public String toString()
	{
		return this.name;
	}
}
