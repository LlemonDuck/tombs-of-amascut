package com.duckblade.osrs.toa.features.pointstracker;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Singleton
public class PointsOverlay extends OverlayPanel implements PluginLifecycleComponent
{

	private final OverlayManager overlayManager;
	private final PointsTracker pointsTracker;
	private final PartyPointsTracker partyPointsTracker;
	private final TombsOfAmascutConfig config;

	@Inject
	public PointsOverlay(OverlayManager overlayManager, TombsOfAmascutConfig config, PointsTracker pointsTracker, PartyPointsTracker partyPointsTracker)
	{
		this.overlayManager = overlayManager;
		this.config = config;
		this.pointsTracker = pointsTracker;
		this.partyPointsTracker = partyPointsTracker;
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.pointsTrackerOverlayEnable() &&
			raidState.isInRaid();
	}

	@Override
	public void startUp()
	{
		this.overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		this.overlayManager.removeIf(o -> o instanceof PointsOverlay);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().add(
			TitleComponent.builder()
				.text("ToA Points")
				.build()
		);

		addPointsLine("Total:", pointsTracker.getTotalPoints());
		if (partyPointsTracker.isInParty())
		{
			addPointsLine("Personal:", pointsTracker.getPersonalTotalPoints());
		}

		if (config.pointsTrackerShowRoomPoints())
		{
			addPointsLine("Room:", pointsTracker.getPersonalRoomPoints());
		}

		if (config.pointsTrackerShowUniqueChance())
		{
			addChanceLine("Unique:", pointsTracker.getUniqueChance());
		}

		if (config.pointsTrackerShowPetChance())
		{
			addChanceLine("Pet:", pointsTracker.getPetChance());
		}

		return super.render(graphics);
	}

	private void addLine(String left, String right)
	{
		panelComponent.getChildren().add(
			LineComponent.builder()
				.left(left)
				.right(right)
				.build()
		);
	}

	private void addPointsLine(String title, int value)
	{
		addLine(title, PointsTracker.POINTS_FORMAT.format(value));
	}

	private void addChanceLine(String title, double value)
	{
		addLine(title, PointsTracker.PERCENT_FORMAT.format(value / 100));
	}
}
