package com.duckblade.osrs.toa.features.timetracking;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.TombsOfAmascutPlugin;
import static com.duckblade.osrs.toa.features.timetracking.TargetTimeManager.SCRIPT_TOA_TIME_UPDATE_TIMER;
import static com.duckblade.osrs.toa.features.timetracking.TargetTimeManager.WIDGET_TIMER;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import java.awt.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@Slf4j
@Singleton
public class SplitsInfoBox
	extends InfoBox
	implements PluginLifecycleComponent
{

	private final EventBus eventBus;
	private final InfoBoxManager infoBoxManager;
	private final SpriteManager spriteManager;

	private final Client client;
	private final SplitsTracker splitsTracker;
	private final RaidStateTracker raidStateTracker;

	private SplitsMode tooltipMode;
	private String currentTime;
	private int timerSpriteId;

	@Inject
	public SplitsInfoBox(
		TombsOfAmascutPlugin plugin,
		EventBus eventBus,
		InfoBoxManager infoBoxManager,
		SpriteManager spriteManager,
		Client client,
		SplitsTracker splitsTracker,
		RaidStateTracker raidStateTracker
	)
	{
		super(null, plugin);

		this.eventBus = eventBus;
		this.infoBoxManager = infoBoxManager;
		this.spriteManager = spriteManager;
		this.client = client;
		this.splitsTracker = splitsTracker;
		this.raidStateTracker = raidStateTracker;
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		this.tooltipMode = config.splitsInfoboxHoverSplits();
		return raidState.isInRaid() && config.splitsInfobox();
	}

	@Override
	public void startUp()
	{
		currentTime = null;
		timerSpriteId = 0;
		infoBoxManager.addInfoBox(this);
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		currentTime = null;
		timerSpriteId = 0;
		infoBoxManager.removeInfoBox(this);
		eventBus.unregister(this);
	}

	@Override
	public String getText()
	{
		if (currentTime == null)
		{
			return "--:--";
		}

		return currentTime;
	}

	@Override
	public Color getTextColor()
	{
		return Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		if (tooltipMode == SplitsMode.OFF)
		{
			return null;
		}

		StringBuilder sb = new StringBuilder();

		RaidRoom currentRoom = raidStateTracker.getCurrentState().getCurrentRoom();
		boolean hasCurrentTime = false;

		for (Split s : splitsTracker.getSplits())
		{
			if (tooltipMode.includesRoom(s.getRoom()))
			{
				hasCurrentTime = hasCurrentTime || currentRoom == s.getRoom();
				sb.append(s.getRoom())
					.append(": ")
					.append(s.getSplit())
					.append("<br>");
			}
		}

		String currentName;
		if (!hasCurrentTime && (currentName = tooltipMode.nextSplit(currentRoom)) != null)
		{
			sb.append(currentName)
				.append(": ")
				.append(currentTime);
		}

		return sb.toString();
	}

	@Subscribe(priority = Integer.MAX_VALUE) // needs to run before anything messes with it (e.g. TargetTimeManager)
	public void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() == SCRIPT_TOA_TIME_UPDATE_TIMER)
		{
			Widget timer = client.getWidget(WIDGET_TIMER);
			if (timer != null)
			{
				this.currentTime = timer.getText().split("\\.")[0];
			}

			Widget spriteWidget = client.getWidget(InterfaceID.ToaHud.RAID_DIFFICULTY_GFX);
			if (spriteWidget != null && timerSpriteId != spriteWidget.getSpriteId())
			{
				timerSpriteId = spriteWidget.getSpriteId();
				spriteManager.getSpriteAsync(timerSpriteId, 0, this);
			}
		}
	}
}
