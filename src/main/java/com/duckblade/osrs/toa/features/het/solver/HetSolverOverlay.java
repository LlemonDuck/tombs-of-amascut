package com.duckblade.osrs.toa.features.het.solver;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.PolygonUtil;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import static net.runelite.api.Perspective.LOCAL_TILE_SIZE;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ColorUtil;

@Singleton
public class HetSolverOverlay extends Overlay implements PluginLifecycleComponent
{

	private static final Polygon PREMOVE_ARROW = new Polygon(
		new int[]{
			+(LOCAL_TILE_SIZE / 8), // box top right
			+(LOCAL_TILE_SIZE / 8), // box bottom right
			-(LOCAL_TILE_SIZE / 8), // box bottom left
			-(LOCAL_TILE_SIZE / 8), // box top left
			-(5 * LOCAL_TILE_SIZE / 16), // head left
			0, // head top
			+(5 * LOCAL_TILE_SIZE / 16), // head right
		},
		new int[]{
			0,
			-(LOCAL_TILE_SIZE / 4),
			-(LOCAL_TILE_SIZE / 4),
			0,
			0,
			+(5 * LOCAL_TILE_SIZE / 16),
			0,
		},
		7
	);

	// just a single line
	private static final Polygon PREMOVE_COMPACT = new Polygon(
		new int[]{
			+(LOCAL_TILE_SIZE / 2), // top left
			-(LOCAL_TILE_SIZE / 2), // top right
			0, // center
		},
		new int[]{
			+(LOCAL_TILE_SIZE / 2),
			+(LOCAL_TILE_SIZE / 2),
			+(3 * LOCAL_TILE_SIZE / 4),
		},
		3
	);

	public enum PremoveMode
	{
		OFF,
		ARROW,
		COMPACT,
	}

	private final OverlayManager overlayManager;
	private final Client client;
	private final HetSolver hetSolver;

	private PremoveMode premoveMode;

	@Inject
	public HetSolverOverlay(OverlayManager overlayManager, Client client, HetSolver hetSolver)
	{
		this.overlayManager = overlayManager;
		this.client = client;
		this.hetSolver = hetSolver;

		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		this.premoveMode = config.hetSolverPremoveMode();

		return config.hetSolverEnable() && raidState.getCurrentRoom() == RaidRoom.HET;
	}

	@Override
	public void startUp()
	{
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(this);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		HetSolution sol = hetSolver.getSolution();
		if (sol == null)
		{
			return null;
		}

		// re-evaluate in case the user has caused the solution to now fail, in which case the plugin won't update the solution
		HetSolutionResult res = sol.getScore(hetSolver.getRoomStates());
		for (HetTileState incorrectState : res.getIncorrectStates())
		{
			if (incorrectState.getState() == HetTileState.STATE_EMPTY)
			{
				// highlight whatever object needs to be removed
				GameObject highlightObj = hetSolver.getRoomObjects()[incorrectState.getX()][incorrectState.getY()];
				Shape hull;
				if (highlightObj != null && (hull = highlightObj.getConvexHull()) != null)
				{
					OverlayUtil.renderPolygon(g, hull, Color.red);
				}
				continue;
			}

			if (incorrectState.getState() == HetTileState.STATE_MIRROR_MOVABLE)
			{
				// highlight the tile that needs a mirror
				int sceneX = incorrectState.getX() + hetSolver.getPuzzleBase().getX();
				int sceneY = incorrectState.getY() + hetSolver.getPuzzleBase().getY();
				Polygon tile = Perspective.getCanvasTilePoly(client, LocalPoint.fromScene(sceneX, sceneY));
				if (tile != null)
				{
					OverlayUtil.renderPolygon(g, tile, Color.red);

					Point sw = new Point(tile.xpoints[0], tile.ypoints[0]);
					Point se = new Point(tile.xpoints[1], tile.ypoints[1]);
					Point ne = new Point(tile.xpoints[2], tile.ypoints[2]);
					Point nw = new Point(tile.xpoints[3], tile.ypoints[3]);

					Polygon tri = new Polygon();
					switch (incorrectState.getOrientation())
					{
						case HetTileState.ORIENTATION_NORTH_EAST:
							tri.addPoint(nw.x, nw.y);
							tri.addPoint(ne.x, ne.y);
							tri.addPoint(se.x, se.y);
							break;

						case HetTileState.ORIENTATION_SOUTH_EAST:
							tri.addPoint(sw.x, sw.y);
							tri.addPoint(se.x, se.y);
							tri.addPoint(ne.x, ne.y);
							break;

						case HetTileState.ORIENTATION_SOUTH_WEST:
							tri.addPoint(nw.x, nw.y);
							tri.addPoint(sw.x, sw.y);
							tri.addPoint(se.x, se.y);
							break;

						default:
							tri.addPoint(sw.x, sw.y);
							tri.addPoint(nw.x, nw.y);
							tri.addPoint(ne.x, ne.y);
							break;
					}
					g.setColor(Color.red);
					g.fill(tri);
				}

				if (premoveMode != PremoveMode.OFF)
				{
					renderPreMoveArrow(g, incorrectState);
				}
			}
		}

		return null;
	}

	private void renderPreMoveArrow(Graphics2D g, HetTileState incorrectState)
	{
		int mirrorX = incorrectState.getX() + hetSolver.getPuzzleBase().getX();
		int mirrorY = incorrectState.getY() + hetSolver.getPuzzleBase().getY();
		int preMoveX = mirrorX;
		int preMoveY = mirrorY;

		int badMirrorFlags = CollisionDataFlag.BLOCK_MOVEMENT_FLOOR |
			CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION |
			CollisionDataFlag.BLOCK_MOVEMENT_FULL;
		int badPreFlags = badMirrorFlags;

		LocalPoint centerTile;
		double angle;
		switch (incorrectState.getOrientation())
		{
			case HetTileState.ORIENTATION_NORTH_EAST:
				// pre-move from south
				preMoveY -= 1;
				angle = 0;
				badMirrorFlags |= CollisionDataFlag.BLOCK_MOVEMENT_SOUTH;
				badPreFlags |= CollisionDataFlag.BLOCK_MOVEMENT_NORTH;
				break;

			case HetTileState.ORIENTATION_SOUTH_EAST:
				// pre-move from west
				preMoveX -= 1;
				angle = 270;
				badMirrorFlags |= CollisionDataFlag.BLOCK_MOVEMENT_WEST;
				badPreFlags |= CollisionDataFlag.BLOCK_MOVEMENT_EAST;
				break;

			case HetTileState.ORIENTATION_SOUTH_WEST:
				// pre-move from north
				preMoveY += 1;
				angle = 180;
				badMirrorFlags |= CollisionDataFlag.BLOCK_MOVEMENT_NORTH;
				badPreFlags |= CollisionDataFlag.BLOCK_MOVEMENT_SOUTH;
				break;

			case HetTileState.ORIENTATION_NORTH_WEST:
			default:
				// pre-move from east
				preMoveX += 1;
				angle = 90;
				badMirrorFlags |= CollisionDataFlag.BLOCK_MOVEMENT_EAST;
				badPreFlags |= CollisionDataFlag.BLOCK_MOVEMENT_WEST;
				break;
		}

		// dim the pre-move if impossible
		g.setColor(new Color(0x2CA82C));
		WorldView wv = client.getLocalPlayer().getWorldView();
		CollisionData[] collisionMaps = wv.getCollisionMaps();
		CollisionData collisionData;
		// if either are null something has changed in the api layer, let's just assume we can pre-move
		if (collisionMaps != null && (collisionData = collisionMaps[wv.getPlane()]) != null)
		{
			int mirrorFlags = collisionData.getFlags()[mirrorX][mirrorY];
			int preFlags = collisionData.getFlags()[preMoveX][preMoveY];
			if ((mirrorFlags & badMirrorFlags) != 0 || (preFlags & badPreFlags) != 0)
			{
				g.setColor(ColorUtil.colorWithAlpha(Color.orange, 64));
			}
		}

		centerTile = LocalPoint.fromScene(preMoveX, preMoveY, wv);

		Polygon premoveShape = premoveMode == PremoveMode.ARROW ? PREMOVE_ARROW : PREMOVE_COMPACT;
		Polygon orientedArrow = PolygonUtil.rotate(premoveShape, Math.toRadians(angle));
		Polygon transposedArrow = PolygonUtil.localPointTranspose(centerTile, orientedArrow);
		Polygon canvasArrow = PolygonUtil.toCanvas(client, wv, transposedArrow);

		if (canvasArrow != null)
		{
			g.fill(canvasArrow);
		}
	}
}
