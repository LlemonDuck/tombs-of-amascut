/*
 * Copyright (c) 2026, stadtehrsoftware
 * All rights reserved.
 * BSD 2-Clause License; see LICENSE.
 */
package com.duckblade.osrs.toa.features.boss.kephri;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.NPC;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class KephriHighlights extends Overlay implements PluginLifecycleComponent
{

	private final OverlayManager overlayManager;
	private final EventBus eventBus;
	private final TombsOfAmascutConfig config;
	private final Set<NPC> npcs = new HashSet<>();

	@Inject
	public KephriHighlights(OverlayManager overlayManager, EventBus eventBus, TombsOfAmascutConfig config)
	{
		this.overlayManager = overlayManager;
		this.eventBus = eventBus;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.getCurrentRoom() == RaidRoom.KEPHRI && (config.flashKephriEggs() || config.arcaneScarabWarning());
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
		npcs.clear();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		int id = event.getNpc().getId();
		if (id == NpcID.KEPHRI_EGG_EXPLODE || id == NpcID.TOA_KEPHRI_GUARDIAN_MAGE)
		{
			npcs.add(event.getNpc());
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		npcs.remove(event.getNpc());
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		long now = System.currentTimeMillis();
		for (NPC npc : npcs)
		{
			boolean egg = npc.getId() == NpcID.KEPHRI_EGG_EXPLODE;
			if ((egg && !config.flashKephriEggs()) || (!egg && !config.arcaneScarabWarning()))
			{
				continue;
			}
			Color outline = egg ? Color.getHSBColor((now % 1800L) / 1800f, 1f, 1f) : new Color(255, 0, 70);
			int alpha = (int) (35 + 80 * ((Math.sin((now % 800L) / 800.0 * Math.PI * 2) + 1) / 2));
			Shape shape = egg ? npc.getConvexHull() : npc.getCanvasTilePoly();
			if (shape != null)
			{
				OverlayUtil.renderPolygon(graphics, shape, outline,
					new Color(outline.getRed(), outline.getGreen(), outline.getBlue(), alpha), new BasicStroke(3));
			}
			if (!egg)
			{
				OverlayUtil.renderActorOverlay(graphics, npc, "KILL ARCANE SCARAB", outline);
			}
		}
		return null;
	}
}
