package com.duckblade.osrs.toa.features.scabaras.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
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
	private final LightPuzzleSolver lightPuzzleSolver;

	@Inject
	public ScabarasOverlay(Client client, LightPuzzleSolver lightPuzzleSolver)
	{
		this.client = client;
		this.lightPuzzleSolver = lightPuzzleSolver;

		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (LocalPoint flip : lightPuzzleSolver.getFlips())
		{
			Polygon canvasTilePoly = Perspective.getCanvasTilePoly(client, flip);
			OverlayUtil.renderPolygon(graphics, canvasTilePoly, Color.red);
		}

		return null;
	}

}
