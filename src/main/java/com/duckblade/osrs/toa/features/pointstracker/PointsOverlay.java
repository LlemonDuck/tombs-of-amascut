package com.duckblade.osrs.toa.features.pointstracker;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

		if (partyPointsTracker.isInParty())
		{
			addLine("Party:", partyPointsTracker.getTotalPartyPoints());
		}

		addLine("Total:", pointsTracker.getTotalPoints());
		if (config.pointsTrackerShowRoomPoints())
		{
			addLine("Room:", pointsTracker.getPersonalRoomPoints());
		}

		if (config.pointsTrackerShowUniqueChance())
		{
			addLine("Unique %:", pointsTracker.getUniqueChance());
		}

		if (config.pointsTrackerShowPetChance())
		{
			addLine("Pet %:", pointsTracker.getPetChance());
		}

		return super.render(graphics);
	}

	private void addLine(String title, double value)
	{
		panelComponent.getChildren().add(
			LineComponent.builder()
				.left(title)
				.right(DecimalFormat.getInstance().format(value))
				.build()
		);
	}
}
