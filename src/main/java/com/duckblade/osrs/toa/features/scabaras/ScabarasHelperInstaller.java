package com.duckblade.osrs.toa.features.scabaras;

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
public class ScabarasHelperInstaller implements PluginLifecycleComponent
{

	private static final BufferedImage PANEL_ICON = ImageUtil.loadImageResource(ScabarasHelperInstaller.class, "icon.png");

	private final ClientToolbar clientToolbar;
	private final ScabarasHelperPanel scabarasHelperPanel;

	private NavigationButton navButton;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState currentState)
	{
		return config.scabarasTileHelper() &&
			currentState.getCurrentRoom() == RaidRoom.SCABARAS;
	}

	@Override
	public void startUp()
	{
		if (navButton == null)
		{
			navButton = NavigationButton.builder()
				.icon(PANEL_ICON)
				.panel(scabarasHelperPanel)
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
