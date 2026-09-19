package com.duckblade.osrs.toa.features.boss;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.boss.kephri.KephriHighlights;
import com.duckblade.osrs.toa.features.boss.zebak.ZebakWaveOverlay;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.NPC;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EncounterHighlightsTest
{

	@Test
	void tracksOnlyExplosiveEggsAndClearsOnShutdown()
	{
		TombsOfAmascutConfig config = mock(TombsOfAmascutConfig.class);
		when(config.flashKephriEggs()).thenReturn(true);
		KephriHighlights overlay = new KephriHighlights(mock(OverlayManager.class), mock(EventBus.class), config);
		assertTrue(overlay.isEnabled(config, new RaidState(false, true, RaidRoom.KEPHRI, 1)));
		assertFalse(overlay.isEnabled(config, new RaidState(false, true, RaidRoom.ZEBAK, 1)));
		NPC egg = mock(NPC.class);
		NPC other = mock(NPC.class);
		when(egg.getId()).thenReturn(NpcID.KEPHRI_EGG_EXPLODE);
		overlay.onNpcSpawned(new NpcSpawned(egg));
		overlay.onNpcSpawned(new NpcSpawned(other));
		Graphics2D graphics = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			overlay.render(graphics);
			verify(egg).getConvexHull();
			verify(other, never()).getConvexHull();
			overlay.onNpcDespawned(new NpcDespawned(egg));
			overlay.render(graphics);
			verify(egg, times(1)).getConvexHull();
			overlay.onNpcSpawned(new NpcSpawned(egg));
			overlay.shutDown();
			overlay.render(graphics);
			verify(egg, times(1)).getConvexHull();
		}
		finally
		{
			graphics.dispose();
		}
	}

	@Test
	void tracksBothWaveVariantsAndClearsOnDespawn()
	{
		ZebakWaveOverlay overlay = new ZebakWaveOverlay(mock(OverlayManager.class), mock(EventBus.class));
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
