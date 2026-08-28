package com.duckblade.osrs.toa.features.apmeken;

import java.util.Map;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

@Singleton
public class ApmekenWavePanel extends PluginPanel
{

	public ApmekenWavePanel()
	{
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(CENTER_ALIGNMENT);

		JLabel headerLabel = new JLabel("Apmeken Waves");
		headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
		headerLabel.setAlignmentX(CENTER_ALIGNMENT);
		headerLabel.setFont(FontManager.getRunescapeFont().deriveFont(24f));
		add(headerLabel);

		int waveNum = 1;
		for (Map<Baboon, Integer> wave : Baboon.WAVES)
		{
			JPanel rowPanel = new JPanel();
			rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.Y_AXIS));
			rowPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			rowPanel.setAlignmentX(CENTER_ALIGNMENT);
			add(rowPanel);

			rowPanel.add(new JLabel("Wave " + waveNum++));
			wave.forEach((baboon, count) ->
			{
				JLabel spawnLabel = new JLabel(count + "x " + baboon.getDisplayName());
				spawnLabel.setHorizontalAlignment(SwingConstants.LEFT);
				spawnLabel.setFont(FontManager.getRunescapeFont());
				spawnLabel.setForeground(baboon.getSidePanelColor());
				rowPanel.add(spawnLabel);
			});
		}
	}

}
