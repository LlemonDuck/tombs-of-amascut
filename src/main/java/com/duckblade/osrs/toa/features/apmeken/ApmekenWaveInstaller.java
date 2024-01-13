package com.duckblade.osrs.toa.features.apmeken;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import lombok.RequiredArgsConstructor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ApmekenWaveInstaller implements PluginLifecycleComponent
{

	private static final BufferedImage PANEL_ICON = ImageUtil.loadImageResource(ApmekenWaveInstaller.class, "icon.png");

	private final ClientToolbar clientToolbar;
	private final ApmekenWavePanel apmekenWavePanel;

	private NavigationButton navButton;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.apmekenWaveHelper() &&
			raidState.getCurrentRoom() == RaidRoom.APMEKEN;
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

		openPanel();
	}

	@Override
	public void shutDown()
	{
		removePanel();
	}

	private void openPanel()
	{
		clientToolbar.addNavigation(navButton);
		SwingUtilities.invokeLater(() -> clientToolbar.openPanel(navButton));
	}

	private void removePanel()
	{
		clientToolbar.removeNavigation(navButton);
	}
}
