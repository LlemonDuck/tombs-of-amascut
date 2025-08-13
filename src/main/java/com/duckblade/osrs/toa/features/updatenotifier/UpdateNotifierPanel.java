package com.duckblade.osrs.toa.features.updatenotifier;

import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.PluginPanel;

public class UpdateNotifierPanel extends PluginPanel
{

	UpdateNotifierPanel(List<String> updates, Runnable acknowledgeCallback)
	{
		super(true);

		setBorder(BorderFactory.createEmptyBorder(20, 5, 0, 5));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel content = new JLabel();
		content.setText(buildHtml(updates));
		content.setHorizontalAlignment(SwingConstants.CENTER);
		content.setAlignmentX(CENTER_ALIGNMENT);
		content.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
		add(content);

		JButton acknowledge = new JButton("Close");
		acknowledge.addActionListener(ignored -> acknowledgeCallback.run());
		acknowledge.setAlignmentX(CENTER_ALIGNMENT);
		add(acknowledge);
	}

	private static String buildHtml(List<String> updates)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");

		sb.append("<h1 style=\"width: 1%; margin: 0 auto;\">ToA\u00A0Plugin\u00A0Updates</h1>");
		sb.append("<br/><br/>");

		sb.append("<ul style=\"list-style-type: disc; margin: 0px 15px;\">");
		for (String update : updates)
		{
			sb.append("<li>");
			sb.append(update);
			sb.append("</li>");
			sb.append("<br/>");
		}
		sb.append("</ul><br/>");

		sb.append("<em>With all updates, please check the plugin config to enable or disable features you wish to use.</em>");

		sb.append("</html>");
		return sb.toString();
	}
}
