package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.NpcUtil;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class Swarmer implements PluginLifecycleComponent
{
	private static final BufferedImage PANEL_ICON = ImageUtil.loadImageResource(Swarmer.class, "icon.png");

	private final Client client;
	public final TombsOfAmascutConfig config;
	private final ClientToolbar clientToolbar;
	private final EventBus eventBus;
	private final NpcUtil npcUtil;

	private SwarmerPanel sidePanel;
	private NavigationButton navButton;

	public static final int SWARM_NPC_ID = 11723;
	public static final int SWARM_LEAK_ANIMATION_ID = 9607;
	public static final int SWARM_DEATH_ANIMATION_ID = 9608;
	public static int WaveNumber = 1;
	public static int KephriDownCount = 0;

	private final int KEPHRI_DOWNED_NPC_ID = 11720;
	private final int[] KEPHRI_ALIVE_NPC_IDS = {11721, 11719};

	private final String ROOM_ENDED_MESSAGE = "Challenge complete: Kephri.";

	private boolean isKephriDowned = false;

	@Getter
	private final ArrayList<SwarmNpc> aliveSwarms = new ArrayList<>();

	private final ArrayList<SwarmNpc> allSwarms = new ArrayList<>();


	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.swarmerOverlay();
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		createSidePanel();
		allSwarms.clear();
		aliveSwarms.clear();
		isKephriDowned = false;
		WaveNumber = 1;
		KephriDownCount = 0;
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		removeSidePanel();
		allSwarms.clear();
		aliveSwarms.clear();
		isKephriDowned = false;
		WaveNumber = 1;
		KephriDownCount = 0;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGIN_SCREEN) {
			isKephriDowned = false;
			WaveNumber = 1;
			KephriDownCount = 0;
			aliveSwarms.clear();
			allSwarms.clear();
			sidePanel.clearRecentRaids();
		} else if (e.getGameState() == GameState.LOGGED_IN) {
			sidePanel.updateRecentRaids();
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		final NPC npc = event.getNpc();
		final int npcId = npc.getId();


		if (isKephriDowned && npcId == SWARM_NPC_ID) {
			SwarmNpc swarm = new SwarmNpc(npc);
			allSwarms.add(swarm);
			if (aliveSwarms.stream().noneMatch(s -> s.getIndex() == swarm.getIndex())) {
				aliveSwarms.add(swarm);
				getCardinalNpcs(npc).forEach(cardinalNpc ->
				{
					SwarmNpc cardinalSwarm = new SwarmNpc(cardinalNpc);
					if (aliveSwarms.stream().noneMatch(s -> s.getIndex() == cardinalSwarm.getIndex())) {
						aliveSwarms.add(cardinalSwarm);
					}
				});
				Swarmer.WaveNumber++;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick ignoredEvent)
	{
		for (SwarmNpc swarm : allSwarms) {
			if (swarm.isAlive()) {
				if (swarm.getNpc().getAnimation() == SWARM_LEAK_ANIMATION_ID) {
					swarm.setLeaked(true);
					swarm.setAlive(false);
					aliveSwarms.removeIf(s -> s.getIndex() == swarm.getIndex());
				} else if (npcUtil.isDying(swarm.getNpc()) || swarm.getNpc().getAnimation() == SWARM_DEATH_ANIMATION_ID) {
					swarm.setAlive(false);
					aliveSwarms.removeIf(s -> s.getIndex() == swarm.getIndex());
				}
			}
		}

		IndexedObjectSet<? extends NPC> npcs = client.getTopLevelWorldView().npcs();

		for (NPC npc : npcs) {
			final int npcId = npc.getId();
			if (!isKephriDowned && npcId == KEPHRI_DOWNED_NPC_ID) {
				isKephriDowned = true;
				KephriDownCount++;
				Swarmer.WaveNumber = 1;
			} else if (isKephriDowned && Arrays.stream(KEPHRI_ALIVE_NPC_IDS).anyMatch(id -> id == npcId)) {
				isKephriDowned = false;
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (
				event.getType().equals(ChatMessageType.GAMEMESSAGE) &&
						event.getMessage().startsWith(ROOM_ENDED_MESSAGE)
		) {
			Swarmer.KephriDownCount = 0;

			ArrayList<KephriRoomData> swarmData = new ArrayList<>();

			for (SwarmNpc swarm : allSwarms) {
				if (swarm.isLeaked()) {
					swarmData.stream()
							.filter(s -> s.getWave() == swarm.getWaveSpawned() && s.getDown() == swarm.getPhase())
							.findFirst()
							.ifPresentOrElse(
									raidData -> raidData.setLeaks(raidData.getLeaks() + 1),
									() -> swarmData.add(new KephriRoomData(swarm.getPhase(), swarm.getWaveSpawned(), 1))
							);
				}
			}

			KephriRoomData.saveRaidData(swarmData);
			allSwarms.clear();
			sidePanel.updateRecentRaids();
		}
	}

	private List<NPC> getCardinalNpcs(NPC npc)
	{
		WorldPoint npcLocation = npc.getWorldLocation();
		int npcX = npcLocation.getX();
		int npcY = npcLocation.getY();
		WorldPoint north = new WorldPoint(npcX, npcY - 1, client.getTopLevelWorldView().getPlane());
		WorldPoint south = new WorldPoint(npcX, npcY + 1, client.getTopLevelWorldView().getPlane());
		WorldPoint east = new WorldPoint(npcX + 1, npcY, client.getTopLevelWorldView().getPlane());
		WorldPoint west = new WorldPoint(npcX - 1, npcY, client.getTopLevelWorldView().getPlane());

		NPC northNpc = getNpcOnTile(north);
		NPC southNpc = getNpcOnTile(south);
		NPC eastNpc = getNpcOnTile(east);
		NPC westNpc = getNpcOnTile(west);

		List<NPC> cardinalNpcs = new ArrayList<>();
		if (northNpc != null)
			cardinalNpcs.add(northNpc);
		if (southNpc != null)
			cardinalNpcs.add(southNpc);
		if (eastNpc != null)
			cardinalNpcs.add(eastNpc);
		if (westNpc != null)
			cardinalNpcs.add(westNpc);

		return cardinalNpcs;
	}

	private NPC getNpcOnTile(WorldPoint worldPoint)
	{
		IndexedObjectSet<? extends NPC> npcs = client.getTopLevelWorldView().npcs();

		for (NPC npc : npcs) {
			if (
					npc.getId() == SWARM_NPC_ID &&
							npc.getWorldLocation().getX() == worldPoint.getX() && npc.getWorldLocation().getY() == worldPoint.getY()
			) {
				return npc;
			}
		}
		return null;
	}

	private void createSidePanel()
	{
		if (sidePanel == null) {
			sidePanel = new SwarmerPanel();
		}
		if (navButton == null) {
			navButton = NavigationButton.builder()
					.tooltip("Swarmer")
					.icon(PANEL_ICON)
					.priority(999)
					.panel(sidePanel)
					.build();
		}
		clientToolbar.addNavigation(navButton);
	}

	private void removeSidePanel()
	{
		clientToolbar.removeNavigation(navButton);
	}
}
