package com.duckblade.osrs.toa.features.apmeken;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidRoomChanged;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import lombok.RequiredArgsConstructor;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ApmekenWaveInstaller implements PluginLifecycleComponent
{

	private static final BufferedImage PANEL_ICON = ImageUtil.loadImageResource(ApmekenWaveInstaller.class, "icon.png");

	private final EventBus eventBus;
	private final ClientToolbar clientToolbar;
	private final RaidStateTracker raidStateTracker;
	private final ApmekenWavePanel apmekenWavePanel;

	private NavigationButton navButton;

	@Override
	public boolean isConfigEnabled(TombsOfAmascutConfig config)
	{
		return config.apmekenWaveHelper();
	}

	@Override
	public void startUp()
	{
		if (navButton == null)
		{
			navButton = NavigationButton.builder()
				.icon(PANEL_ICON)
				.panel(apmekenWavePanel)
				.priority(999)
				.tooltip("Apmeken Waves")
				.build();
		}

		eventBus.register(this);
		if (raidStateTracker.isInRaid() && raidStateTracker.getCurrentRoom() == RaidRoom.APMEKEN)
		{
			openPanel();
		}
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		removePanel();
	}

	@Subscribe
	public void onRaidRoomChanged(RaidRoomChanged e)
	{
		if (e.getCurrent() == RaidRoom.APMEKEN)
		{
			openPanel();
		}
		else
		{
			removePanel();
		}
	}

	private void openPanel()
	{
		clientToolbar.addNavigation(navButton);
		SwingUtilities.invokeLater(() ->
		{
			if (!navButton.isSelected())
			{
				navButton.getOnSelect().run();
			}
		});
	}

	private void removePanel()
	{
		navButton.setSelected(false);
		clientToolbar.removeNavigation(navButton);
	}
}
