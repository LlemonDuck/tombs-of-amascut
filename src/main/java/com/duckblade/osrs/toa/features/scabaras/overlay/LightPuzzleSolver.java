package com.duckblade.osrs.toa.features.scabaras.overlay;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.scabaras.ScabarasHelperMode;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LightPuzzleSolver implements PluginLifecycleComponent
{

	private static final int GROUND_OBJECT_LIGHT_BACKGROUND = 45344;
	private static final int GAME_OBJECT_LIGHT_ENABLED = 45384;

	private static final Point[] SCENE_COORD_STARTS = {
		new Point(36, 56),
		new Point(36, 44),
		new Point(53, 56),
		new Point(53, 44),
	};

	private final EventBus eventBus;
	private final Client client;

	private boolean solved;
	private boolean[] tileStates = new boolean[9]; // we'll just ignore the middle tile but keep the math simple

	@Getter
	private Set<LocalPoint> flips = Collections.emptySet();

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.scabarasHelperMode() == ScabarasHelperMode.OVERLAY &&
			raidState.getCurrentRoom() == RaidRoom.SCABARAS;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);

		solved = false;
		solve();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		if (e.getGameObject().getId() == GAME_OBJECT_LIGHT_ENABLED)
		{
			solved = false;
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned e)
	{
		if (e.getGameObject().getId() == GAME_OBJECT_LIGHT_ENABLED)
		{
			solved = false;
		}
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		if (!solved)
		{
			solve();
		}
	}

	private void solve()
	{
		solved = true;

		Tile[][] sceneTiles = client.getScene().getTiles()[client.getPlane()];
		Point tl = findStartTile(sceneTiles);
		if (tl == null)
		{
			log.warn("Failed to locate start of light puzzle");
			return;
		}

		this.tileStates = readTileStates(sceneTiles, tl);
		this.flips = findSolution(tl);
	}

	private Point findStartTile(Tile[][] sceneTiles)
	{
		for (Point sceneCoordStart : SCENE_COORD_STARTS)
		{
			Tile startTile = sceneTiles[sceneCoordStart.getX()][sceneCoordStart.getY()];
			GroundObject groundObject = startTile.getGroundObject();
			if (groundObject != null && groundObject.getId() == GROUND_OBJECT_LIGHT_BACKGROUND)
			{
				return sceneCoordStart;
			}
		}

		return null;
	}

	private boolean[] readTileStates(Tile[][] sceneTiles, Point topLeft)
	{
		boolean[] tileStates = new boolean[8];
		int ix = 0;
		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				// middle of puzzle has no light
				if (x == 1 && y == 1)
				{
					continue;
				}

				Tile lightTile = sceneTiles[topLeft.getX() + (x * 2)][topLeft.getY() - (y * 2)];
				boolean active = Arrays.stream(lightTile.getGameObjects())
					.filter(Objects::nonNull)
					.mapToInt(GameObject::getId)
					.anyMatch(id -> id == GAME_OBJECT_LIGHT_ENABLED);

				log.debug("Read light ({}, {}) as active={}", x, y, active);
				tileStates[ix++] = active;
			}
		}

		return tileStates;
	}

	private Set<LocalPoint> findSolution(Point topLeft)
	{
		// find the solution
		//noinspection OptionalGetWithoutIsPresent
		boolean[] solutionFlips = IntStream.range(0, 256)
			.mapToObj(LightPuzzleSolver::createBitField)
			.sorted(Comparator.comparingInt(LightPuzzleSolver::countFlips))
			.filter(flips -> validateSolution(tileStates, flips))
			.findFirst()
			.get();

		// convert to scene points
		Set<LocalPoint> points = new HashSet<>();
		int ix = 0;
		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				if (x == 1 && y == 1)
				{
					continue;
				}

				if (solutionFlips[ix++])
				{
					points.add(LocalPoint.fromScene(topLeft.getX() + (x * 2), topLeft.getY() - (y * 2)));
				}
			}
		}

		return points;
	}

	private static boolean[] createBitField(int i)
	{
		return new boolean[]{
			(i & 0x80) == 0x80,
			(i & 0x40) == 0x40,
			(i & 0x20) == 0x20,
			(i & 0x10) == 0x10,
			(i & 0x08) == 0x08,
			(i & 0x04) == 0x04,
			(i & 0x02) == 0x02,
			(i & 0x01) == 0x01
		};
	}

	private static int countFlips(boolean[] flips)
	{
		int c = 0;
		for (boolean flip : flips)
		{
			c += flip ? 1 : 0;
		}

		return c;
	}

	private static boolean validateSolution(boolean[] tileStates, boolean[] flips)
	{
		log.debug("Testing state {} with solution {}", tileStates, flips);
		boolean[] mutStates = Arrays.copyOf(tileStates, 8);

		// perform the flips
		for (int i = 0; i < 8; i++)
		{
			if (!flips[i])
			{
				continue;
			}

			mutStates[i] = !mutStates[i];
			switch (i)
			{
				case 0:
					mutStates[1] = !mutStates[1];
					mutStates[3] = !mutStates[3];
					break;

				case 1:
					mutStates[0] = !mutStates[0];
					mutStates[2] = !mutStates[2];
					break;

				case 2:
					mutStates[1] = !mutStates[1];
					mutStates[4] = !mutStates[4];
					break;

				case 3:
					mutStates[0] = !mutStates[0];
					mutStates[5] = !mutStates[5];
					break;

				case 4:
					mutStates[2] = !mutStates[2];
					mutStates[7] = !mutStates[7];
					break;

				case 5:
					mutStates[3] = !mutStates[3];
					mutStates[6] = !mutStates[6];
					break;

				case 6:
					mutStates[5] = !mutStates[5];
					mutStates[7] = !mutStates[7];
					break;

				case 7:
					mutStates[4] = !mutStates[4];
					mutStates[6] = !mutStates[6];
					break;
			}
		}

		// ensure all tiles are lit
		for (boolean mutState : mutStates)
		{
			if (!mutState)
			{
				return false;
			}
		}

		return true;
	}
}
