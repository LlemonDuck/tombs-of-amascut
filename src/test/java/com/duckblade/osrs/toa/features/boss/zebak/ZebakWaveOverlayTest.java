package com.duckblade.osrs.toa.features.boss.zebak;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.NPC;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class ZebakWaveOverlayTest
{

	private final ZebakWaveOverlay overlay = new ZebakWaveOverlay(mock(OverlayManager.class), mock(EventBus.class));

	@Test
	void tracksBothWaveVariantsAndClearsOnDespawn()
	{
		Graphics2D graphics = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			for (int id : new int[]{NpcID.TOA_ZEBAK_WAVE, NpcID.TOA_ZEBAK_WAVE_BLOODY})
			{
				NPC wave = mock(NPC.class);
				when(wave.getId()).thenReturn(id);
				overlay.onNpcSpawned(new NpcSpawned(wave));
				overlay.render(graphics);
				verify(wave).getCanvasTilePoly();
				overlay.onNpcDespawned(new NpcDespawned(wave));
				overlay.render(graphics);
				verify(wave, times(1)).getCanvasTilePoly();
			}
		}
		finally
		{
			graphics.dispose();
		}
	}
}
