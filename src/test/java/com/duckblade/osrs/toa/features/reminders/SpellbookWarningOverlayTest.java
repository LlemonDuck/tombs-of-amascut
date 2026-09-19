package com.duckblade.osrs.toa.features.reminders;

import net.runelite.api.gameval.ItemID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpellbookWarningOverlayTest
{
	@Test
	public void namesEverySpellbook()
	{
		assertEquals("STANDARD", SpellbookWarningOverlay.spellbookName(0));
		assertEquals("ANCIENT", SpellbookWarningOverlay.spellbookName(1));
		assertEquals("LUNAR", SpellbookWarningOverlay.spellbookName(2));
		assertEquals("ARCEUUS", SpellbookWarningOverlay.spellbookName(3));
		assertEquals("UNKNOWN", SpellbookWarningOverlay.spellbookName(99));
	}

	@Test
	public void acceptsEveryDaggerVariant()
	{
		final int[] daggers = {
			ItemID.DRAGON_DAGGER,
			ItemID.DRAGON_DAGGER_P,
			ItemID.DRAGON_DAGGER_P_,
			ItemID.DRAGON_DAGGER_P__,
			ItemID.ABYSSAL_DAGGER,
			ItemID.ABYSSAL_DAGGER_P,
			ItemID.ABYSSAL_DAGGER_P_,
			ItemID.ABYSSAL_DAGGER_P__,
			ItemID.BR_DRAGON_DAGGER,
			ItemID.BH_ABYSSAL_DAGGER_IMBUE,
			ItemID.BH_ABYSSAL_DAGGER_P_IMBUE,
			ItemID.BH_ABYSSAL_DAGGER_P__IMBUE,
			ItemID.BH_ABYSSAL_DAGGER_P___IMBUE,
			ItemID.BH_DRAGON_DAGGER_CORRUPTED,
			ItemID.BH_DRAGON_DAGGER_P_CORRUPTED,
			ItemID.BH_DRAGON_DAGGER_P__CORRUPTED,
			ItemID.BH_DRAGON_DAGGER_P___CORRUPTED
		};

		for (int dagger : daggers)
		{
			assertTrue(SpellbookWarningOverlay.isDagger(dagger));
		}
		assertFalse(SpellbookWarningOverlay.isDagger(ItemID.COINS));
	}
}
