package com.duckblade.osrs.toa.features.het;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

@Singleton
public class BeamTimerOverlay extends Overlay
{

	private final Client client;
	private final BeamTimer beamTimer;

	@Inject
	public BeamTimerOverlay(Client client, BeamTimer beamTimer)
	{
		this.client = client;
		this.beamTimer = beamTimer;

		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (beamTimer.challengeComplete)
		{
			return null;
		}
		if (beamTimer.casterStatue == null)
		{
			return null;
		}

		LocalPoint statueLocation = LocalPoint.fromWorld(client, beamTimer.casterStatue.getWorldLocation());
		if (statueLocation == null)
		{
			return null;
		}

		Point canvasLocation = Perspective.localToCanvas(client, statueLocation, client.getPlane());
		if (canvasLocation != null)
		{
			ProgressPieComponent progressPieComponent = new ProgressPieComponent();
			progressPieComponent.setPosition(canvasLocation);

			double progress = beamTimer.beamProgress();
			progressPieComponent.setProgress(progress);
			if (progress < 1)
			{
				progressPieComponent.setBorderColor(Color.CYAN);
				progressPieComponent.setFill(Color.CYAN);
			}
			else
			{
				progressPieComponent.setBorderColor(Color.GREEN);
				progressPieComponent.setFill(Color.GREEN);
			}
			progressPieComponent.render(graphics);
		}

		return null;
	}
}
