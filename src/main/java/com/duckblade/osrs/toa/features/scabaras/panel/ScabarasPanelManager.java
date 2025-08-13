package com.duckblade.osrs.toa.features.scabaras.panel;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.features.scabaras.ScabarasHelperMode;
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
public class ScabarasPanelManager implements PluginLifecycleComponent
{

	private static final BufferedImage PANEL_ICON = ImageUtil.loadImageResource(ScabarasPanelManager.class, "icon.png");

	private final ClientToolbar clientToolbar;
	private final ScabarasPanel scabarasPanel;

	private NavigationButton navButton;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState currentState)
	{
		return config.scabarasHelperMode() == ScabarasHelperMode.SIDE_PANEL &&
			currentState.getCurrentRoom() == RaidRoom.SCABARAS;
	}

	@Override
	public void startUp()
	{
		if (navButton == null)
		{
			navButton = NavigationButton.builder()
				.icon(PANEL_ICON)
				.panel(scabarasPanel)
				.priority(999)
				.tooltip("Scabaras Tile Puzzle Helper")
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
