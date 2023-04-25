package com.duckblade.osrs.toa.features.scabaras;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ImmutableMap;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectID;
import net.runelite.api.Point;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
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

	private static final int OBELISK_ID = 43876; // imposter as 11698 (inactive), 11699 (active)
	private static final Map<Point, State> QUADRANT_STATES = ImmutableMap.of(
		new Point(36, 57), State.HIGHLIGHT_LOWER, // top left
		new Point(53, 57), State.HIGHLIGHT_UPPER, // top right
		new Point(36, 45), State.HIGHLIGHT_UPPER, // bottom left
		new Point(53, 45), State.HIGHLIGHT_LOWER  // bottom right
	);

	private static final int FLAME_ID = ObjectID.BARRIER_45135;
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
		eventBus.unregister(this);
		overlayManager.removeIf(o -> o instanceof SkipObeliskOverlay);
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
		checkForObelisk(e.getGameObject());
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getMessage().startsWith("Your party failed to complete the challenge"))
		{
			reset();
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

	private void checkForObelisk(GameObject obj)
	{
		if (state != State.UNKNOWN || obj.getId() != OBELISK_ID)
		{
			return;
		}

		log.debug("Found obelisk ({}) spawn at {}", obj.getId(), obj.getSceneMinLocation());
		State derivedState = QUADRANT_STATES.get(obj.getSceneMinLocation());
		if (derivedState != null)
		{
			log.debug("Determined that obelisk puzzle is avoided by {}", state);
			state = derivedState;
		}
	}
}
