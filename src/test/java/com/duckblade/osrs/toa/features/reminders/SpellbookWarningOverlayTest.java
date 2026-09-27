package com.duckblade.osrs.toa.features.reminders;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.OverlayManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class SpellbookWarningOverlayTest
{
	private final Client client = mock(Client.class);
	private final TombsOfAmascutConfig config = mock(TombsOfAmascutConfig.class);
	private final SpellbookWarningOverlay overlay = new SpellbookWarningOverlay(client, config, mock(OverlayManager.class));

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
	public void daggerReminderWorksWithSpellbookReminderDisabled()
	{
		when(config.daggerWarning()).thenReturn(true);
		assertTrue(overlay.isEnabled(config, new RaidState(true, false, null, 1)));
		assertFalse(overlay.isEnabled(config, new RaidState(false, true, RaidRoom.NEXUS, 1)));
		Player player = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(player);
		Graphics2D graphics = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			overlay.render(graphics);
			verify(player).getCanvasTextLocation(eq(graphics), eq("dagger not in inventory"), anyInt());
		}
		finally
		{
			graphics.dispose();
		}
	}

	@Test
	public void visibleBankSuppressesBothReminders()
	{
		when(config.spellbookWarning()).thenReturn(true);
		Player player = mock(Player.class);
		Widget bank = mock(Widget.class);
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getWidget(InterfaceID.Bankmain.UNIVERSE)).thenReturn(bank);
		Graphics2D graphics = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			overlay.render(graphics);
			verifyNoInteractions(player);
			when(bank.isHidden()).thenReturn(true);
			overlay.render(graphics);
			verify(player).getCanvasTextLocation(eq(graphics), eq("CURRENT SPELLBOOK: STANDARD"), anyInt());
		}
		finally
		{
			graphics.dispose();
		}
	}
}
