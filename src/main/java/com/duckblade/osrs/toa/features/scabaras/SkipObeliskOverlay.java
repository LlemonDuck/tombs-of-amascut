package com.duckblade.osrs.toa.features.scabaras;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class SkipObeliskOverlay extends Overlay implements PluginLifecycleComponent
{

	public enum EnableMode
	{
		SOLO_ONLY,
		ALWAYS,
		OFF,
		;
	}

	enum State
	{
		HIGHLIGHT_UPPER,
		HIGHLIGHT_LOWER,
		UNKNOWN,
		;
	}

	private static final Set<Integer> OBELISK_IDS = ImmutableSet.of(NpcID.TOA_SCABARAS_GUESSER_OBELISK, NpcID.TOA_SCABARAS_GUESSER_OBELISK_HIT);
	private static final Map<Point, State> QUADRANT_STATES = ImmutableMap.of(
		new Point(36, 57), State.HIGHLIGHT_LOWER, // top left
		new Point(53, 57), State.HIGHLIGHT_UPPER, // top right
		new Point(36, 45), State.HIGHLIGHT_UPPER, // bottom left
		new Point(53, 45), State.HIGHLIGHT_LOWER  // bottom right
	);

	private static final int FLAME_ID = ObjectID.TOA_PATH_BARRIER;
	private static final Point FLAME_UPPER_HALF_LOC = new Point(28, 54);
	private static final Point FLAME_LOWER_HALF_LOC = new Point(28, 42);

	private final EventBus eventBus;
	private final OverlayManager overlayManager;
	private final ModelOutlineRenderer modelOutlineRenderer;

	private State state = State.UNKNOWN;
	private GameObject flameLower, flameUpper;

	private static final long BAD_RENDER_WARN_COOLDOWN = 10 * 1000;
	private long renderWarnCooldown = System.currentTimeMillis();

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		switch (config.scabarasHighlightSkipObeliskEntry())
		{
			case ALWAYS:
				return raidState.getCurrentRoom() == RaidRoom.SCABARAS;

			case SOLO_ONLY:
				return raidState.getCurrentRoom() == RaidRoom.SCABARAS &&
					raidState.getPlayerCount() == 1;

			default:
				return false;
		}
	}

	@Override
	public void startUp()
	{
		reset();
		eventBus.register(this);
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		reset();
		eventBus.unregister(this);
		overlayManager.remove(this);
	}

	private void reset()
	{
		state = State.UNKNOWN;
		flameLower = null;
		flameUpper = null;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (state == State.UNKNOWN)
		{
			return null;
		}

		GameObject toHighlight = state == State.HIGHLIGHT_LOWER ? flameLower : flameUpper;
		if (toHighlight == null && renderWarnCooldown < System.currentTimeMillis())
		{
			log.warn("Called to render with state {} but null highlight", state);
			renderWarnCooldown = System.currentTimeMillis() + BAD_RENDER_WARN_COOLDOWN;
			return null;
		}

		modelOutlineRenderer.drawOutline(toHighlight, 3, Color.green, 0);
		return null;
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		checkForFlame(e.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned e)
	{
		// in some unknown case, maybe unlocked fps only,
		// it's possible to get a render tick before the shutDown clears the references
		// so we should invalidate them early regardless if needed
		if (e.getGameObject().getId() == FLAME_ID)
		{
			flameLower = null;
			flameUpper = null;
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned e)
	{
		checkForObelisk(e.getNpc());
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getMessage().startsWith("Your party failed to complete the challenge"))
		{
			log.debug("Party failed to complete challenge, resetting state");
			state = State.UNKNOWN;
		}
	}

	private void checkForFlame(GameObject obj)
	{
		if (obj.getId() == FLAME_ID)
		{
			Point scenePoint = obj.getSceneMinLocation(); // size 1 so this works
			log.debug("Found flame spawn ({}) at {}", obj.getId(), scenePoint);
			if (FLAME_UPPER_HALF_LOC.equals(scenePoint))
			{
				flameUpper = obj;
			}
			else if (FLAME_LOWER_HALF_LOC.equals(scenePoint))
			{
				flameLower = obj;
			}
		}
	}

	private void checkForObelisk(NPC npc)
	{
		if (state != State.UNKNOWN || !OBELISK_IDS.contains(npc.getId()))
		{
			return;
		}

		Point p = new Point(npc.getLocalLocation().getSceneX(), npc.getLocalLocation().getSceneY());
		log.debug("Found obelisk ({}) spawn at {}", npc.getId(), p);
		State derivedState = QUADRANT_STATES.getOrDefault(p, State.UNKNOWN);
		if (derivedState != State.UNKNOWN)
		{
			state = derivedState;
			log.debug("Determined that obelisk puzzle is avoided by {}", state);
		}
	}
}
