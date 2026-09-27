package com.duckblade.osrs.toa.features.boss.zebak;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class ZebakWaveOverlayTest
{

	private final ZebakWaveOverlay overlay = new ZebakWaveOverlay(mock(OverlayManager.class), mock(EventBus.class), mock(Client.class));

	@Test
	void tracksBothWaveVariantsAndClearsOnDespawn()
	{
		Graphics2D graphics = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			for (int id : new int[]{NpcID.TOA_ZEBAK_WAVE, NpcID.TOA_ZEBAK_WAVE_BLOODY})
			{
				NPC wave = mockWave(id);
				overlay.onNpcSpawned(new NpcSpawned(wave));
				overlay.render(graphics);
				verify(wave).getWorldLocation();
				overlay.onNpcDespawned(new NpcDespawned(wave));
				overlay.render(graphics);
				verify(wave, times(1)).getWorldLocation();
			}
		}
		finally
		{
			graphics.dispose();
		}
	}

	@Test
	void rendersServerLocationRatherThanRenderedLocation()
	{
		Graphics2D graphics = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			NPC wave = mockWave(NpcID.TOA_ZEBAK_WAVE);
			overlay.onNpcSpawned(new NpcSpawned(wave));
			overlay.render(graphics);

			verify(wave).getWorldLocation();
			verify(wave, never()).getCanvasTilePoly();
			verify(wave, never()).getLocalLocation();
		}
		finally
		{
			graphics.dispose();
		}
	}

	private static NPC mockWave(int id)
	{
		NPC wave = mock(NPC.class);
		when(wave.getId()).thenReturn(id);
		// outside of the (empty) mocked scene, so no canvas projection is attempted
		when(wave.getWorldLocation()).thenReturn(new WorldPoint(3400, 5200, 0));
		when(wave.getWorldView()).thenReturn(mock(WorldView.class));
		return wave;
	}
}
