package com.duckblade.osrs.toa.features.scabaras.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class ScabarasOverlay extends Overlay
{

	private final Client client;
	private final AdditionPuzzleSolver additionPuzzleSolver;
	private final LightPuzzleSolver lightPuzzleSolver;

	@Inject
	public ScabarasOverlay(Client client, AdditionPuzzleSolver additionPuzzleSolver, LightPuzzleSolver lightPuzzleSolver)
	{
		this.client = client;
		this.additionPuzzleSolver = additionPuzzleSolver;
		this.lightPuzzleSolver = lightPuzzleSolver;

		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		renderLocalPoints(graphics, lightPuzzleSolver.getFlips());
		renderLocalPoints(graphics, additionPuzzleSolver.getFlips());
		return null;
	}

	private void renderLocalPoints(Graphics2D graphics, Set<LocalPoint> points)
	{
		for (LocalPoint flip : points)
		{
			Polygon canvasTilePoly = Perspective.getCanvasTilePoly(client, flip);
			OverlayUtil.renderPolygon(graphics, canvasTilePoly, Color.red);
		}
	}

}
