package com.duckblade.osrs.toa.features.nexus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BundleType
{
	LIFE(50921478),
	CHAOS(50921481),
	POWER(50921484);

	private final int widgetId;

	public static BundleType byWidgetId(int widgetId)
	{
		for (BundleType type : values())
		{
			if (type.getWidgetId() == widgetId)
			{
				return type;
			}
		}
		return null;
	}

}
