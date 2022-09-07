package com.duckblade.osrs.toa.features.scabaras;

import java.awt.image.BufferedImage;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

@Singleton
public class ScabarasHelperPanel extends PluginPanel
{

	private static final BufferedImage TILE_PUZZLE_IMAGE = ImageUtil.loadImageResource(ScabarasHelperPanel.class, "tile-puzzle.png");

	public ScabarasHelperPanel()
	{
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(CENTER_ALIGNMENT);

		JLabel header = new JLabel("Orientation is EAST up!");
		header.setAlignmentX(CENTER_ALIGNMENT);
		header.setHorizontalAlignment(SwingConstants.CENTER);
		header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		add(header);

		JLabel tilesImgLabel = new JLabel(new ImageIcon(TILE_PUZZLE_IMAGE));
		tilesImgLabel.setAlignmentX(CENTER_ALIGNMENT);
		tilesImgLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(tilesImgLabel);
	}

}
