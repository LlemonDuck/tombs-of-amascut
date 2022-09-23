package com.duckblade.osrs.toa.features.scabaras.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Queue;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ColorUtil;

@Singleton
public class ScabarasOverlay extends Overlay
{

	private final Client client;
	private final AdditionPuzzleSolver additionPuzzleSolver;
	private final LightPuzzleSolver lightPuzzleSolver;
	private final SequencePuzzleSolver sequencePuzzleSolver;

	@Inject
	public ScabarasOverlay(Client client, AdditionPuzzleSolver additionPuzzleSolver, LightPuzzleSolver lightPuzzleSolver, SequencePuzzleSolver sequencePuzzleSolver)
	{
		this.client = client;
		this.additionPuzzleSolver = additionPuzzleSolver;
		this.lightPuzzleSolver = lightPuzzleSolver;
		this.sequencePuzzleSolver = sequencePuzzleSolver;

		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		renderLocalPoints(graphics, lightPuzzleSolver.getFlips());
		renderLocalPoints(graphics, additionPuzzleSolver.getFlips());

		renderLocalSequence(graphics, sequencePuzzleSolver.getPoints(), sequencePuzzleSolver.getCompletedTiles());

		return null;
	}

	private void renderLocalPoints(Graphics2D graphics, Set<LocalPoint> points)
	{
		for (LocalPoint tile : points)
		{
			Polygon canvasTilePoly = Perspective.getCanvasTilePoly(client, tile);
			OverlayUtil.renderPolygon(graphics, canvasTilePoly, Color.red);
		}
	}

	private void renderLocalSequence(Graphics2D graphics, Queue<LocalPoint> points, int progress)
	{
		int ix = 0;
		for (LocalPoint tile : points)
		{
			Color c = ix < progress
				? Color.gray
				: ColorUtil.colorLerp(Color.cyan, Color.blue, (double) ix / (points.size() - 1));

			Polygon canvasTilePoly = Perspective.getCanvasTilePoly(client, tile);
			OverlayUtil.renderPolygon(graphics, canvasTilePoly, c);

			Rectangle bounds = canvasTilePoly.getBounds();
			OverlayUtil.renderTextLocation(graphics, new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2), String.valueOf(++ix), c);
		}
	}

}
