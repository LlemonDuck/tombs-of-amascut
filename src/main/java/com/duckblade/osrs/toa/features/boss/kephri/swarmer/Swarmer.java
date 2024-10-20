package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class Swarmer implements PluginLifecycleComponent
{
	private static final int ANIMATION_KEPHRI_DOWN = 9579;
	private static final int ANIMATION_KEPHRI_UP = 9581;
	private static final int ANIMATION_SWARM_LEAK = 9607;
	private static final int ANIMATION_SWARM_DEATH = 9608;

	private static final BufferedImage PANEL_ICON = ImageUtil.loadImageResource(Swarmer.class, "icon.png");

	private final Client client;
	private final ClientToolbar clientToolbar;
	private final EventBus eventBus;

	private SwarmerPanel sidePanel;
	private NavigationButton navButton;

	public static int WaveNumber = 1;
	public static int KephriDownCount = 0;

	private final String ROOM_ENDED_MESSAGE = "Challenge complete: Kephri.";

	private boolean isKephriDowned = false;
	private int lastSpawnTick = -1;

	@Getter
	private final Map<Integer, SwarmNpc> aliveSwarms = new HashMap<>();

	private final ArrayList<SwarmNpc> allSwarms = new ArrayList<>();


	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.swarmerOverlay()
			&& (raidState.isInLobby() || raidState.isInRaid());
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		createSidePanel();
		reset();
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
		removeSidePanel();
		reset();
	}

	private void reset()
	{
		allSwarms.clear();
		aliveSwarms.clear();
		isKephriDowned = false;
		lastSpawnTick = -1;
		WaveNumber = 1;
		KephriDownCount = 0;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		final NPC npc = event.getNpc();
		final int npcId = npc.getId();

		if (isKephriDowned && npcId == NpcID.SCARAB_SWARM_11723)
		{
			SwarmNpc swarm = new SwarmNpc(npc, WaveNumber, KephriDownCount);
			allSwarms.add(swarm);
			aliveSwarms.put(npc.getIndex(), swarm);

			int thisTick = client.getTickCount();
			if (lastSpawnTick != thisTick)
			{
				WaveNumber++;
				lastSpawnTick = thisTick;
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged e)
	{
		if (!(e.getActor() instanceof NPC))
		{
			return;
		}

		NPC npc = ((NPC) e.getActor());
		if (npc.getId() == NpcID.SCARAB_SWARM_11723)
		{
			handleSwarmAnimationChanged(npc);
		}
		else if (npc.getId() == NpcID.KEPHRI || npc.getId() == NpcID.KEPHRI_11720)
		{
			handleKephriAnimationChanged(npc);
		}
	}

	private void handleSwarmAnimationChanged(NPC npc)
	{
		SwarmNpc swarm = aliveSwarms.get(npc.getIndex());
		if (swarm == null)
		{
			return;
		}

		if (npc.getAnimation() == ANIMATION_SWARM_LEAK)
		{
			swarm.setAlive(false);
			swarm.setLeaked(true);
		}
		else if (npc.getAnimation() == ANIMATION_SWARM_DEATH)
		{
			swarm.setAlive(false);
			swarm.setLeaked(true);
		}
	}

	private void handleKephriAnimationChanged(NPC npc)
	{
		if (!isKephriDowned && npc.getAnimation() == ANIMATION_KEPHRI_DOWN)
		{
			isKephriDowned = true;
			KephriDownCount++;
			WaveNumber = 1;
		}
		else if (isKephriDowned && npc.getAnimation() == ANIMATION_KEPHRI_UP)
		{
			isKephriDowned = false;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (
				event.getType().equals(ChatMessageType.GAMEMESSAGE) &&
						event.getMessage().startsWith(ROOM_ENDED_MESSAGE)
		)
		{
			Swarmer.KephriDownCount = 0;

			ArrayList<KephriRoomData> swarmData = new ArrayList<>();

			for (SwarmNpc swarm : allSwarms)
			{
				if (swarm.isLeaked())
				{
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

		for (NPC npc : npcs)
		{
			if (
					npc.getId() == NpcID.SCARAB_SWARM_11723 &&
							npc.getWorldLocation().getX() == worldPoint.getX() && npc.getWorldLocation().getY() == worldPoint.getY()
			)
			{
				return npc;
			}
		}
		return null;
	}

	private void createSidePanel()
	{
		if (sidePanel == null)
		{
			sidePanel = new SwarmerPanel();
		}
		if (navButton == null)
		{
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
