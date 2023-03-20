package com.duckblade.osrs.toa.features.het.solver;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.util.Arrays;
import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectID;
import net.runelite.api.Point;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class HetSolver implements PluginLifecycleComponent
{

	private static final int STATUE_SHIELDED = ObjectID.SHIELDED_STATUE;

	private final EventBus eventBus;
	private final ClientThread clientThread;

	@Getter
	private final HetTileState[][] roomStates = new HetTileState[21][21]; // for determining solution

	@Getter
	private final GameObject[][] roomObjects = new GameObject[21][21]; // for overlay highlighting

	@Getter
	private Point puzzleBase = null; // reference point for coord checks, bottom left corner of 21x21 area inside room

	@Getter
	private HetSolutionResult result;

	@Getter
	private HetSolution solution;

	private boolean solveQueued = false; // we solve at the end of the tick
	private boolean puzzleChurned = false;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.hetSolverEnable() && raidState.getCurrentRoom() == RaidRoom.HET;
	}

	@Override
	public void startUp()
	{
		clearSolve();
		puzzleBase = null;
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		clearSolve();
		eventBus.unregister(this);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() != GameState.LOGGED_IN)
		{
			clearSolve();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getMessage().startsWith("Your party failed to complete the challenge"))
		{
			clearSolve();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		onGameObjectSpawned(e, true);
	}

	private void onGameObjectSpawned(GameObjectSpawned e, boolean canLoop)
	{
		Point p = e.getGameObject().getSceneMinLocation();
		if (e.getGameObject().getId() == STATUE_SHIELDED)
		{
			puzzleBase = new Point(p.getX() - 12, p.getY() - 9);
			log.debug("Found puzzle base at {}", puzzleBase);
			return;
		}

		if (!HetTileState.OBJECT_ID_TO_STATE.containsKey(e.getGameObject().getId()))
		{
			return;
		}

		if (puzzleBase == null)
		{
			// puzzle base event is probably queued, but this event came in first
			if (canLoop)
			{
				clientThread.invokeLater(() -> onGameObjectSpawned(e, false));
			}
			return;
		}

		// make sure we're actually inside the room boundaries
		p = normalize(p);
		if (p.getX() < 0 || p.getY() < 0 || p.getX() >= roomStates.length || p.getY() >= roomStates[0].length)
		{
			return;
		}

		// it's a relevant object, store it in the state map
		HetTileState hts = HetTileState.fromGameObject(e.getGameObject(), puzzleBase);
		roomStates[p.getX()][p.getY()] = hts;
		roomObjects[p.getX()][p.getY()] = e.getGameObject();

		if (hts.stateResetsSolution())
		{
			puzzleChurned = true;
		}

		// these events are all single-thread so no need for an atomic boolean here
		if (!solveQueued)
		{
			solveQueued = true;
			clientThread.invokeLater(this::solve);
		}
	}

	private void solve()
	{
		solveQueued = false;

		if (puzzleChurned)
		{
			// if the action was from the room spawning in, prevent gradient valley by clearing the preferential solution
			puzzleChurned = false;
			this.result = null;
			this.solution = null;
		}

		// once we've found a solution once, preferentially keep it unless another solution becomes strictly closer
		HetSolutionResult minResult = this.result != null ? this.result : new HetSolutionResult(false, 0, 0, 0, Collections.emptyList());
		HetSolution minSolution = this.solution != null ? this.solution : null;
		for (HetSolution solution : HetSolution.values())
		{
			HetSolutionResult score = solution.getScore(roomStates);
			if (score.compareTo(minResult) < 0)
			{
				minResult = score;
				minSolution = solution;
			}
		}

		if (!minResult.isCompletable())
		{
			log.debug("All solutions were eliminated");
			clearSolve();
			return;
		}

		this.result = minResult;
		this.solution = minSolution;
	}

	private void clearSolve()
	{
		solveQueued = false;
		result = null;
		solution = null;

		// wipe the board
		for (HetTileState[] tileState : roomStates)
		{
			Arrays.fill(tileState, null);
		}
		for (GameObject[] go : roomObjects)
		{
			Arrays.fill(go, null);
		}
	}

	private Point normalize(Point p)
	{
		return new Point(p.getX() - puzzleBase.getX(), p.getY() - puzzleBase.getY());
	}
}
