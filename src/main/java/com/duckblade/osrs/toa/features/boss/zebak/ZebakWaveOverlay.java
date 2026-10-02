package com.duckblade.osrs.toa.features.boss.zebak;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class ZebakWaveOverlay
	extends Overlay
	implements PluginLifecycleComponent
{

	private final Client client;
	private final Set<NPC> waves = new HashSet<>();

	@Inject
	public ZebakWaveOverlay(Client client)
	{
		this.client = client;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.getCurrentRoom() == RaidRoom.ZEBAK && config.highlightZebakWaves();
	}

	@Override
	public void shutDown()
	{
		waves.clear();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		int id = event.getNpc().getId();
		if (id == NpcID.TOA_ZEBAK_WAVE || id == NpcID.TOA_ZEBAK_WAVE_BLOODY)
		{
			waves.add(event.getNpc());
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		waves.remove(event.getNpc());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (NPC wave : waves)
		{
			// server-side tile, which leads the rendered position by up to a tick
			LocalPoint trueTile = LocalPoint.fromWorld(wave.getWorldView(), wave.getWorldLocation());
			if (trueTile == null)
			{
				continue;
			}

			Polygon tile = Perspective.getCanvasTilePoly(client, trueTile);
			if (tile != null)
			{
				OverlayUtil.renderPolygon(graphics, tile, Color.CYAN, new Color(0, 180, 255, 55), new BasicStroke(4));
			}
		}
		return null;
	}
}
