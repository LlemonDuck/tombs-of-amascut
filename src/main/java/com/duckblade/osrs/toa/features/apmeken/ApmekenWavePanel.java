package com.duckblade.osrs.toa.features.apmeken;

import static com.duckblade.osrs.toa.features.apmeken.Baboon.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
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

	private static final List<Map<Baboon, Integer>> WAVES = ImmutableList.of(
		ImmutableMap.of(BRAWLER, 2),
		ImmutableMap.of(THROWER, 1, MAGE, 1),
		ImmutableMap.of(SHAMAN, 1, BRAWLER, 2),
		ImmutableMap.of(THROWER, 2, VOLATILE, 1),
		ImmutableMap.of(MAGE, 2, CURSED, 1),
		ImmutableMap.of(THROWER, 2, SHAMAN, 1, VOLATILE, 1, CURSED, 1),
		ImmutableMap.of(MAGE, 1, SHAMAN, 2, VOLATILE, 1),
		ImmutableMap.of(BRAWLER, 2, SHAMAN, 2, CURSED, 1),
		ImmutableMap.of(BRAWLER, 1, THROWER, 1, SHAMAN, 1, CURSED, 2),
		ImmutableMap.of(SHAMAN, 2, VOLATILE, 3)
	);

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
		for (Map<Baboon, Integer> wave : WAVES)
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
				spawnLabel.setForeground(baboon.getColor());
				rowPanel.add(spawnLabel);
			});
		}
	}

}
