package com.duckblade.osrs.toa.features.apmeken;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VolatileBaboonCountdownTest
{

	private final Client client = mock(Client.class);
	private final VolatileBaboonCountdown overlay = new VolatileBaboonCountdown(client, mock(OverlayManager.class), mock(EventBus.class));

	@Test
	void countsDownFromObservedAnimationWithoutRestartingOnDuplicateEvents()
	{
		NPC npc = mock(NPC.class);
		when(npc.getId()).thenReturn(NpcID.TOA_PATH_APMEKEN_BABOON_ZOMBIE);
		when(npc.getAnimation()).thenReturn(AnimationID.NPC_MANDRILL_EXPLODE);
		when(client.getTickCount()).thenReturn(100);
		overlay.onAnimationChanged(animationChanged(npc));
		assertEquals(3, overlay.remainingTicks(npc));
		when(client.getTickCount()).thenReturn(101);
		overlay.onAnimationChanged(animationChanged(npc));
		assertEquals(2, overlay.remainingTicks(npc));
		when(client.getTickCount()).thenReturn(102);
		assertEquals(1, overlay.remainingTicks(npc));
		when(client.getTickCount()).thenReturn(103);
		overlay.onGameTick(new GameTick());
		assertEquals(0, overlay.remainingTicks(npc));
	}

	@Test
	void ignoresOtherNpcsAndAnimationsAndClearsOnDespawnOrShutdown()
	{
		NPC npc = mock(NPC.class);
		when(npc.getAnimation()).thenReturn(AnimationID.NPC_MANDRILL_EXPLODE);
		overlay.onAnimationChanged(animationChanged(npc));
		assertEquals(0, overlay.remainingTicks(npc));
		when(npc.getId()).thenReturn(NpcID.TOA_PATH_APMEKEN_BABOON_ZOMBIE);
		when(npc.getAnimation()).thenReturn(-1);
		overlay.onAnimationChanged(animationChanged(npc));
		assertEquals(0, overlay.remainingTicks(npc));
		when(npc.getAnimation()).thenReturn(AnimationID.NPC_MANDRILL_EXPLODE);
		overlay.onAnimationChanged(animationChanged(npc));
		overlay.onNpcDespawned(new NpcDespawned(npc));
		assertEquals(0, overlay.remainingTicks(npc));
		overlay.onAnimationChanged(animationChanged(npc));
		overlay.shutDown();
		assertEquals(0, overlay.remainingTicks(npc));
	}

	@Test
	void requiresSettingAndApmekenRoom()
	{
		TombsOfAmascutConfig config = mock(TombsOfAmascutConfig.class);
		RaidState apmeken = new RaidState(false, true, RaidRoom.APMEKEN, 1);
		assertFalse(overlay.isEnabled(config, apmeken));
		when(config.volatileBaboonTimer()).thenReturn(true);
		assertTrue(overlay.isEnabled(config, apmeken));
		assertFalse(overlay.isEnabled(config, new RaidState(false, true, RaidRoom.BABA, 1)));
	}

	private static AnimationChanged animationChanged(NPC npc)
	{
		AnimationChanged event = new AnimationChanged();
		event.setActor(npc);
		return event;
	}
}
