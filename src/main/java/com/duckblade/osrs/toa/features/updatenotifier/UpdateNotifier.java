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

	public static final int TARGET_VERSION = 8;
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

			case 7:
				updates.add("This is a big update, so first a special thanks to @rdutta for submitting many features! Some config options have been moved into new sections.");
				updates.add("<strong>(Miscellaneous -> Hide Fade Transition)</strong> can remove the fade to black between rooms. Visual artifacts that are normally hidden may be visible.");
				updates.add("<strong>(Akkha -> Shadows Hp Overlay)</strong> can provide a numerical HP overlay on each shadow.");
				updates.add("<strong>(Burial Tomb -> Detect Cursed Phalanx)</strong> can now prevent you from opening the loot chest on raid level 500+ if you have a cursed phalanx equipped or in your inventory.");
				updates.add("<strong>(Burial Tomb -> Track Purple Dry Count)</strong> can provide a chat message indicating how many raids you have gone without seeing a unique drop.");
				updates.add("<strong>(Path of Apmeken -> Baboon Outline)</strong> can automatically highlight each baboon type a unique colour.");
				updates.add("<strong>(Path of Apmeken -> Volatile Baboon Tile)</strong> can draw a 3x3 box around volatile baboons.");
				updates.add("The Path of Het solver will no longer continue to show the solution when the seal is weakened.");
				updates.add("The points tracker has been updated to account for deaths more accurately. If you have Separate Room Points enabled, you will begin to see that room points are preserved after wipes, this is intentional and accounted for.");
		}

		return updates;
	}
}
