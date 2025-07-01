package com.duckblade.osrs.toa.features.pointstracker;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PurpleWeightingPartyBoardManager implements PluginLifecycleComponent
{

	private static final int SCRIPT_INIT_LOOT_REWARDS = 6770;

	private final EventBus eventBus;

	private final Client client;
	private final PurpleWeightingManager weightingManager;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.isInLobby() &&
			config.purpleWeightingDisplayOnPartyBoard();
	}

	@Override
	public void startUp()
	{
		installToWidget();
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		widgetUninstall();
		eventBus.unregister(this);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired e)
	{
		if (e.getScriptId() == SCRIPT_INIT_LOOT_REWARDS)
		{
			installToWidget();
		}
	}

	@Subscribe
	public void onPurpleWeightChanged(PurpleWeightingManager.PurpleWeightChanged e)
	{
		installToWidget();
	}

	private void installToWidget()
	{
		Widget lootContainer = client.getWidget(InterfaceID.ToaPartydetails.RAID_LOOT_POTENTIAL);
		Widget[] lootChildren;
		if (lootContainer == null || (lootChildren = lootContainer.getChildren()) == null)
		{
			return;
		}

		// remove the previous %s since the script doesn't fully rebuild the widget
		widgetUninstall();

		for (Widget child : lootChildren)
		{
			Purple purple = Purple.forItemId(child.getItemId());
			if (purple == null)
			{
				continue;
			}

			PurpleWeightingManager.PurpleWeighting weighting = weightingManager.getWeighting(purple);
			lootContainer.createChild(-1, WidgetType.TEXT)
				.setText(String.format("%.1f%%", weighting.getPurplePercent() * 100))
				.setFontId(FontID.PLAIN_11)
				.setXTextAlignment(WidgetTextAlignment.CENTER)
				.setYTextAlignment(WidgetTextAlignment.BOTTOM)
				.setTextShadowed(true)
				.setTextColor(Color.white.getRGB())
				.setSize(child.getOriginalWidth(), child.getOriginalHeight(), child.getWidthMode(), child.getHeightMode())
				.setPos(child.getOriginalX(), child.getOriginalY(), child.getXPositionMode(), child.getYPositionMode())
				.revalidate();
		}
	}

	private void widgetUninstall()
	{
		Widget lootContainer = client.getWidget(InterfaceID.ToaPartydetails.RAID_LOOT_POTENTIAL);
		Widget[] lootChildren;
		if (lootContainer == null || (lootChildren = lootContainer.getChildren()) == null)
		{
			return;
		}

		for (int i = 0; i < lootChildren.length; i++)
		{
			if (lootChildren[i].getText().contains("%"))
			{
				lootChildren[i] = null;
			}
		}
	}
}
