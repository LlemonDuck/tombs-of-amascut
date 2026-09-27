package com.duckblade.osrs.toa.features.reminders;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.util.RaidState;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.ui.overlay.OverlayManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToaInventoryOverlayTest
{
	private final Client client = mock(Client.class);
	private final TombsOfAmascutConfig config = mock(TombsOfAmascutConfig.class);
	private final ToaInventoryOverlay overlay = new ToaInventoryOverlay(client, config, mock(OverlayManager.class));

	private void player(int prayerPoints, int specEnergy)
	{
		when(client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(prayerPoints);
		when(client.getVarpValue(VarPlayerID.SA_ENERGY)).thenReturn(specEnergy);
	}

	@Test
	public void kerisNeedsBothPrayerAndSpec()
	{
		when(config.flashKeris()).thenReturn(true);

		player(50, 750);
		assertEquals(ToaInventoryOverlay.READY_COLOR, overlay.highlightColor(ItemID.KERIS_PARTISAN_SUN));

		player(49, 1000);
		assertEquals(ToaInventoryOverlay.NOT_READY_COLOR, overlay.highlightColor(ItemID.KERIS_PARTISAN_SUN));

		player(99, 749);
		assertEquals(ToaInventoryOverlay.NOT_READY_COLOR, overlay.highlightColor(ItemID.KERIS_PARTISAN_SUN));

		player(49, 749);
		assertEquals(ToaInventoryOverlay.NOT_READY_COLOR, overlay.highlightColor(ItemID.KERIS_PARTISAN_SUN));
	}

	@Test
	public void bothAmbrosiaDosesAreReadyRegardlessOfPlayerState()
	{
		when(config.flashAmbrosia()).thenReturn(true);
		player(1, 0);

		assertEquals(ToaInventoryOverlay.READY_COLOR, overlay.highlightColor(ItemID.TOA_SUPPLY_PANICHEAL_1));
		assertEquals(ToaInventoryOverlay.READY_COLOR, overlay.highlightColor(ItemID.TOA_SUPPLY_PANICHEAL_2));
	}

	@Test
	public void disabledRemindersAndUnrelatedItemsAreIgnored()
	{
		player(99, 1000);

		when(config.flashKeris()).thenReturn(true);
		when(config.flashAmbrosia()).thenReturn(false);
		assertNull(overlay.highlightColor(ItemID.TOA_SUPPLY_PANICHEAL_1));
		assertNull(overlay.highlightColor(ItemID.TOA_SUPPLY_PANICHEAL_2));
		assertNull(overlay.highlightColor(ItemID.COINS));

		when(config.flashKeris()).thenReturn(false);
		when(config.flashAmbrosia()).thenReturn(true);
		assertNull(overlay.highlightColor(ItemID.KERIS_PARTISAN_SUN));
	}

	@Test
	public void onlyEnabledInsideTheRaid()
	{
		when(config.flashKeris()).thenReturn(true);

		assertTrue(overlay.isEnabled(config, new RaidState(false, true, null, 1)));
		assertFalse(overlay.isEnabled(config, new RaidState(true, false, null, 1)));
		assertFalse(overlay.isEnabled(config, new RaidState(false, false, null, 1)));
	}

	@Test
	public void disabledWhenBothRemindersAreOff()
	{
		when(config.flashKeris()).thenReturn(false);
		when(config.flashAmbrosia()).thenReturn(false);

		assertFalse(overlay.isEnabled(config, new RaidState(false, true, null, 1)));
	}
}
