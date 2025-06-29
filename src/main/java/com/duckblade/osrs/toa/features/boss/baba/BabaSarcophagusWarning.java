package com.duckblade.osrs.toa.features.boss.baba;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class BabaSarcophagusWarning
	extends Overlay
	implements PluginLifecycleComponent
{

	// this isn't known until we actually see a healthbar, but it should be 30
	private static final int SARCOPHAGUS_HEALTH_SCALE = 30;

	@Data
	@AllArgsConstructor
	static class SarcophagusState
	{
		LocalPoint localPoint;
		int healthRatio;
		int healthScale;
	}

	private final OverlayManager overlayManager;
	private final EventBus eventBus;

	private final Client client;
	private final TombsOfAmascutConfig config;

	private final Set<NPC> sarcophagiNpcs = new HashSet<>();
	private final Map<Integer, SarcophagusState> states = new HashMap<>();

	@Inject
	public BabaSarcophagusWarning(
		Client client,
		OverlayManager overlayManager,
		EventBus eventBus,
		TombsOfAmascutConfig config
	)
	{
		this.overlayManager = overlayManager;
		this.eventBus = eventBus;
		this.client = client;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.isInRaid() &&
			raidState.getCurrentRoom() == RaidRoom.BABA &&
			config.babaSarcophagusHighlights();
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

		// make sure we don't hold onto stale NPC references
		states.clear();
		sarcophagiNpcs.clear();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Color colourHealthy = config.babaSarcophagusHealthyColour();
		Color colourLow = config.babaSarcophagusLowColour();

		for (SarcophagusState sarcophagus : states.values())
		{
			Polygon tilePoly = Perspective.getCanvasTilePoly(client, sarcophagus.getLocalPoint());
			if (tilePoly == null) // may be off-screen
			{
				continue;
			}

			// draw the tile box in the right colour
			Color healthColor = sarcophagus.getHealthRatio() <= (sarcophagus.getHealthScale() / 2) ? colourLow : colourHealthy;
			graphics.setColor(healthColor);
			graphics.draw(tilePoly);
		}

		return null;
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		// each tick, track the current hp if it is known so that we can continue to show the right colour after it is hidden
		for (NPC sarcophagusNpc : sarcophagiNpcs)
		{
			if (sarcophagusNpc.getHealthRatio() >= 0)
			{
				states.put(
					sarcophagusNpc.getIndex(),
					new SarcophagusState(
						sarcophagusNpc.getLocalLocation(),
						sarcophagusNpc.getHealthRatio(),
						sarcophagusNpc.getHealthScale()
					)
				);
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned e)
	{
		NPC sarcophagusNpc = e.getNpc();
		if (sarcophagusNpc.getId() == NpcID.TOA_BABA_SARC_NPC)
		{
			sarcophagiNpcs.add(sarcophagusNpc);

			states.put(
				sarcophagusNpc.getIndex(),
				new SarcophagusState(
					sarcophagusNpc.getLocalLocation(),
					SARCOPHAGUS_HEALTH_SCALE, // health won't be shown until it takes damage, so we can assume full health
					SARCOPHAGUS_HEALTH_SCALE
				)
			);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned e)
	{
		sarcophagiNpcs.remove(e.getNpc());
	}
}
