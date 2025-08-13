package com.duckblade.osrs.toa.launcher.debugplugins;

import com.duckblade.osrs.toa.features.PathLevelTracker;
import com.duckblade.osrs.toa.util.RaidCompletionTracker;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class StateOverlay extends OverlayPanel
{

	public enum StateDisplays
	{
		PATH_LEVELS,
		RAID_STATE,
		ROOM_COMPLETIONS,
	}

	private final ToaDebugConfig config;

	private final PathLevelTracker pathLevelTracker;
	private final RaidStateTracker raidStateTracker;
	private final RaidCompletionTracker raidCompletionTracker;

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.stateOverlay().isEmpty())
		{
			return null;
		}

		if (config.stateOverlay().contains(StateDisplays.RAID_STATE))
		{
			getPanelComponent().getChildren().addAll(
				List.of(
					TitleComponent.builder()
						.color(Color.ORANGE)
						.text("Raid State")
						.build(),
					LineComponent.builder()
						.left("In Raid")
						.rightColor(raidStateTracker.getCurrentState().isInRaid() ? Color.GREEN : Color.RED)
						.right(String.valueOf(raidStateTracker.getCurrentState().isInRaid()))
						.build(),
					LineComponent.builder()
						.left("In Lobby")
						.rightColor(raidStateTracker.getCurrentState().isInLobby() ? Color.GREEN : Color.RED)
						.right(String.valueOf(raidStateTracker.getCurrentState().isInLobby()))
						.build(),
					LineComponent.builder()
						.left("Room")
						.right(String.valueOf(raidStateTracker.getCurrentState().getCurrentRoom()))
						.build(),
					LineComponent.builder()
						.left("Players")
						.right(String.valueOf(raidStateTracker.getCurrentState().getPlayerCount()))
						.build()
				)
			);
		}

		if (config.stateOverlay().contains(StateDisplays.ROOM_COMPLETIONS))
		{
			getPanelComponent().getChildren().add(
				TitleComponent.builder()
					.color(Color.ORANGE)
					.text("Completion")
					.build()
			);
			for (String boss : raidCompletionTracker.getCompletedBosses())
			{
				getPanelComponent().getChildren().add(
					LineComponent.builder()
						.right(boss)
						.build()
				);
			}
		}

		if (config.stateOverlay().contains(StateDisplays.PATH_LEVELS))
		{
			getPanelComponent().getChildren().addAll(
				List.of(
					TitleComponent.builder()
						.color(Color.ORANGE)
						.text("Path Levels")
						.build(),
					LineComponent.builder()
						.left("Zebak")
						.right(String.valueOf(pathLevelTracker.getZebakPathLevel()))
						.build(),
					LineComponent.builder()
						.left("Kephri")
						.right(String.valueOf(pathLevelTracker.getKephriPathLevel()))
						.build(),
					LineComponent.builder()
						.left("Akkha")
						.right(String.valueOf(pathLevelTracker.getAkkhaPathLevel()))
						.build(),
					LineComponent.builder()
						.left("Ba-ba")
						.right(String.valueOf(pathLevelTracker.getBabaPathLevel()))
						.build()
				)
			);
		}

		return super.render(graphics);
	}
}
