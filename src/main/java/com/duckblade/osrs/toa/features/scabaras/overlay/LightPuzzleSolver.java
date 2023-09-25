package com.duckblade.osrs.toa.features.scabaras.overlay;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.scabaras.ScabarasHelperMode;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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

	private static final int[] LIGHTS_PUZZLE_XOR_ARRAY = {
		0B01110101,
		0B10111010,
		0B11001101,
		0B11001110,
		0B01110011,
		0B10110011,
		0B01011101,
		0B10101110,
	};

	private final EventBus eventBus;
	private final Client client;

	private boolean solved;
	private int tileStates = -1; // bitmask northwest to southeast

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
			log.debug("Failed to locate start of light puzzle");
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

	private int readTileStates(Tile[][] sceneTiles, Point topLeft)
	{
		int tileStates = 0;
		for (int i = 0; i < 8; i++)
		{
			// middle of puzzle has no light
			// skip middle tile
			int tileIx = i > 3 ? i + 1 : i;
			int x = tileIx % 3;
			int y = tileIx / 3;
			Tile lightTile = sceneTiles[topLeft.getX() + (x * 2)][topLeft.getY() - (y * 2)];

			boolean active = Arrays.stream(lightTile.getGameObjects())
				.filter(Objects::nonNull)
				.mapToInt(GameObject::getId)
				.anyMatch(id -> id == GAME_OBJECT_LIGHT_ENABLED);

			log.debug("Read light ({}, {}) as active={}", x, y, active);
			if (active)
			{
				tileStates |= 1 << i;
			}
		}

		return tileStates;
	}

	private Set<LocalPoint> findSolution(Point topLeft)
	{
		int xor = 0;
		for (int i = 0; i < 8; i++)
		{
			// invert the state for xor (consider lights out as a 1)
			int mask = 1 << i;
			if ((tileStates & mask) != mask)
			{
				xor ^= LIGHTS_PUZZLE_XOR_ARRAY[i];
			}
		}

		// convert to scene points
		Set<LocalPoint> points = new HashSet<>();
		for (int i = 0; i < 8; i++)
		{
			int mask = 1 << i;
			if ((xor & mask) == mask)
			{
				// skip middle tile
				int tileIx = i > 3 ? i + 1 : i;
				int x = tileIx % 3;
				int y = tileIx / 3;
				points.add(LocalPoint.fromScene(topLeft.getX() + (x * 2), topLeft.getY() - (y * 2)));
			}
		}

		return points;
	}
}
