package com.duckblade.osrs.toa.features.invocationpresets;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.duckblade.osrs.toa.TombsOfAmascutConfigCache;
import com.duckblade.osrs.toa.module.PluginLifecycleComponent;
import com.duckblade.osrs.toa.util.Invocation;
import com.google.common.collect.Sets;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

@Singleton
public class InvocationPresetsOverlay extends Overlay implements PluginLifecycleComponent
{

	private static final int RAID_LEVEL_METER_PARENT_ID = 774;
	private static final int RAID_LEVEL_METER_CHILD_ID = 82;

	private final OverlayManager overlayManager;
	private final Client client;
	private final TombsOfAmascutConfig config;
	private final InvocationPresetsManager invocationPresetsManager;

	@Inject
	public InvocationPresetsOverlay(OverlayManager overlayManager, Client client, TombsOfAmascutConfigCache cachedConfig, InvocationPresetsManager invocationPresetsManager)
	{
		this.overlayManager = overlayManager;
		this.client = client;
		this.config = cachedConfig;
		this.invocationPresetsManager = invocationPresetsManager;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public boolean isConfigEnabled(TombsOfAmascutConfig config)
	{
		return config.invocationPresetsEnable();
	}

	@Override
	public void startUp()
	{
		overlayManager.add(this);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(this);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		InvocationPreset preset = invocationPresetsManager.getCurrentPreset();
		if (preset == null)
		{
			return null;
		}

		boolean matching = preset.getInvocations().equals(invocationPresetsManager.getActiveInvocations());
		if (!matching)
		{
			Sets.SetView<Invocation> toEnable = Sets.difference(preset.getInvocations(), invocationPresetsManager.getActiveInvocations());
			renderSet(graphics, config.invocationPresetsEnableColor(), toEnable);

			Sets.SetView<Invocation> toDisable = Sets.difference(invocationPresetsManager.getActiveInvocations(), preset.getInvocations());
			renderSet(graphics, config.invocationPresetsDisableColor(), toDisable);
		}

		// current preset view
		Widget w = client.getWidget(RAID_LEVEL_METER_PARENT_ID, RAID_LEVEL_METER_CHILD_ID);
		if (w != null && !w.isHidden())
		{
			graphics.setFont(FontManager.getRunescapeSmallFont());
			String text = "Preset: " + preset.getName() + (matching ? "" : " !!!");
			int textWidth = graphics.getFontMetrics().stringWidth(text);

			Rectangle barBounds = w.getBounds();
			int textX = barBounds.x + (barBounds.width / 2) - (textWidth / 2);
			int textY = barBounds.y + barBounds.height + 12;

			graphics.setColor(Color.black);
			graphics.drawString(text, textX + 1, textY + 1);
			graphics.setColor(matching ? Color.green : Color.red);
			graphics.drawString(text, textX, textY);
		}

		return null;
	}

	private void renderSet(Graphics2D graphics, Color color, Set<Invocation> invocations)
	{
		for (Invocation invoc : invocations)
		{
			Widget invocW = invocationPresetsManager.getInvocationWidget(invoc);
			if (invocW == null || invocW.isHidden() || invocW.getParent().isHidden())
			{
				return;
			}

			Rectangle toRender = invocW.getParent().getBounds().intersection(invocW.getBounds());
			graphics.setColor(new Color(color.getRGB()));
			graphics.draw(toRender);
			graphics.setColor(color);
			graphics.fill(toRender);
		}
	}
}
