package com.duckblade.osrs.toa.features.het.solver;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
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
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class HetSolverOverlay extends Overlay implements PluginLifecycleComponent
{

	private final OverlayManager overlayManager;
	private final Client client;
	private final HetSolver hetSolver;

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
		overlayManager.removeIf(o -> o instanceof HetSolverOverlay);
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
			}
		}

		return null;
	}
}
