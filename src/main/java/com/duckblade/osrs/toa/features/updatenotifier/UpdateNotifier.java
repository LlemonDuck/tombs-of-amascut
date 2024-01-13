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

	public static final int TARGET_VERSION = 7;
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
				.panel(new UpdateNotifierPanel(getUpdates(), () ->
				{
					config.updateNotifierLastVersion(TARGET_VERSION);
					clientToolbar.removeNavigation(navButton);
				}))
				.build();

			clientToolbar.addNavigation(navButton);

			SwingUtilities.invokeLater(() -> clientToolbar.openPanel(navButton));
		});
	}

	@Override
	public void shutDown()
	{
		SwingUtilities.invokeLater(() -> clientToolbar.removeNavigation(navButton));
	}

	private List<String> getUpdates()
	{
		List<String> updates = new ArrayList<>();

		if (config.updateNotifierLastVersion() < 3)
		{
			updates.add("<strong>A bug was fixed that prevented this update panel from appearing. If this is your first time seeing it, some of the below changes might be things you've already been using for a while!</strong>");
		}

		switch (config.updateNotifierLastVersion())
		{
			case 0:
				updates.add("A set of puzzle solvers for the Path of Scabaras have been added, which will overlay solutions for you.");
				updates.add("The Path of Het's Deposit-pickaxe swap can now remove the option to enter the next room until you have deposited your pickaxe.");
				updates.add("Plugin-based invocation presets have been readded, but are disabled by default.");
				updates.add("You can now add a custom sound to play when the purple loot chest is opened, e.g. the Legend of Zelda chest loot jingle. Check the config descriptions or plugin README for instructions (thanks @TheStonedTurtle!).");

			case 1:
				updates.add("A points tracker has been added to determine points earned in raids, as well as the unique rate from those points. Calculating points for groups requires players to be in a RuneLite party.");

			case 2:
				updates.add("The Path of Het helper will now display an indicator of when the next beam will fire. Click the seal when it is green from one tile away to get an extra damage tick.");
				updates.add("Some bugs have been fixed with the points tracker. Please continue to report further issues on GitHub!");

			case 3:
				updates.add("The Quick-Proceed swaps can now be set to NOT_CRONDIS, to allow team synchronization on that puzzle without disabling swaps for the entire raid.");
				updates.add("HP Orbs can be toggled off with a new config option.");
				updates.add("The loot chest Bank-all option can be toggled to no longer require two clicks.");
				updates.add("The loot sarcophagus flame colours can now be recoloured (thanks @rdutta!).");
				updates.add("The Scabaras addition puzzle routes now use more optimal paths (thanks @PowContent!).");

			case 4:
				updates.add("HP orbs can now be replaced by health bars a la ToB Health Bars.");
				updates.add("The NOT_CRONDIS option for Quick-proceed swaps will no longer prevent quick entry on the door to the boss.");
				updates.add("An indicator for which puzzle entrance to use to skip the obelisk puzzle can be enabled under Scabaras options.");

			case 5:
				updates.add("Added support for room and path time splits.");
				updates.add("Added a visual overlay deposit-pickaxe reminder for both the puzzle room and pre-raid lobby.");
				updates.add("Fixed a myriad of bugs in points tracking, health bar formatting, and error handling.");

			case 6:
				updates.add("The Path of Het mirror puzzle now has a full overlay solver.");
		}

		return updates;
	}
}
