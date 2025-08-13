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
	private final PurpleWeightingManager weightingManager;
	private final TombsOfAmascutConfig config;

	@Inject
	public PointsOverlay(
		OverlayManager overlayManager,
		PointsTracker pointsTracker,
		PartyPointsTracker partyPointsTracker,
		PurpleWeightingManager weightingManager,
		TombsOfAmascutConfig config
	)
	{
		this.overlayManager = overlayManager;
		this.pointsTracker = pointsTracker;
		this.partyPointsTracker = partyPointsTracker;
		this.weightingManager = weightingManager;
		this.config = config;

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
		this.overlayManager.remove(this);
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

		OverlayPurpleWeightDisplayMode purpleWeightDisplayMode = config.purpleWeightingOnPointsOverlay();
		if (purpleWeightDisplayMode != OverlayPurpleWeightDisplayMode.OFF)
		{
			addLine("", "");

			for (Purple purple : Purple.values())
			{
				addPurpleWeightLine(purple, purpleWeightDisplayMode);
			}
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
		addLine(title, PointsTracker.PERCENT_FORMAT.format(value));
	}

	private void addPurpleWeightLine(Purple purple, OverlayPurpleWeightDisplayMode mode)
	{
		PurpleWeightingManager.PurpleWeighting weighting = weightingManager.getWeighting(purple);
		String formatted;
		switch (mode)
		{
			case BOTH:
				formatted = String.format("%.3f%% (%.1f%%)", weighting.getPointsAdjustedPercent() * 100, weighting.getPurplePercent() * 100);
				break;

			case PER_PURPLE:
				formatted = String.format("%.1f%%", weighting.getPurplePercent() * 100);
				break;

			case POINTS_ADJUSTED:
				formatted = String.format("%.3f%%", weighting.getPointsAdjustedPercent() * 100);
				break;

			case OFF:
			default:
				return;
		}

		addLine(purple.getShortName(), formatted);
	}
}
