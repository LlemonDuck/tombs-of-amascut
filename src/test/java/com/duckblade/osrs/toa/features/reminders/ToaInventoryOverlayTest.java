package com.duckblade.osrs.toa.features.reminders;

import java.awt.Color;
import net.runelite.api.gameval.ItemID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ToaInventoryOverlayTest
{
	@Test
	public void kerisUsesPrayerThreshold()
	{
		assertEquals(new Color(0, 255, 80), ToaInventoryOverlay.highlightColor(ItemID.KERIS_PARTISAN_SUN, 51, true, true));
		assertEquals(new Color(255, 40, 40), ToaInventoryOverlay.highlightColor(ItemID.KERIS_PARTISAN_SUN, 50, true, true));
	}

	@Test
	public void bothAmbrosiaDosesAreGreen()
	{
		assertEquals(new Color(0, 255, 80), ToaInventoryOverlay.highlightColor(ItemID.TOA_SUPPLY_PANICHEAL_1, 1, true, true));
		assertEquals(new Color(0, 255, 80), ToaInventoryOverlay.highlightColor(ItemID.TOA_SUPPLY_PANICHEAL_2, 1, true, true));
	}

	@Test
	public void unrelatedItemsAreIgnored()
	{
		assertNull(ToaInventoryOverlay.highlightColor(ItemID.COINS, 99, true, true));
		assertNull(ToaInventoryOverlay.highlightColor(ItemID.KERIS_PARTISAN_SUN, 99, false, true));
		assertNull(ToaInventoryOverlay.highlightColor(ItemID.TOA_SUPPLY_PANICHEAL_1, 99, true, false));
	}
}
