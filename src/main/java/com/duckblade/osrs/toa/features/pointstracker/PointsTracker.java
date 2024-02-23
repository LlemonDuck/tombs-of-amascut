package com.duckblade.osrs.toa.features.pointstracker;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
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
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Varbits;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.ColorUtil;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PointsTracker implements PluginLifecycleComponent
{

	static final NumberFormat POINTS_FORMAT = NumberFormat.getInstance();
	static final NumberFormat PERCENT_FORMAT = new DecimalFormat("#.##%");

	private static final String START_MESSAGE = "You enter the Tombs of Amascut";
	private static final String DEATH_MESSAGE = "You have died";
	private static final String ROOM_FAIL_MESSAGE = "Your party failed to complete";
	private static final String ROOM_FINISH_MESSAGE = "Challenge complete";

	private static final int BASE_POINTS = 5000;
	private static final int MAX_ROOM_POINTS = 20_000;

	// i'm not sure whether BASE_POINTS should be added
	// i.e. is it 64k available to earn pre- or post- 5000 pt subtraction
	private static final int MAX_TOTAL_POINTS = 64_000 + BASE_POINTS;

	private static final int ANIMATION_ID_WARDEN_DOWN = 9670;

	private static final Map<Integer, Double> DAMAGE_POINTS_FACTORS = ImmutableMap.<Integer, Double>builder()
		.put(NpcID.CORE, 0.0)
		.put(NpcID.CORE_11771, 0.0)
		.put(NpcID.ENERGY_SIPHON, 0.0)
		.put(NpcID.BOULDER_11782, 0.0)
		.put(NpcID.BOULDER_11783, 0.0)
		.put(NpcID.BABOON_BRAWLER, 1.2)
		.put(NpcID.BABOON_BRAWLER_11712, 1.2)
		.put(NpcID.BABOON_THROWER, 1.2)
		.put(NpcID.BABOON_THROWER_11713, 1.2)
		.put(NpcID.BABOON_MAGE, 1.2)
		.put(NpcID.BABOON_MAGE_11714, 1.2)
		.put(NpcID.BABOON_SHAMAN, 1.2)
		.put(NpcID.VOLATILE_BABOON, 1.2)
		.put(NpcID.CURSED_BABOON, 1.2)
		.put(NpcID.BABOON_THRALL, 1.2)
		.put(NpcID.BABA, 2.0)
		.put(NpcID.BABA_11779, 2.0)
		.put(NpcID.BABA_11780, 2.0)
		.put(NpcID.ZEBAK, 1.5)
		.put(NpcID.ZEBAK_11730, 1.5)
		.put(NpcID.ZEBAK_11732, 1.5)
		.put(NpcID.ZEBAK_11733, 1.5)
		.put(NpcID.SPITTING_SCARAB, 0.5)
		.put(NpcID.SOLDIER_SCARAB, 0.5)
		.put(NpcID.ARCANE_SCARAB, 0.5)
		.put(NpcID.HETS_SEAL_WEAKENED, 2.5)
		.put(NpcID.OBELISK_11750, 1.5)
		.put(NpcID.OBELISK_11751, 1.5)
		.put(NpcID.OBELISK_11752, 1.5)
		.put(NpcID.ELIDINIS_WARDEN, 0.0) // non-combat wardens (prevents extra points during p1->p2 transition)
		.put(NpcID.ELIDINIS_WARDEN_11748, 0.0)
		.put(NpcID.TUMEKENS_WARDEN, 0.0)
		.put(NpcID.TUMEKENS_WARDEN_11749, 0.0)
		.put(NpcID.ELIDINIS_WARDEN_11759, 0.0)
		.put(NpcID.TUMEKENS_WARDEN_11760, 0.0)
		.put(NpcID.ELIDINIS_WARDEN_11753, 2.0) // p2 wardens
		.put(NpcID.ELIDINIS_WARDEN_11754, 2.0)
		.put(NpcID.ELIDINIS_WARDEN_11755, 0.0) // downed
		.put(NpcID.TUMEKENS_WARDEN_11756, 2.0)
		.put(NpcID.TUMEKENS_WARDEN_11757, 2.0)
		.put(NpcID.TUMEKENS_WARDEN_11758, 0.0) // downed
		.put(NpcID.ELIDINIS_WARDEN_11761, 2.5) // p3 wardens
		.put(NpcID.ELIDINIS_WARDEN_11763, 2.5)
		.put(NpcID.TUMEKENS_WARDEN_11762, 2.5)
		.put(NpcID.TUMEKENS_WARDEN_11764, 2.5)
		.build();

	// these have a cap at 3 "downs"
	private static final ImmutableSet<Integer> P2_WARDENS = ImmutableSet.of(
		NpcID.ELIDINIS_WARDEN_11753,
		NpcID.ELIDINIS_WARDEN_11754,
		NpcID.ELIDINIS_WARDEN_11755,
		NpcID.TUMEKENS_WARDEN_11756,
		NpcID.TUMEKENS_WARDEN_11757,
		NpcID.TUMEKENS_WARDEN_11758
	);

	private static final ImmutableSet<Integer> MVP_ITEMS = ImmutableSet.of(
		ItemID.FANG_27219,
		ItemID.SCARAB_DUNG,
		ItemID.BIG_BANANA,
		ItemID.ELDRITCH_ASHES
	);

	private static final ImmutableSet<Integer> WARDEN_HITSPLAT_TYPES = ImmutableSet.of(
		53, // hit
		55 // max hit
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
		raidLevel = client.getVarbitValue(Varbits.TOA_RAID_LEVEL);
	}

	@Subscribe
	public void onRaidStateChanged(RaidStateChanged e)
	{
		teamSize = e.getNewState().getPlayerCount();
		if (e.getPreviousState() == null || e.getPreviousState().getCurrentRoom() == null)
		{
			return;
		}

		switch (e.getPreviousState().getCurrentRoom())
		{
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
			personalRoomPoints = 0;
			updatePersonalPartyPoints(false);
		}
		else if (e.getMessage().startsWith(ROOM_FINISH_MESSAGE))
		{
			personalTotalPoints = Math.min(MAX_TOTAL_POINTS, personalTotalPoints + personalRoomPoints);
			personalRoomPoints = 0;

			boolean wardens = e.getMessage().contains("Wardens");
			updatePersonalPartyPoints(wardens);

			if (wardens && config.pointsTrackerPostRaidMessage())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", buildPointsMessage(), "", false);
			}
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
		if (e.getHitsplat().isMine() || WARDEN_HITSPLAT_TYPES.contains(e.getHitsplat().getHitsplatType()))
		{
			this.personalRoomPoints = (int) Math.min(MAX_ROOM_POINTS, personalRoomPoints + e.getHitsplat().getAmount() * factor);
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
		return this.personalTotalPoints + this.personalRoomPoints - BASE_POINTS;
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
		return getPersonalTotalPoints() + nonPartyPoints;
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
		int points = Math.min(MAX_TOTAL_POINTS, personalTotalPoints + personalRoomPoints) - BASE_POINTS;
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

}
