package com.duckblade.osrs.toa.features.boss.kephri.swarmer;

import net.runelite.client.ui.PluginPanel;

import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

@Singleton
public class SwarmerPanel extends PluginPanel
{
    private final Color textColor = Color.WHITE;
    private final Color sidePanelColor = new Color(0x282828);
    private final Color backgroundColor = new Color(0x161616);
    private final Color tableColor1 = new Color(0x1F1F1F);
    private final Color tableColor2 = new Color(0x2D2D2D);

    Font tableTitleFont;
    Font tableFont;

    JTable statsTable;

    private JPanel mainPanel;
    private String loadedRaidData;
    private String selectedRaid;

    SwarmerPanel()
    {
        this.tableTitleFont = new Font(SwarmerFonts.REGULAR.toString(), Font.PLAIN, 18);
        this.tableFont = new Font(SwarmerFonts.VERDANA.toString(), Font.PLAIN, 12);

        renderSidePanel(null, null);

    }

    private void renderSidePanel(String[] rList, DefaultTableModel leaksTableModel)
    {
        if (mainPanel != null)
        {
            remove(mainPanel);
        }

        getParent().setLayout(new BorderLayout());
        getParent().add(this, BorderLayout.CENTER);
        setLayout(new BorderLayout());

        // Create main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));  // Vertical alignment
        mainPanel.setBackground(sidePanelColor);
        mainPanel.setForeground(textColor);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Recent Raids Panel
        JPanel recentRaidsPanel = new JPanel();
        recentRaidsPanel.setLayout(new BoxLayout(recentRaidsPanel, BoxLayout.Y_AXIS));
        recentRaidsPanel.setBackground(backgroundColor);

        JLabel recentRaidsLabel = new JLabel("Recent Raids");
        recentRaidsLabel.setBackground(backgroundColor);
        recentRaidsLabel.setForeground(textColor);
        recentRaidsLabel.setFont(tableTitleFont);
        recentRaidsLabel.setHorizontalAlignment(SwingConstants.LEFT);
        recentRaidsLabel.setPreferredSize(new Dimension(150, 20));

        recentRaidsPanel.add(recentRaidsLabel);

        if (rList == null)
        {
            rList = new String[0];
        }

        JList<String> raidsList = new JList<>(rList);
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
        mainPanel.add(recentRaidsPanel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Leaks Table Panel
        JPanel leaksPanel = new JPanel();
        leaksPanel.setLayout(new BoxLayout(leaksPanel, BoxLayout.Y_AXIS));
        leaksPanel.setBackground(backgroundColor);

        JLabel leaksLabel = new JLabel("Leaks");
        leaksLabel.setBackground(backgroundColor);
        leaksLabel.setForeground(textColor);
        leaksLabel.setFont(tableTitleFont);
        leaksLabel.setHorizontalAlignment(SwingConstants.LEFT);
        leaksLabel.setPreferredSize(new Dimension(150, 20));
        leaksPanel.add(leaksLabel);

        if (leaksTableModel == null)
        {
            String[] columnNames = { "Down", "Wave", "Leaks" };
            Object[][] data = new Object[0][3];
            leaksTableModel = new DefaultTableModel(data, columnNames)
            {
                @Override
                public boolean isCellEditable(int row, int column)
                {
                    return false;
                }
            };
        }

        JTable leaksTable = new JTable(leaksTableModel);
        leaksTable.setRowSelectionAllowed(false);
        leaksTable.setColumnSelectionAllowed(false);
        leaksTable.setCellSelectionEnabled(false);
        leaksTable.setBackground(backgroundColor);
        leaksTable.setForeground(textColor);
        leaksTable.setGridColor(backgroundColor);
        leaksTable.setFont(tableFont);

        // Alternate row colors
        leaksTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
        {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setBackground(row % 2 == 0 ? tableColor1 : tableColor2);
            c.setForeground(textColor);
            return c;
            }
        });

        // Create a custom cell renderer to center the text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Apply the renderer to each column
        for (int i = 0; i < leaksTable.getColumnCount(); i++)
        {
            leaksTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane leaksScrollPane = new JScrollPane(leaksTable);
        leaksScrollPane.setPreferredSize(new Dimension(100, 400));

        leaksPanel.add(leaksScrollPane);
        mainPanel.add(leaksPanel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
    }

    public void loadRaidData(String raid)
    {
        if (raid == null || raid.equals(loadedRaidData))
        {
            return;
        }
        List<KephriRoomData> raidDataList = KephriRoomData.getRaidData(raid);

        String[] columnNames = { "Down", "Wave", "Leaks" };
        Object[][] data = new Object[raidDataList.size()][3];
        DefaultTableModel leaksTableModel = new DefaultTableModel(data, columnNames)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };

        leaksTableModel.setRowCount(0); // Clear existing data
        for (KephriRoomData raidData : raidDataList)
        {
            leaksTableModel.addRow(new Object[]{raidData.getDown(), raidData.getWave(), raidData.getLeaks()});
        }

        loadedRaidData = raid;

        renderSidePanel(getRecentRaids(), leaksTableModel);
    }

    public void updateRecentRaids()
    {
        renderSidePanel(getRecentRaids(), new DefaultTableModel());
    }
    public void clearRecentRaids()
    {
        renderSidePanel(new String[0], new DefaultTableModel());
    }

    private String[] getRecentRaids()
    {
        List<String> raids = KephriRoomData.getRaidList();

        raids.replaceAll(s -> new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(Long.parseLong(s) * 1000)));

        return raids.toArray(new String[0]);
    }

}
