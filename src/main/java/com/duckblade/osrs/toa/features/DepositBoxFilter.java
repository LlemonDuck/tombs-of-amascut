package com.duckblade.osrs.toa.features;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DepositBoxFilter implements PluginLifecycleComponent
{

	private static final int SOUND_EFFECT_DENIED = 2277;
	private static final String ACTION_NO_DEPOSIT = "ToA Plugin Info";

	static final Set<Integer> DEPOSIT_BOX_SLOT_IDS = ImmutableSet.of(
		InterfaceID.BankDepositbox.INVENTORY,
		InterfaceID.BankDepositbox.SLOT0,
		InterfaceID.BankDepositbox.SLOT1,
		InterfaceID.BankDepositbox.SLOT2,
		InterfaceID.BankDepositbox.SLOT3,
		InterfaceID.BankDepositbox.SLOT4,
		InterfaceID.BankDepositbox.SLOT5,
		InterfaceID.BankDepositbox.SLOT7,
		InterfaceID.BankDepositbox.SLOT9,
		InterfaceID.BankDepositbox.SLOT10,
		InterfaceID.BankDepositbox.SLOT12,
		InterfaceID.BankDepositbox.SLOT13,
		InterfaceID.BankDepositbox.EXTRA_QUIVER_AMMO
	);

	private final EventBus eventBus;

	private final Client client;
	private final TombsOfAmascutConfig config;

	private Set<String> allowedItemNames;
	private boolean preventInterfaceDeposit;
	private boolean preventUseDeposit;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState currentState)
	{
		if (currentState.getCurrentRoom() == RaidRoom.NEXUS) // todo
		{
			this.allowedItemNames = new HashSet<>(Text.fromCSV(config.depositBoxFilterString()));
			this.preventInterfaceDeposit = config.depositBoxPreventInterface();
			this.preventUseDeposit = config.depositBoxPreventUse();
			return true;
		}

		return false;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	private void onMenuEntryAdded(final MenuEntryAdded e)
	{
		interceptDepositAction(e);
		interceptUseOnDepositBox(e);
	}

	boolean isDepositAllowed(String itemName)
	{
		return allowedItemNames.contains(Text.removeTags(itemName));
	}

	private void interceptDepositAction(MenuEntryAdded e)
	{
		if (!preventInterfaceDeposit ||
			!isDepositAction(e.getMenuEntry()) ||
			isDepositAllowed(e.getTarget()))
		{
			return;
		}

		// deprioritize instead of remove so users can manually override
		e.getMenuEntry()
			.setDeprioritized(true);

		// prevent adding 5x for the Deposit-1/5/10/etc options
		if (Arrays.stream(client.getMenu()
				.getMenuEntries())
			.anyMatch(me -> me.getOption().equals(ACTION_NO_DEPOSIT)))
		{
			return;
		}

		// add a feedback action so users aren't confused why they can't deposit
		client.getMenu()
			.createMenuEntry(-1)
			.setType(MenuAction.RUNELITE_WIDGET)
			.setOption(ACTION_NO_DEPOSIT)
			.setTarget(e.getTarget())
			.onClick(_clicked ->
			{
				client.playSoundEffect(SOUND_EFFECT_DENIED);
				client.addChatMessage(
					ChatMessageType.GAMEMESSAGE,
					"",
					"Deposit action prevented due to item filters. " +
						"To deposit this item, reconfigure options in Tombs of Amascut -> Deposit Box -> Allowed Items, " +
						"or use the right-click menu.",
					"RL/Tombs of Amascut"
				);
			});
	}

	private void interceptUseOnDepositBox(MenuEntryAdded e)
	{
		if (!preventUseDeposit ||
			!isUseAction(e.getMenuEntry()))
		{
			return;
		}

		String[] targetParts = e.getTarget().split(" -> ");
		if (!targetParts[1].contains("Deposit"))
		{
			return;
		}

		String itemName = Text.removeTags(targetParts[0]).strip();
		if (isDepositAllowed(itemName))
		{
			return;
		}

		client.getMenu()
			.removeMenuEntry(e.getMenuEntry());
	}

	private boolean isUseAction(MenuEntry e)
	{
		return e.getType() == MenuAction.WIDGET_TARGET_ON_GAME_OBJECT &&
			e.getOption().equals("Use");
	}

	private boolean isDepositAction(MenuEntry e)
	{
		return e.getType() == MenuAction.CC_OP &&
			(e.getOption().equals("Bank") || e.getOption().startsWith("Deposit-")) &&
			DEPOSIT_BOX_SLOT_IDS.contains(e.getParam1());
	}

}
