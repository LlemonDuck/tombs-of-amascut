package com.duckblade.osrs.toa.features.apmeken;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class VolatileBaboonCountdown extends Overlay implements PluginLifecycleComponent
{

	private final Client client;
	private final OverlayManager overlayManager;
	private final EventBus eventBus;
	private final Map<NPC, Integer> explosionTicks = new HashMap<>();

	@Inject
	public VolatileBaboonCountdown(Client client, OverlayManager overlayManager, EventBus eventBus)
	{
		this.client = client;
		this.overlayManager = overlayManager;
		this.eventBus = eventBus;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.getCurrentRoom() == RaidRoom.APMEKEN && config.volatileBaboonTimer();
	}

	@Override
	public void startUp()
	{
		overlayManager.add(this);
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(this);
		eventBus.unregister(this);
		explosionTicks.clear();
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() instanceof NPC)
		{
			NPC npc = (NPC) event.getActor();
			if (npc.getId() == NpcID.TOA_PATH_APMEKEN_BABOON_ZOMBIE && npc.getAnimation() == AnimationID.NPC_MANDRILL_EXPLODE)
			{
				explosionTicks.putIfAbsent(npc, client.getTickCount() + 3);
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		explosionTicks.remove(event.getNpc());
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		explosionTicks.values().removeIf(tick -> tick <= client.getTickCount());
	}

	int remainingTicks(NPC npc)
	{
		return Math.max(0, explosionTicks.getOrDefault(npc, client.getTickCount()) - client.getTickCount());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (NPC npc : explosionTicks.keySet())
		{
			int remaining = remainingTicks(npc);
			if (remaining > 0)
			{
				OverlayUtil.renderActorOverlay(graphics, npc, Integer.toString(remaining), new Color(255, 70, 70));
			}
		}
		return null;
	}
}
