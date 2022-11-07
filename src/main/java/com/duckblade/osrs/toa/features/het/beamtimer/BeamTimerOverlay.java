package com.duckblade.osrs.toa.features.het.beamtimer;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

@Singleton
public class BeamTimerOverlay extends Overlay implements PluginLifecycleComponent
{

	private final OverlayManager overlayManager;
	private final Client client;
	private final BeamTimerTracker beamTimerTracker;

	@Inject
	public BeamTimerOverlay(OverlayManager overlayManager, Client client, BeamTimerTracker beamTimerTracker)
	{
		this.overlayManager = overlayManager;
		this.client = client;
		this.beamTimerTracker = beamTimerTracker;
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return beamTimerTracker.isEnabled(config, raidState);
	}

	@Override
	public void startUp()
	{
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		overlayManager.removeIf(o -> o instanceof BeamTimerOverlay);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		GameObject casterStatue = beamTimerTracker.getCasterStatue();
		if (casterStatue == null)
		{
			return null;
		}

		Point canvasPoint = Perspective.localToCanvas(client, casterStatue.getLocalLocation(), client.getPlane());
		if (canvasPoint == null)
		{
			return null;
		}

		double progress = beamTimerTracker.getProgress();
		Color c = progress == 0 ? Color.green : Color.cyan;
		if (progress < 0)
		{
			return null;
		}

		ProgressPieComponent pie = new ProgressPieComponent();
		pie.setPosition(canvasPoint);
		pie.setProgress(1 - progress);
		pie.setBorderColor(c);
		pie.setFill(c);
		return pie.render(graphics);
	}

}
