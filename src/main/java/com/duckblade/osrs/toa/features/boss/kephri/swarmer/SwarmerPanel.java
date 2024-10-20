package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.inject.Singleton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.runelite.client.ui.PluginPanel;

@Singleton
public class SwarmerPanel extends PluginPanel
{
	private static final String[] LEAKS_COLUMN_NAMES = {"Down", "Wave", "Leaks"};

	private static final Color textColor = Color.WHITE;
	private static final Color tableColor1 = new Color(0x1F1F1F);
	private static final Color tableColor2 = new Color(0x2D2D2D);

	private final DefaultListModel<String> raidsListModel;
	private final DefaultTableModel leaksTableModel;

	private String loadedRaidData;
	private String selectedRaid;

	SwarmerPanel()
	{
		super(false);

		Font tableTitleFont = new Font(SwarmerFonts.REGULAR.toString(), Font.PLAIN, 18);
		Font tableFont = new Font(SwarmerFonts.VERDANA.toString(), Font.PLAIN, 12);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Box.createVerticalStrut(20));

		JPanel recentRaidsPanel = new JPanel();
		recentRaidsPanel.setLayout(new BoxLayout(recentRaidsPanel, BoxLayout.Y_AXIS));

		JLabel recentRaidsLabel = new JLabel("Recent Raids");
		recentRaidsLabel.setForeground(textColor);
		recentRaidsLabel.setFont(tableTitleFont);
		recentRaidsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		recentRaidsLabel.setAlignmentX(CENTER_ALIGNMENT);
		recentRaidsPanel.add(recentRaidsLabel);
		recentRaidsPanel.add(Box.createVerticalStrut(5));

		raidsListModel = new DefaultListModel<>();
		JList<String> raidsList = new JList<>(raidsListModel);
		raidsList.setBackground(tableColor1);
		raidsList.setForeground(textColor);
		raidsList.setFont(tableFont);
		raidsList.setSelectionBackground(tableColor2);
		raidsList.setSelectionForeground(textColor);
		raidsList.addListSelectionListener(e ->
		{
			if (!e.getValueIsAdjusting())
			{
				String selectedRaid = raidsList.getSelectedValue();
				if (selectedRaid != null)
				{
					this.selectedRaid = selectedRaid;
					loadRaidData(selectedRaid);
				}
			}
		});
		raidsList.setCellRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setBackground(value.equals(selectedRaid) ? tableColor2 : tableColor1);
				label.setPreferredSize(new Dimension(label.getPreferredSize().width, 25));
				return label;
			}
		});

		recentRaidsPanel.add(new JScrollPane(raidsList));
		add(recentRaidsPanel);
		add(Box.createVerticalStrut(20));

		JPanel leaksPanel = new JPanel();
		leaksPanel.setLayout(new BoxLayout(leaksPanel, BoxLayout.Y_AXIS));

		JLabel leaksLabel = new JLabel("Leaks");
		leaksLabel.setForeground(textColor);
		leaksLabel.setFont(tableTitleFont);
		leaksLabel.setHorizontalAlignment(SwingConstants.CENTER);
		leaksLabel.setAlignmentX(CENTER_ALIGNMENT);
		leaksPanel.add(leaksLabel);
		leaksPanel.add(Box.createVerticalStrut(5));

		leaksTableModel = new DefaultTableModel()
		{
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		JTable leaksTable = new JTable(leaksTableModel);
		leaksTable.setRowSelectionAllowed(false);
		leaksTable.setColumnSelectionAllowed(false);
		leaksTable.setCellSelectionEnabled(false);
		leaksTable.setBackground(tableColor1);
		leaksTable.setForeground(textColor);
		leaksTable.setGridColor(tableColor1);
		leaksTable.setFont(tableFont);

		DefaultTableCellRenderer leaksCellRenderer = new DefaultTableCellRenderer()
		{
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				c.setBackground(row % 2 == 0 ? tableColor1 : tableColor2);
				c.setForeground(textColor);
				return c;
			}
		};
		leaksCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		leaksTable.setDefaultRenderer(Object.class, leaksCellRenderer);

		JScrollPane leaksScrollPane = new JScrollPane(leaksTable);
		leaksScrollPane.setPreferredSize(new Dimension(100, 400));

		leaksPanel.add(leaksScrollPane);
		add(leaksPanel);
		add(Box.createVerticalStrut(20));

		updateRecentRaids();
	}

	public void loadRaidData(String raid)
	{
		if (raid == null || raid.equals(loadedRaidData))
		{
			return;
		}

		List<KephriRoomData> raidDataList = KephriRoomData.getRaidData(raid);
		Object[][] newData = new Object[raidDataList.size()][3];
		for (int i = 0; i < raidDataList.size(); i++)
		{
			KephriRoomData raidData = raidDataList.get(i);
			newData[i] = new Object[]{raidData.getDown(), raidData.getWave(), raidData.getLeaks()};
		}
		leaksTableModel.setDataVector(newData, LEAKS_COLUMN_NAMES);

		loadedRaidData = raid;
	}

	public void updateRecentRaids()
	{
		raidsListModel.clear();
		raidsListModel.addAll(getRecentRaids());
	}

	private List<String> getRecentRaids()
	{
		List<String> raids = KephriRoomData.getRaidList();
		raids.replaceAll(s -> new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(Long.parseLong(s) * 1000)));
		return raids;
	}

}
