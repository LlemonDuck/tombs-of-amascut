package com.duckblade.osrs.toa.features.pointstracker;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.TombsOfAmascutPlugin;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.duckblade.osrs.toa.util.RaidStateChanged;
import com.duckblade.osrs.toa.util.RaidStateTracker;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginMessage;
import net.runelite.client.util.ColorUtil;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PointsTracker implements PluginLifecycleComponent
{

	public static final String EVENT_NAME = "raidCompletedPoints";

	/* I have some insider knowledge here that the blog was describing points earning slightly wrong wrt deaths.
	 * Points are earned to both total and room points at the same time,
	 * rather than being queued up in room points and added onto total after the room.
	 * When dying, you preserve the room points amount toward cap, but subtract 20% from total.
	 * There is no special behaviour when wiping a room; the 20% points lost is intended to account for that.
	 */

	static final NumberFormat POINTS_FORMAT = NumberFormat.getInstance();
	static final NumberFormat PERCENT_FORMAT = new DecimalFormat("#.##%");

	private static final String START_MESSAGE = "You enter the Tombs of Amascut";
	private static final String DEATH_MESSAGE = "You have died";
	private static final String ROOM_FAIL_MESSAGE = "Your party failed to complete";
	private static final String ROOM_FINISH_MESSAGE = "Challenge complete";

	private static final int BASE_POINTS = 5000;
	private static final int MAX_ROOM_POINTS = 20_000;
	private static final int CRONDIS_MAX_ROOM_POINTS = 10_000;

	private static final int MAX_TOTAL_POINTS = 64_000;

	private static final int ANIMATION_ID_WARDEN_DOWN = 9670;

	private static final Map<Integer, Double> DAMAGE_POINTS_FACTORS = ImmutableMap.<Integer, Double>builder()
		.put(NpcID.TOA_WARDEN_TUMEKEN_CORE, 0.0)
		.put(NpcID.TOA_WARDEN_ELIDINIS_CORE, 0.0)
		.put(NpcID.WARDENS_P3_ORB_BLUE, 0.0)
		.put(NpcID.TOA_BABA_BOULDER, 0.0)
		.put(NpcID.TOA_BABA_BOULDER_WEAK, 0.0)
		.put(NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_1, 1.2)
		.put(NpcID.TOA_PATH_APMEKEN_BABOON_MELEE_2, 1.2)
		.put(NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_1, 1.2)
		.put(NpcID.TOA_PATH_APMEKEN_BABOON_RANGED_2, 1.2)
		.put(NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_1, 1.2)
		.put(NpcID.TOA_PATH_APMEKEN_BABOON_MAGIC_2, 1.2)
		.put(NpcID.TOA_PATH_APMEKEN_BABOON_SHAMAN, 1.2)
		.put(NpcID.TOA_PATH_APMEKEN_BABOON_ZOMBIE, 1.2)
		.put(NpcID.TOA_PATH_APMEKEN_BABOON_CURSED, 1.2)
		.put(NpcID.TOA_PATH_APMEKEN_BABOON_THRALL, 1.2)
		.put(NpcID.TOA_BABA, 2.0)
		.put(NpcID.TOA_BABA_COFFIN, 2.0)
		.put(NpcID.TOA_BABA_DIGGING, 2.0)
		.put(NpcID.TOA_ZEBAK_TRANSMOG, 1.5)
		.put(NpcID.TOA_ZEBAK, 1.5)
		.put(NpcID.TOA_ZEBAK_ENRAGED, 1.5)
		.put(NpcID.TOA_ZEBAK_DEAD, 1.5)
		.put(NpcID.TOA_KEPHRI_GUARDIAN_RANGED, 0.5)
		.put(NpcID.TOA_KEPHRI_GUARDIAN_MELEE, 0.5)
		.put(NpcID.TOA_KEPHRI_GUARDIAN_MAGE, 0.5)
		.put(NpcID.TOA_HET_GOAL_VULNERABLE, 2.5)
		.put(NpcID.TOA_WARDENS_P1_OBELISK_NPC_INACTIVE, 1.5)
		.put(NpcID.TOA_WARDENS_P1_OBELISK_NPC, 1.5)
		.put(NpcID.TOA_WARDENS_P2_OBELISK_NPC, 1.5)
		.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE1_INACTIVE, 0.0) // non-combat wardens (prevents extra points during p1->p2 transition)
		.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE1, 0.0)
		.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE1_INACTIVE, 0.0)
		.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE1, 0.0)
		.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE3_INACTIVE, 0.0)
		.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE3_INACTIVE, 0.0)
		.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE2_MAGE, 2.0) // p2 wardens
		.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE2_RANGE, 2.0)
		.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE2_EXPOSED, 0.0) // downed
		.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE2_MAGE, 2.0)
		.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE2_RANGE, 2.0)
		.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE2_EXPOSED, 0.0) // downed
		.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE3, 2.5) // p3 wardens
		.put(NpcID.TOA_WARDEN_ELIDINIS_PHASE3_CHARGING, 2.5)
		.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE3, 2.5)
		.put(NpcID.TOA_WARDEN_TUMEKEN_PHASE3_CHARGING, 2.5)
		.build();

	// these have a cap at 3 "downs"
	private static final ImmutableSet<Integer> P2_WARDENS = ImmutableSet.of(
		NpcID.TOA_WARDEN_ELIDINIS_PHASE2_MAGE,
		NpcID.TOA_WARDEN_ELIDINIS_PHASE2_RANGE,
		NpcID.TOA_WARDEN_ELIDINIS_PHASE2_EXPOSED,
		NpcID.TOA_WARDEN_TUMEKEN_PHASE2_MAGE,
		NpcID.TOA_WARDEN_TUMEKEN_PHASE2_RANGE,
		NpcID.TOA_WARDEN_TUMEKEN_PHASE2_EXPOSED
	);

	private static final ImmutableSet<Integer> MVP_ITEMS = ImmutableSet.of(
		ItemID.TOA_ZEBAK_FANG,
		ItemID.TOA_KEPHRI_POO,
		ItemID.TOA_BABA_BANANA,
		ItemID.TOA_AKKHA_ASHES
	);

	private final EventBus eventBus;
	private final Client client;
	private final TombsOfAmascutConfig config;
	private final PartyPointsTracker partyPointsTracker;
	private final RaidStateTracker raidStateTracker;

	@Getter
	private int personalRoomPoints;
	private int personalTotalPoints;
	private int nonPartyPoints; // points that are earned once by the entire party
	private final List<Integer> seenMvpItems = new ArrayList<>(4);

	private int teamSize;
	private int raidLevel;
	private int wardenDowns;

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		// always track even if not displaying, so that party members get points totals
		return raidState.isInRaid();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);

		reset();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		raidLevel = client.getVarbitValue(VarbitID.TOA_CLIENT_RAID_LEVEL);
	}

	@Subscribe
	public void onRaidStateChanged(RaidStateChanged e)
	{
		teamSize = e.getNewState().getPlayerCount();
		if (e.getPreviousState() == null
			|| e.getPreviousState().getCurrentRoom() == null
			|| e.getPreviousState().getCurrentRoom() == e.getNewState().getCurrentRoom())
		{
			return;
		}

		if (e.getNewState().getCurrentRoom() == RaidRoom.TOMB && config.pointsTrackerAllowExternal())
		{
			// this is used by raid data tracker, ping gh@null-zero (or team) before any changes
			postPointsEvent();
		}

		switch (e.getPreviousState().getCurrentRoom())
		{
			case TOMB:
				if (config.pointsTrackerPostRaidMessage())
				{
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", buildPointsMessage(), "", false);
				}
				break;

			// puzzle estimates
			case SCABARAS:
				personalTotalPoints += 300;
				nonPartyPoints += 300;
				updatePersonalPartyPoints(false);
				break;

			case APMEKEN:
				personalTotalPoints += 450;
				nonPartyPoints += 300;
				updatePersonalPartyPoints(false);
				break;

			case CRONDIS:
				personalTotalPoints += 400;
				nonPartyPoints += 300;
				updatePersonalPartyPoints(false);
				break;

			case HET:
			case WARDENS:
				nonPartyPoints += 300;
				break;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		if (e.getMessage().startsWith(START_MESSAGE))
		{
			reset();
		}
		else if (e.getMessage().startsWith(DEATH_MESSAGE))
		{
			personalTotalPoints -= (int) Math.max(0.2 * personalTotalPoints, 1000);
			if (personalTotalPoints < 0)
			{
				personalTotalPoints = 0;
			}

			updatePersonalPartyPoints(false);
		}
		else if (e.getMessage().startsWith(ROOM_FAIL_MESSAGE))
		{
			wardenDowns = 0;
		}
		else if (e.getMessage().startsWith(ROOM_FINISH_MESSAGE))
		{
			personalRoomPoints = 0;

			boolean isWardens = e.getMessage().contains("Wardens");
			updatePersonalPartyPoints(isWardens);
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied e)
	{
		if (e.getHitsplat().getAmount() < 1 || !(e.getActor() instanceof NPC))
		{
			return;
		}

		NPC target = (NPC) e.getActor();
		log.debug("Hitsplat type {} damage {} on {}", e.getHitsplat().getHitsplatType(), e.getHitsplat().getAmount(), target.getId());
		if (P2_WARDENS.contains(target.getId()) && wardenDowns > 3)
		{
			return;
		}

		double factor = DAMAGE_POINTS_FACTORS.getOrDefault(target.getId(), 1.0);
		if (e.getHitsplat().isMine())
		{
			int pointsEarned = (int) (e.getHitsplat().getAmount() * factor);
			int roomMax = raidStateTracker.getCurrentState().getCurrentRoom() == RaidRoom.CRONDIS ? CRONDIS_MAX_ROOM_POINTS : MAX_ROOM_POINTS;
			if (personalRoomPoints + pointsEarned > roomMax)
			{
				pointsEarned = roomMax - personalRoomPoints;
			}

			this.personalRoomPoints = Math.min(roomMax, personalRoomPoints + pointsEarned);
			this.personalTotalPoints = Math.min(MAX_TOTAL_POINTS, personalTotalPoints + pointsEarned);

			updatePersonalPartyPoints(false);
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned e)
	{
		if (MVP_ITEMS.contains(e.getItem().getId()) && !seenMvpItems.contains(e.getItem().getId()))
		{
			personalTotalPoints += 300 * teamSize;
			seenMvpItems.add(e.getItem().getId());
			updatePersonalPartyPoints(false);
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged e)
	{
		if (!(e.getActor() instanceof NPC) || !P2_WARDENS.contains(((NPC) e.getActor()).getId()))
		{
			return;
		}

		if (e.getActor().getAnimation() == ANIMATION_ID_WARDEN_DOWN)
		{
			wardenDowns++;
		}
	}

	public int getPersonalTotalPoints()
	{
		if (!partyPointsTracker.isInParty())
		{
			return this.personalTotalPoints - BASE_POINTS + nonPartyPoints;
		}
		return this.personalTotalPoints;
	}

	public double getPersonalPercent()
	{
		if (raidStateTracker.getPlayerCount() == 1)
		{
			return 1.0;
		}

		return (double) getPersonalTotalPoints() / getTotalPoints();
	}

	public int getTotalPoints()
	{
		if (partyPointsTracker.isInParty())
		{
			return partyPointsTracker.getTotalPartyPoints();
		}
		return getPersonalTotalPoints();
	}

	public double getUniqueChance()
	{
		return UniqueChanceCalculator.getUniqueChance(raidLevel, getTotalPoints());
	}

	public double getPetChance()
	{
		return UniqueChanceCalculator.getPetChance(raidLevel, getTotalPoints());
	}

	private void reset()
	{
		this.personalTotalPoints = BASE_POINTS;
		this.personalRoomPoints = 0;
		this.nonPartyPoints = 0;
		this.teamSize = 0;
		this.raidLevel = -1;
		this.wardenDowns = 0;
		this.seenMvpItems.clear();

		partyPointsTracker.clearPartyPointsMap();
		updatePersonalPartyPoints(true);
	}

	private void updatePersonalPartyPoints(boolean sendNow)
	{
		int points = getPersonalTotalPoints();
		if (sendNow)
		{
			partyPointsTracker.sendPointsUpdate(points);
		}
		else
		{
			partyPointsTracker.schedulePointsUpdate(points);
		}
	}

	private String buildPointsMessage()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Total points: ");
		sb.append(ColorUtil.wrapWithColorTag(POINTS_FORMAT.format(getTotalPoints()), Color.red));
		sb.append(", Personal points: ");
		sb.append(ColorUtil.wrapWithColorTag(POINTS_FORMAT.format(getPersonalTotalPoints()), Color.red));
		sb.append(" (");
		sb.append(ColorUtil.wrapWithColorTag(PERCENT_FORMAT.format(getPersonalPercent()), Color.red));
		sb.append(")");
		return sb.toString();
	}

	private void postPointsEvent()
	{
		eventBus.post(new PluginMessage(
			TombsOfAmascutPlugin.EVENT_NAMESPACE,
			EVENT_NAME,
			ImmutableMap.<String, Object>builder()
				.put("version", 1)
				.put("totalPoints", getTotalPoints())
				.put("personalPoints", getPersonalTotalPoints())
				.build()
		));
	}

}
