package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidState;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
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

	private int waveNumber = 1;
	private int kephriDownCount = 0;

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
		waveNumber = 1;
		kephriDownCount = 0;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		final NPC npc = event.getNpc();
		final int npcId = npc.getId();

		if (isKephriDowned && npcId == NpcID.SCARAB_SWARM_11723)
		{
			SwarmNpc swarm = new SwarmNpc(npc, waveNumber, kephriDownCount);
			allSwarms.add(swarm);
			aliveSwarms.put(npc.getIndex(), swarm);

			int thisTick = client.getTickCount();
			if (lastSpawnTick != thisTick)
			{
				waveNumber++;
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
			kephriDownCount++;
			waveNumber = 1;
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
			kephriDownCount = 0;

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
