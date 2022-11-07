package com.duckblade.osrs.toa.features.updatenotifier;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import lombok.RequiredArgsConstructor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UpdateNotifier implements PluginLifecycleComponent
{

	public static final int TARGET_VERSION = 3;
	private static final BufferedImage PANEL_ICON = ImageUtil.loadImageResource(UpdateNotifier.class, "icon.png");

	private final ClientToolbar clientToolbar;
	private final TombsOfAmascutConfig config;

	private NavigationButton navButton;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.showUpdateMessages() &&
			config.updateNotifierLastVersion() < TARGET_VERSION &&
			raidState.isInLobby();
	}

	@Override
	public void startUp()
	{
		SwingUtilities.invokeLater(() ->
		{
			navButton = NavigationButton.builder()
				.tooltip("ToA Plugin Updates")
				.priority(999)
				.icon(PANEL_ICON)
				.panel(new UpdateNotifierPanel(getUpdates(), () -> config.updateNotifierLastVersion(TARGET_VERSION)))
				.build();

			clientToolbar.addNavigation(navButton);

			if (!navButton.isSelected())
			{
				navButton.getOnSelect().run();
			}
		});
	}

	@Override
	public void shutDown()
	{
		SwingUtilities.invokeLater(() ->
		{
			clientToolbar.removeNavigation(navButton);
		});
	}

	private List<String> getUpdates()
	{
		List<String> updates = new ArrayList<>();

		switch (config.updateNotifierLastVersion())
		{
			case 0:
				updates.add("A set of puzzle solvers for the Path of Scabaras have been added, which will overlay solutions for you.");
				updates.add("The Path of Het's Deposit-pickaxe swap can now remove the option to enter the next room until you have deposited your pickaxe.");
				updates.add("Plugin-based invocation presets have been readded, but are disabled by default.");
				updates.add("You can now add a custom sound to play when the purple loot chest is opened, e.g. the Legend of Zelda chest loot jingle. Check the config descriptions or plugin README for instructions.");

			case 1:
				updates.add("A points tracker has been added to determine points earned in raids, as well as the unique rate from those points. Calculating points for groups requires players to be in a RuneLite party.");

			case 2:
				updates.add("The Path of Het helper will now display an indicator of when the next beam will fire. Click the seal when it is green from one tile away to get an extra damage tick.");
				updates.add("Some bugs have been fixed with the points tracker. Please continue to report further issues on GitHub!");
		}

		return updates;
	}
}
