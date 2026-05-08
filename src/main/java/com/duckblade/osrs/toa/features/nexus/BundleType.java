package com.duckblade.osrs.toa.features.nexus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.InterfaceID;

@RequiredArgsConstructor
@Getter
public enum BundleType
{
	LIFE(InterfaceID.ToaMidraidLoot.SELECT_BUTTON_1),
	CHAOS(InterfaceID.ToaMidraidLoot.SELECT_BUTTON_2),
	POWER(InterfaceID.ToaMidraidLoot.SELECT_BUTTON_3);

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
