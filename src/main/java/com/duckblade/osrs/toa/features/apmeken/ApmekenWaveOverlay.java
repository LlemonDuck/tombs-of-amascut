package com.duckblade.osrs.toa.features.apmeken;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.RaidRoom;
import com.duckblade.osrs.toa.util.RaidState;
import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Point;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Singleton
public class ApmekenWaveOverlay
	extends OverlayPanel
	implements PluginLifecycleComponent
{

	private static final ImmutableSet<Point> SPAWN_POINTS = ImmutableSet.of(
		new Point(24, 40),
		new Point(32, 40),
		new Point(40, 40),
		new Point(24, 24),
		new Point(32, 24),
		new Point(40, 24)
	);

	private int wave = 0;
	private boolean queueWaveProgress = false;

	@Inject
	public ApmekenWaveOverlay()
	{
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		setPreferredPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public boolean isEnabled(TombsOfAmascutConfig config, RaidState raidState)
	{
		return raidState.getCurrentRoom() == RaidRoom.APMEKEN &&
			config.apmekenWaveHelperMode() == ApmekenWaveHelperMode.OVERLAY;
	}

	@Override
	public void startUp()
	{
		queueWaveProgress = false;
		wave = 0;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		getPanelComponent().getChildren()
			.add(TitleComponent.builder()
				.text(String.format("Wave %d/%d", wave, Baboon.WAVES.size()))
				.build());

		int currentWaveIx = Math.max(wave - 1, 0);
		Baboon.WAVES.get(currentWaveIx)
			.forEach(this::renderEntry);

		if ((currentWaveIx + 1) >= Baboon.WAVES.size())
		{
			return super.render(graphics);
		}

		getPanelComponent().getChildren()
			.add(TitleComponent.builder()
				.text("")
				.build());

		getPanelComponent().getChildren()
			.add(TitleComponent.builder()
				.text("Next Wave")
				.build());

		Baboon.WAVES.get(currentWaveIx + 1)
			.forEach(this::renderEntry);

		return super.render(graphics);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned e)
	{
		int x = e.getNpc().getWorldLocation().getRegionX();
		int y = e.getNpc().getWorldLocation().getRegionY();
		Point point = new Point(x, y);

		if (SPAWN_POINTS.contains(point))
		{
			queueWaveProgress = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		if (queueWaveProgress)
		{
			wave++;
			queueWaveProgress = false;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (e.getType() == ChatMessageType.GAMEMESSAGE && e.getMessage().startsWith("Your party failed to complete the challenge"))
		{
			reset();
		}
	}

	private void reset()
	{
		queueWaveProgress = false;
		wave = 0;
	}

	private void renderEntry(Baboon baboon, int count)
	{
		String name = baboon.getDisplayName();
		Color color = baboon.getOverlayColor();

		getPanelComponent().getChildren()
			.add(LineComponent.builder()
				.left(count + "x")
				.right(name)
				.rightColor(color)
				.build());
	}
}
