package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
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
public class SwarmerPanelManager implements PluginLifecycleComponent
{

	public enum PanelMode
	{
		ALWAYS,
		AT_TOA,
		NEVER,
		;
	}

	private static final BufferedImage PANEL_ICON = ImageUtil.loadImageResource(SwarmerPanelManager.class, "icon.png");

	private final ClientToolbar clientToolbar;
	private final SwarmerDataManager dataManager;

	private NavigationButton navButton;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		switch (config.swarmerSidePanel())
		{
			case ALWAYS:
				return true;

			case AT_TOA:
				return raidState.isInLobby() || raidState.isInRaid();

			case NEVER:
			default:
				return false;
		}
	}

	@Override
	public void startUp()
	{
		SwingUtilities.invokeLater(() ->
		{
			navButton = NavigationButton.builder()
				.tooltip("Swarmer")
				.icon(PANEL_ICON)
				.priority(999)
				.panel(new SwarmerPanel(dataManager))
				.build();
			clientToolbar.addNavigation(navButton);
		});
	}

	@Override
	public void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
	}
}
