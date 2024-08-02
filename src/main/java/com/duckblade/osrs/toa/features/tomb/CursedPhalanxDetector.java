package com.duckblade.osrs.toa.features.tomb;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.InventoryUtil;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class CursedPhalanxDetector implements PluginLifecycleComponent
{
    private static final Set<Integer> CURSED_PHALANX_ITEM_IDS = ImmutableSet.of(
            ItemID.CURSED_PHALANX,
            ItemID.OSMUMTENS_FANG_OR
    );
    private boolean isEligibleForKit = true;
    @Inject
    private EventBus eventBus;
    @Inject
    private Client client;

    @Inject
    private RaidStateTracker raidStateTracker;

    @Override
    public boolean isEnabled(final TombsOfAmascutConfig config, final RaidState raidState)
    {
        return config.cursedPhalanxDetect();
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
    private void onGameTick(GameTick e)
    {
        if (raidStateTracker.getCurrentState().getCurrentRoom() == null) // reset when not in raid
        {
            isEligibleForKit = true;
            return;
        }

        if ( isEligibleForKit && (
                client.getVarbitValue(Varbits.TOA_MEMBER_0_HEALTH) == 30 ||
                client.getVarbitValue(Varbits.TOA_MEMBER_1_HEALTH) == 30 ||
                client.getVarbitValue(Varbits.TOA_MEMBER_2_HEALTH) == 30 ||
                client.getVarbitValue(Varbits.TOA_MEMBER_3_HEALTH) == 30 ||
                client.getVarbitValue(Varbits.TOA_MEMBER_4_HEALTH) == 30 ||
                client.getVarbitValue(Varbits.TOA_MEMBER_5_HEALTH) == 30 ||
                client.getVarbitValue(Varbits.TOA_MEMBER_6_HEALTH) == 30 ||
                client.getVarbitValue(Varbits.TOA_MEMBER_7_HEALTH) == 30
        ))
        {
            isEligibleForKit = false;
        }

    }

    @Subscribe
    private void onMenuOptionClicked(final MenuOptionClicked event)
    {
        if (raidStateTracker.getCurrentState().getCurrentRoom() != RaidRoom.TOMB)
        {
            return;
        }
        if (client.getVarbitValue(Varbits.TOA_RAID_LEVEL) < 500)
        {
            return;
        }

        final MenuEntry menuEntry = event.getMenuEntry();

        if (!menuEntry.getOption().equals("Open"))
        {
            return;
        }

        boolean wearingPhalanx = InventoryUtil.containsAny(client.getItemContainer(InventoryID.EQUIPMENT), CURSED_PHALANX_ITEM_IDS);
        boolean carryingPhalanx = InventoryUtil.containsAny(client.getItemContainer(InventoryID.INVENTORY), CURSED_PHALANX_ITEM_IDS);

        if ((wearingPhalanx || carryingPhalanx) && isEligibleForKit)
        {
            event.consume();
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Remove and/or drop cursed phalanx before doing that.", null);
        }
    }
}
