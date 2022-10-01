package com.duckblade.osrs.toa.features.het;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BeamTimer implements PluginLifecycleComponent
{
	private static final String CHALLENGE_START_MESSAGE = "Challenge started: Path of Het.";
	private static final String CHALLENGE_COMPLETE_MESSAGE = "Challenge complete: Path of Het.";
	private static final int CASTER_STATUE_ID = 45486;
	private static final int BEAM_HORIZONTAL_ID = 2114;
	private static final int BEAM_VERTICAL_ID = 2064;
	private static final int SHIELD_HIT_ID = 732;
	private static final int BEAM_DURATION = 2;
	private static final int BEAM_TICKS = 9;
	private static final int MINING_TICKS = 25;

	private final EventBus eventBus;
	private final OverlayManager overlayManager;
	private final Client client;

	@Inject
	private BeamTimerOverlay beamTimerOverlay;

	private int tickCounter = 0;
	private boolean challengeStartedThisTick = false;
	private boolean beamStartedThisTick = false;
	private boolean miningPhaseStartedThisTick = false;
	private int beamStartTick;
	private int nextBeamTick;

	boolean challengeComplete = false;
	GameObject casterStatue;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState currentState)
	{
		return config.beamTimer() && currentState.getCurrentRoom() == RaidRoom.HET;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		overlayManager.add(beamTimerOverlay);

		beamStartTick = Integer.MIN_VALUE;
		nextBeamTick = Integer.MAX_VALUE;

		// look for Caster Statue in case component is enabled in the challenge room
		if (casterStatue == null) {
			casterStatue = findGameObject(CASTER_STATUE_ID, client.getScene(), client.getPlane()).orElse(null);
		}
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		overlayManager.remove(beamTimerOverlay);
	}

	@Subscribe
	public void onChatMessage(ChatMessage message)
	{
		if (message.getType() == ChatMessageType.GAMEMESSAGE)
		{
			if (message.getMessage().equals(CHALLENGE_START_MESSAGE))
			{
				challengeStartedThisTick = true;
			}
			else if (message.getMessage().contains(CHALLENGE_COMPLETE_MESSAGE))
			{
				challengeComplete = true;
				log.debug("Challenge complete on tick " + tickCounter);
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();

		if (gameObject.getId() == CASTER_STATUE_ID)
		{
			casterStatue = gameObject;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (challengeComplete)
		{
			return;
		}

		tickCounter++;

		if (challengeStartedThisTick)
		{
			int challengeStartTick = tickCounter;
			nextBeamTick = challengeStartTick + BEAM_TICKS + 1;
			log.debug("Challenge started on tick " + challengeStartTick + ", next beam tick=" + nextBeamTick);
			challengeStartedThisTick = false;
		}

		if (beamStartedThisTick)
		{
			beamStartTick = tickCounter;
			nextBeamTick = beamStartTick + BEAM_TICKS;
			log.debug("Beam fired on tick " + beamStartTick + ", next beam tick=" + nextBeamTick);
			beamStartedThisTick = false;
		}

		if (miningPhaseStartedThisTick)
		{
			int miningPhaseStartTick = tickCounter;
			nextBeamTick = miningPhaseStartTick + MINING_TICKS + BEAM_TICKS;
			log.debug("Mining phase started on tick " + miningPhaseStartTick + ", next beam tick=" + nextBeamTick);
			miningPhaseStartedThisTick = false;
		}
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		GraphicsObject graphicsObject = event.getGraphicsObject();
		int id = graphicsObject.getId();

		if ((id == BEAM_HORIZONTAL_ID || id == BEAM_VERTICAL_ID) && !beamStartedThisTick)
		{
			if (tickCounter >= beamStartTick + BEAM_DURATION) // After beam starts, NOOP until beam is gone
			{
				beamStartedThisTick = true;
			}
		}

		if (id == SHIELD_HIT_ID)
		{
			miningPhaseStartedThisTick = true;
		}
	}

	double beamProgress()
	{
		int remainingTicks = nextBeamTick - tickCounter;
		if (remainingTicks >= BEAM_TICKS)
		{
			return 0.0;
		}

		return (BEAM_TICKS - remainingTicks) / (BEAM_TICKS - 1.0);
	}

	private static Optional<GameObject> findGameObject(int id, Scene scene, int plane) {
		final Tile[][][] tiles = scene.getTiles();
		for (Tile[] tileX : tiles[plane]) {
			for (Tile tile : tileX) {
				GameObject[] gameObjects = tile.getGameObjects();
				for (GameObject gameObject : gameObjects) {
					if (gameObject != null && gameObject.getId() == id) {
						return Optional.of(gameObject);
					}
				}
			}
		}
		return Optional.empty();
	}
}
