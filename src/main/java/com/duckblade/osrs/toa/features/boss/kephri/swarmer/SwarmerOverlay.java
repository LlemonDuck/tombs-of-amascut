package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Renderable;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.party.PartyMember;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;

@Slf4j
@Singleton
public class SwarmerOverlay extends Overlay implements PluginLifecycleComponent
{
	private static final int ANIMATION_KEPHRI_DOWN = 9579;
	private static final int ANIMATION_KEPHRI_UP = 9581;
	private static final int ANIMATION_SWARM_LEAK = 9607;
	private static final int ANIMATION_SWARM_DEATH = 9608;

	private static final String ROOM_ENDED_MESSAGE = "Challenge complete: Kephri.";
	private static final String ROOM_FAIL_MESSAGE = "Your party failed to complete";

	private final Client client;
	private final ClientThread clientThread;
	private final Hooks hooks;
	private final EventBus eventBus;
	private final OverlayManager overlayManager;
	private final WSClient wsClient;
	private final PartyService partyService;
	private final TombsOfAmascutConfig config;
	private final SwarmerDataManager swarmerDataManager;
	private final SwarmerPanel swarmerPanel;

	private final Map<Integer, SwarmNpc> aliveSwarms = new HashMap<>();
	private final Map<Integer, Map<Integer, Integer>> leaks = new HashMap<>();
	private final Set<Integer> deadSwarmIndexes = new HashSet<>();
	private final Map<Skill, Integer> previousXpMap = new EnumMap<>(Skill.class);

	private int waveNumber;
	private int kephriDownCount;
	private int currentPhase = 1;

	private boolean isKephriDowned;
	private int lastSpawnTick;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	@Inject
	public SwarmerOverlay(Client client, ClientThread clientThread, Hooks hooks, EventBus eventBus, OverlayManager overlayManager, WSClient wsClient, PartyService partyService, TombsOfAmascutConfig config, SwarmerDataManager swarmerDataManager, SwarmerPanel swarmerPanel)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.hooks = hooks;
		this.eventBus = eventBus;
		this.overlayManager = overlayManager;
		this.wsClient = wsClient;
		this.partyService = partyService;
		this.config = config;
		this.swarmerDataManager = swarmerDataManager;
		this.swarmerPanel = swarmerPanel;

		setPriority(Overlay.PRIORITY_HIGH);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return config.swarmerOverlay()
			&& raidState.getCurrentRoom() == RaidRoom.KEPHRI;
	}

	@Override
	public void startUp()
	{
		eventBus.register(this);
		overlayManager.add(this);
		wsClient.registerMessage(SwarmKilled.class);
		clientThread.invoke(this::initializePreviousXpMap);
		hooks.registerRenderableDrawListener(drawListener);
		reset();
	}

	@Override
	public void shutDown()
	{
		hooks.unregisterRenderableDrawListener(drawListener);
		wsClient.unregisterMessage(SwarmKilled.class);
		eventBus.unregister(this);
		overlayManager.remove(this);
		reset();
	}

	private void reset()
	{
		leaks.clear();
		aliveSwarms.clear();
		deadSwarmIndexes.clear();
		previousXpMap.clear();
		isKephriDowned = false;
		lastSpawnTick = -1;
		waveNumber = 0;
		kephriDownCount = 0;
		currentPhase = 1;
	}

	private void initializePreviousXpMap()
	{
		previousXpMap.clear();
		for (Skill skill : Skill.values())
		{
			previousXpMap.put(skill, client.getSkillExperience(skill));
		}
	}

	private boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (!config.hideOnHit() && !config.hideHighSwarm())
		{
			return true;
		}

		if (renderable instanceof NPC)
		{
			NPC npc = (NPC) renderable;
			if (npc.getId() != NpcID.SCARAB_SWARM_11723)
			{
				return true;
			}

			int npcIndex = npc.getIndex();

			// Don't draw dead swarms (hit by player)
			if (config.hideOnHit() && deadSwarmIndexes.contains(npcIndex))
			{
				return false;
			}

			// Check if we should hide high wave swarms
			if (config.hideHighSwarm())
			{
				SwarmNpc swarm = aliveSwarms.get(npcIndex);
				if (swarm != null && swarm.getWaveSpawned() > getCurrentThreshold())
				{
					return false;
				}
			}
		}
		return true;
	}

	private int getCurrentThreshold()
	{
		return currentPhase == 1 ? config.waveThreshold() : config.waveThresholdPhase2();
	}

	public boolean isHidden(NPC npc)
	{
		if (npc.getId() != NpcID.SCARAB_SWARM_11723)
		{
			return false;
		}

		if (config.hideOnHit() && deadSwarmIndexes.contains(npc.getIndex()))
		{
			return true;
		}

		if (config.hideHighSwarm())
		{
			SwarmNpc swarm = aliveSwarms.get(npc.getIndex());
			if (swarm != null && swarm.getWaveSpawned() > getCurrentThreshold())
			{
				return true;
			}
		}

		return false;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		final NPC npc = event.getNpc();
		final int npcId = npc.getId();

		if (isKephriDowned && npcId == NpcID.SCARAB_SWARM_11723)
		{
			int thisTick = client.getTickCount();
			if (lastSpawnTick != thisTick)
			{
				waveNumber++;
				lastSpawnTick = thisTick;
			}

			SwarmNpc swarm = new SwarmNpc(npc, waveNumber);
			aliveSwarms.put(npc.getIndex(), swarm);

			// If this is a high wave swarm and we're hiding them, mark it as dead immediately
			if (config.hideHighSwarm() && waveNumber > getCurrentThreshold())
			{
				deadSwarmIndexes.add(npc.getIndex());
				if (!npc.isDead())
				{
					npc.setDead(true);
				}
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if (npc.getId() == NpcID.SCARAB_SWARM_11723)
		{
			deadSwarmIndexes.remove(npc.getIndex());
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (!config.hideOnHit() || !isKephriDowned || aliveSwarms.isEmpty())
		{
			return;
		}

		Skill skill = event.getSkill();
		int xpAfter = client.getSkillExperience(skill);
		Integer xpBeforeObj = previousXpMap.get(skill);

		// Allow the first XP drop we see to immediately hide a swarm
		if (xpBeforeObj == null)
		{
			previousXpMap.put(skill, xpAfter);
		}
		else
		{
			previousXpMap.put(skill, xpAfter);
			if (xpAfter <= xpBeforeObj)
			{
				return;
			}
		}

		// XP gained - hide the swarm we're currently attacking
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		Actor interacting = player.getInteracting();
		if (!(interacting instanceof NPC))
		{
			return;
		}

		NPC interactingNpc = (NPC) interacting;
		if (interactingNpc.getId() != NpcID.SCARAB_SWARM_11723)
		{
			return;
		}

		int npcIndex = interactingNpc.getIndex();
		SwarmNpc scarab = aliveSwarms.get(npcIndex);

		if (scarab != null && !deadSwarmIndexes.contains(npcIndex))
		{
			// Add to dead list first so shouldDraw filters it immediately
			deadSwarmIndexes.add(npcIndex);

			// Remove from alive swarms so overlay doesn't render it
			aliveSwarms.remove(npcIndex);

			// Set the NPC as dead
			if (!interactingNpc.isDead())
			{
				interactingNpc.setDead(true);
			}

			broadcastSwarmKilled(npcIndex);
		}
	}

	@Subscribe
	public void onSwarmKilled(SwarmKilled message)
	{
		PartyMember local = partyService.getLocalMember();
		if (local != null && local.getMemberId() == message.getMemberId())
		{
			return;
		}

		clientThread.invokeLater(() -> markSwarmKilled(message.getNpcIndex()));
	}

	private void broadcastSwarmKilled(int npcIndex)
	{
		if (partyService.getLocalMember() != null)
		{
			partyService.send(new SwarmKilled(npcIndex));
		}

		markSwarmKilled(npcIndex);
	}

	private void markSwarmKilled(int npcIndex)
	{
		deadSwarmIndexes.add(npcIndex);
		aliveSwarms.remove(npcIndex);

		for (NPC npc : client.getNpcs())
		{
			if (npc != null && npc.getIndex() == npcIndex && !npc.isDead())
			{
				npc.setDead(true);
				break;
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
			aliveSwarms.remove(npc.getIndex());
			leaks.compute(kephriDownCount, (downs, waveMap) ->
			{
				Map<Integer, Integer> waveLeaks = waveMap != null ? waveMap : new HashMap<>();
				waveLeaks.compute(swarm.getWaveSpawned(), (wave, count) -> count != null ? count + 1 : 1);
				return waveLeaks;
			});
		}
		else if (npc.getAnimation() == ANIMATION_SWARM_DEATH)
		{
			aliveSwarms.remove(npc.getIndex());
		}
	}

	private void handleKephriAnimationChanged(NPC npc)
	{
		if (!isKephriDowned && npc.getAnimation() == ANIMATION_KEPHRI_DOWN)
		{
			isKephriDowned = true;
			kephriDownCount++;
			waveNumber = 0;
			aliveSwarms.clear();
			deadSwarmIndexes.clear();

			// Cycle phases when Kephri goes down
			if (kephriDownCount > 1)
			{
				currentPhase = (currentPhase == 1) ? 2 : 1;
			}
		}
		else if (isKephriDowned && npc.getAnimation() == ANIMATION_KEPHRI_UP)
		{
			isKephriDowned = false;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!event.getType().equals(ChatMessageType.GAMEMESSAGE))
		{
			return;
		}

		String message = event.getMessage();

		boolean saveData = message.startsWith(ROOM_ENDED_MESSAGE);
		if (config.swarmerSaveOnFail())
		{
			saveData = saveData || message.startsWith(ROOM_FAIL_MESSAGE);
		}

		if (saveData)
		{
			List<SwarmerRoomData> swarmData = new ArrayList<>();
			for (Map.Entry<Integer, Map<Integer, Integer>> e : leaks.entrySet())
			{
				int down = e.getKey();
				for (Map.Entry<Integer, Integer> f : e.getValue().entrySet())
				{
					int wave = f.getKey();
					int leaks = f.getValue();
					swarmData.add(new SwarmerRoomData(down, wave, leaks));
				}
			}
			swarmerDataManager.saveRaidData(swarmData)
				.thenRun(() ->
				{
					if (config.swarmerSidePanel() != SwarmerPanelManager.PanelMode.NEVER)
					{
						swarmerPanel.updateRecentRaids();
					}
				});
		}

		if (message.startsWith(ROOM_FAIL_MESSAGE))
		{
			reset();
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		aliveSwarms.values()
			.stream()
			.filter(swarm -> !shouldFilterSwarm(swarm))
			.collect(Collectors.groupingBy(swarm -> swarm.getNpc().getWorldLocation()))
			.values()
			.forEach(tileSwarms ->
			{
				int stackOffset = 0;
				for (SwarmNpc swarm : tileSwarms)
				{
					this.draw(graphics, swarm, stackOffset);
					stackOffset += graphics.getFontMetrics().getHeight();
				}
			});
		return null;
	}

	private boolean shouldFilterSwarm(SwarmNpc swarm)
	{
		// Don't show if the swarm is marked as dead (hit by player)
		if (config.hideOnHit() && deadSwarmIndexes.contains(swarm.getNpc().getIndex()))
		{
			return true;
		}

		// Don't show if the NPC itself is being hidden
		if (isHidden(swarm.getNpc()))
		{
			return true;
		}

		// Don't show if hiding high wave numbers and this swarm is above phase-specific threshold
		if (config.hideHighNumber())
		{
			if (swarm.getWaveSpawned() > getCurrentThreshold())
			{
				return true;
			}
		}
		return false;
	}

	private void draw(Graphics2D graphics, SwarmNpc swarmer, int offset)
	{
		String text = String.valueOf(swarmer.getWaveSpawned());

		Point canvasTextLocation = swarmer.getNpc().getCanvasTextLocation(graphics, text, 0);
		if (canvasTextLocation == null)
		{
			return;
		}
		int x = canvasTextLocation.getX();
		int y = canvasTextLocation.getY() + offset;

		Font font = new Font(config.swarmerFontType().toString(), config.useBoldFont() ? Font.BOLD : Font.PLAIN, config.swarmerFontSize());
		FontRenderContext frc = graphics.getFontRenderContext();
		TextLayout tl = new TextLayout(text, font, frc);
		Shape outline = tl.getOutline(null);
		graphics.translate(x, y);
		graphics.setStroke(new BasicStroke(3));
		graphics.setColor(Color.BLACK);
		graphics.draw(outline);
		graphics.setColor(config.swarmerFontColor());
		graphics.fill(outline);
		graphics.translate(-x, -y);
	}
}
