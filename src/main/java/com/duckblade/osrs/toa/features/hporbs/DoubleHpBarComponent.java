/*
 * Copyright (c) 2018, Cameron <moberg@tuta.io>
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

import com.google.common.base.Strings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.TextComponent;

@Setter
public class DoubleHpBarComponent implements LayoutableRenderableEntity
{

	private double value1;
	private String centerLabel1;
	private double value2;
	private String centerLabel2;
	private Color foregroundColor = new Color(0, 146, 54, 230);
	private Color backgroundColor = new Color(102, 15, 16, 230);
	private Color fontColor = Color.WHITE;
	private Point preferredLocation = new Point();
	private Dimension preferredSize = new Dimension(ComponentConstants.STANDARD_WIDTH, 16);
	private int gap = 3;

	@Getter
	private final Rectangle bounds = new Rectangle();

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final int baseX = preferredLocation.x;
		final int baseY = preferredLocation.y;

		final int totalWidth = preferredSize.width;
		final int totalHeight = Math.max(preferredSize.height, 16);

		final int barWidth = (totalWidth - gap) / 2;

		drawBar(
			graphics,
			centerLabel1,
			value1,
			baseX,
			baseY,
			barWidth,
			totalHeight
		);

		if (!Strings.isNullOrEmpty(centerLabel2))
		{
			drawBar(
				graphics,
				centerLabel2,
				value2,
				baseX + barWidth + gap,
				baseY,
				barWidth,
				totalHeight
			);
		}

		final Dimension dimension = new Dimension(totalWidth, totalHeight);
		bounds.setLocation(preferredLocation);
		bounds.setSize(dimension);
		return dimension;
	}

	private void drawBar(Graphics2D graphics, String name, double value, int baseX, int baseY, int width, int height)
	{
		final FontMetrics metrics = graphics.getFontMetrics();
		if (metrics.stringWidth(name) > width)
		{
			name = trimmedName(metrics, name, width);
		}

		final int progressFill = (int) (width * Math.min(1, value));
		final int progressTextX = baseX + (width - metrics.stringWidth(name)) / 2;
		final int progressTextY = baseY + ((height - metrics.getHeight()) / 2) + metrics.getHeight();

		graphics.setColor(backgroundColor);
		graphics.fillRect(baseX, baseY, width, height);
		graphics.setColor(foregroundColor);
		graphics.fillRect(baseX, baseY, progressFill, height);

		final TextComponent textComponent1 = new TextComponent();
		textComponent1.setPosition(new Point(progressTextX, progressTextY));
		textComponent1.setColor(fontColor);
		textComponent1.setText(name);
		textComponent1.render(graphics);
	}

	private static String trimmedName(FontMetrics metrics, String name, int maxWidth)
	{
		String runningName = name;
		while (metrics.stringWidth(runningName + "...") > maxWidth)
		{
			runningName = runningName.substring(0, runningName.length() - 1);
		}

		return runningName + "...";
	}
}
