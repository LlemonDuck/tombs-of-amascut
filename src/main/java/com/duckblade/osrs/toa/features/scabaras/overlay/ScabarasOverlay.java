package com.duckblade.osrs.toa.features.scabaras.overlay;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Map;
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
	private final TombsOfAmascutConfig config;
	private final AdditionPuzzleSolver additionPuzzleSolver;
	private final LightPuzzleSolver lightPuzzleSolver;
	private final MatchingPuzzleSolver matchingPuzzleSolver;
	private final ObeliskPuzzleSolver obeliskPuzzleSolver;
	private final SequencePuzzleSolver sequencePuzzleSolver;

	@Inject
	public ScabarasOverlay(
		Client client, TombsOfAmascutConfig config, AdditionPuzzleSolver additionPuzzleSolver, LightPuzzleSolver lightPuzzleSolver,
		MatchingPuzzleSolver matchingPuzzleSolver, ObeliskPuzzleSolver obeliskPuzzleSolver, SequencePuzzleSolver sequencePuzzleSolver
	)
	{
		this.client = client;
		this.config = config;
		this.additionPuzzleSolver = additionPuzzleSolver;
		this.lightPuzzleSolver = lightPuzzleSolver;
		this.matchingPuzzleSolver = matchingPuzzleSolver;
		this.obeliskPuzzleSolver = obeliskPuzzleSolver;
		this.sequencePuzzleSolver = sequencePuzzleSolver;

		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		renderLocalPoints(graphics, additionPuzzleSolver.getFlips(), config.scabarasAdditionTileColor());
		renderLocalPoints(graphics, lightPuzzleSolver.getFlips(), config.scabarasLightTileColor());

		renderLocalSequence(
			graphics,
			obeliskPuzzleSolver.getObeliskOrder(),
			obeliskPuzzleSolver.getActiveObelisks(),
			config.scabarasObeliskColorStart(),
			config.scabarasObeliskColorEnd()
		);
		renderLocalSequence(
			graphics,
			sequencePuzzleSolver.getPoints(),
			sequencePuzzleSolver.getCompletedTiles(),
			config.scabarasSequenceColorStart(),
			config.scabarasSequenceColorEnd()
		);

		renderLocalMatching(graphics, matchingPuzzleSolver.getDiscoveredTiles());
		return null;
	}

	private void renderLocalPoints(Graphics2D graphics, Set<LocalPoint> points, Color color)
	{
		for (LocalPoint tile : points)
		{
			Polygon canvasTilePoly = Perspective.getCanvasTilePoly(client, tile);
			if (canvasTilePoly != null)
			{
				OverlayUtil.renderPolygon(graphics, canvasTilePoly, color, new Color(0, 0, 0, Math.min(color.getAlpha(), 50)), new BasicStroke(2));
			}
		}
	}

	private void renderLocalSequence(Graphics2D graphics, Collection<LocalPoint> points, int progress, Color start, Color end)
	{
		int ix = 0;
		for (LocalPoint tile : points)
		{
			Color c = ix < progress ? ColorUtil.colorWithAlpha(Color.gray, start.getAlpha()) : ColorUtil.colorLerp(start, end, ix / 5.0);

			Polygon canvasTilePoly = Perspective.getCanvasTilePoly(client, tile);
			if (canvasTilePoly != null)
			{
				OverlayUtil.renderPolygon(graphics, canvasTilePoly, c, new Color(0, 0, 0, Math.min(c.getAlpha(), 50)), new BasicStroke(2));
				Rectangle bounds = canvasTilePoly.getBounds();
				renderTextLocationAlpha(
					graphics,
					new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2),
					String.valueOf(++ix),
					c
				);
			}
		}
	}

	private void renderLocalMatching(Graphics2D graphics, Map<LocalPoint, MatchingTile> matchingTiles)
	{
		MatchingTileDisplayMode mode = config.scabarasMatchingDisplayMode();
		if (mode == MatchingTileDisplayMode.DISABLED)
		{
			return;
		}

		int matchedOpacity = config.scabarasMatchingCompletedOpacity();
		boolean tile = mode == MatchingTileDisplayMode.TILE || mode == MatchingTileDisplayMode.BOTH;
		boolean name = mode == MatchingTileDisplayMode.NAME || mode == MatchingTileDisplayMode.BOTH;
		matchingTiles.values().forEach(mt ->
		{
			Polygon canvasTilePoly = Perspective.getCanvasTilePoly(client, mt.getLocalPoint());
			if (canvasTilePoly == null)
			{
				return;
			}

			Color color = mt.getColor();
			if (mt.isMatched())
			{
				color = new Color(color.getRed(), color.getGreen(), color.getBlue(), matchedOpacity);
			}

			if (tile)
			{
				OverlayUtil.renderPolygon(graphics, canvasTilePoly, color, new Color(0, 0, 0, Math.min(color.getAlpha(), 50)), new BasicStroke(2));
			}
			if (name)
			{
				Rectangle tileB = canvasTilePoly.getBounds();
				Rectangle txtB = graphics.getFontMetrics().getStringBounds(mt.getName(), graphics).getBounds();
				Point p = new Point(tileB.x + tileB.width / 2 - txtB.width / 2, tileB.y + tileB.height / 2 + txtB.height / 2);
				renderTextLocationAlpha(graphics, p, mt.getName(), color);
			}
		});
	}

	private void renderTextLocationAlpha(Graphics2D graphics, Point p, String text, Color c)
	{
		graphics.setColor(ColorUtil.colorWithAlpha(Color.black, c.getAlpha()));
		graphics.drawString(text, p.getX() + 1, p.getY() + 1);
		graphics.setColor(c);
		graphics.drawString(text, p.getX(), p.getY());
	}

}
