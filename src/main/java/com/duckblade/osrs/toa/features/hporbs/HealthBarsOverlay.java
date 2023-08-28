/*
 * Copyright (c) 2020, Trevor <https://github.com/Trevor159/runelite-external-plugins/blob/b9d58dd864ce33a23b34eac91865bdb1521a379a/LICENSE>
 * Copyright (c) 2023, LlemonDuck <napkinorton@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.duckblade.osrs.toa.features.hporbs;

import com.duckblade.osrs.toa.TombsOfAmascutConfig;
import com.google.common.base.Strings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Varbits;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;

public class HealthBarsOverlay extends OverlayPanel
{

	@Inject
	private Client client;

	@Inject
	private TombsOfAmascutConfig config;

	@Inject
	public HealthBarsOverlay(Client client, TombsOfAmascutConfig config)
	{
		this.client = client;
		this.config = config;

		panelComponent.setGap(new Point(0, 3));
		if (getPreferredPosition() == null)
		{
			setPreferredPosition(OverlayPosition.TOP_LEFT);
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.hpOrbsMode() != HpOrbMode.HEALTH_BARS)
		{
			return null;
		}

		// solo raid
		if (Strings.isNullOrEmpty(client.getVarcStrValue(1100)))
		{
			String playerName = client.getVarcStrValue(1099);
			double hpFactor = hpFactor(client.getVarbitValue(Varbits.TOA_MEMBER_0_HEALTH) - 1);
			panelComponent.getChildren().add(buildHpBar(playerName, hpFactor));

			return super.render(graphics);
		}

		DoubleHpBarComponent current = new DoubleHpBarComponent();
		for (int i = 0; i < 8; i++)
		{
			String playerName = client.getVarcStrValue(1099 + i);
			double hpFactor = hpFactor(client.getVarbitValue(Varbits.TOA_MEMBER_0_HEALTH + i) - 1);
			if (Strings.isNullOrEmpty(playerName))
			{
				continue;
			}

			if (i % 2 == 0)
			{
				current.setCenterLabel1(playerName);
				current.setValue1(hpFactor);
				panelComponent.getChildren().add(current);
			}
			else
			{
				current.setCenterLabel2(playerName);
				current.setValue2(hpFactor);
				current = new DoubleHpBarComponent();
			}
		}

		return super.render(graphics);
	}

	private static double hpFactor(int hpVarb)
	{
		return (double) Math.max(hpVarb, 0) / 26.0;
	}

	private ProgressBarComponent buildHpBar(String name, double hpFactor)
	{
		ProgressBarComponent hpBar = new ProgressBarComponent();
		hpBar.setBackgroundColor(new Color(102, 15, 16, 230));
		hpBar.setForegroundColor(new Color(0, 146, 54, 230));
		hpBar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.TEXT_ONLY);
		hpBar.setCenterLabel(name);
		hpBar.setValue(hpFactor);
		hpBar.setMinimum(0);
		hpBar.setMaximum(1);
		hpBar.setPreferredSize(new Dimension(60, 20));
		return hpBar;
	}
}