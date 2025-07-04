package com.duckblade.osrs.toa.launcher.debugplugins;

import com.duckblade.osrs.toa.util.FontStyle;
import com.duckblade.osrs.toa.util.OverlayUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.NpcOverrides;
import net.runelite.api.Point;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class AkkhaShadowOverridesOverlay extends Overlay
{

	private final ToaDebugConfig config;

	private final Set<NPC> shadows = new TreeSet<>(Comparator.comparing(NPC::getIndex));

	@Inject
	public AkkhaShadowOverridesOverlay(
		ToaDebugConfig config
	)
	{
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned e)
	{
		if (e.getNpc().getId() == NpcID.AKKHAS_SHADOW)
		{
			shadows.add(e.getNpc());
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned e)
	{
		if (e.getNpc().getId() == NpcID.AKKHAS_SHADOW)
		{
			shadows.remove(e.getNpc());
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.akkhaShadowHsl())
		{
			return null;
		}

		shadows.forEach((npc) ->
		{
			{
				final Model m = npc.getModel();
				final String text = "H: " + m.getOverrideHue() + " S: " + m.getOverrideSaturation() + " L: " + m.getOverrideLuminance() + " X: " + m.getOverrideAmount();
				Point point = npc.getCanvasTextLocation(graphics, text, 0);
				if (point != null)
				{
					point = new Point(point.getX(), point.getY() - 20);
					OverlayUtil.renderTextLocation(graphics, point, text, Color.WHITE, 12, FontStyle.PLAIN.getFont(), true);
				}
			}

			NpcOverrides overrides = npc.getModelOverrides();
			if (overrides == null)
			{
				return;
			}

			StringBuilder sb = new StringBuilder();
			if (overrides.getModelIds() != null)
			{
				sb.append("M: ");
				sb.append(Arrays.toString(overrides.getModelIds()));
			}
			if (overrides.getColorToReplaceWith() != null)
			{
				sb.append("C: ");
				sb.append(Arrays.toString(overrides.getColorToReplaceWith()));
			}
			if (overrides.getTextureToReplaceWith() != null)
			{
				sb.append("T: ");
				sb.append(Arrays.toString(overrides.getTextureToReplaceWith()));
			}

			final String text = sb.toString();
			Point point = npc.getCanvasTextLocation(graphics, text, 0);
			if (point != null)
			{
				point = new Point(point.getX(), point.getY() + 20);
				OverlayUtil.renderTextLocation(graphics, point, text, Color.WHITE, 12, FontStyle.PLAIN.getFont(), true);
			}
		});
		return null;
	}
}
