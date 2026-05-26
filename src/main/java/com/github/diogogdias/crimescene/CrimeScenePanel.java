package com.github.diogogdias.crimescene;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

class CrimeScenePanel extends PluginPanel
{
	private static final Color GOLD = new Color(255, 215, 0);
	private static final Color SILVER = new Color(192, 192, 192);
	private static final Color BRONZE = new Color(205, 127, 50);
	private static final String PLAYERS_CARD = "players";
	private static final String NPCS_CARD = "npcs";

	private final JPanel playersList = new JPanel();
	private final JPanel npcsList = new JPanel();
	private final JLabel playersTab = new JLabel("Players", SwingConstants.CENTER);
	private final JLabel npcsTab = new JLabel("NPCs", SwingConstants.CENTER);
	private final CardLayout cardLayout = new CardLayout();
	private final JPanel cards = new JPanel(cardLayout);
	private Runnable clearCallback;

	CrimeScenePanel()
	{
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setLayout(new BorderLayout(0, 10));
		setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

		add(buildHeader(), BorderLayout.NORTH);
		add(buildBody(), BorderLayout.CENTER);
		add(buildClearButton(), BorderLayout.SOUTH);

		selectTab(true);
	}

	private JPanel buildHeader()
	{
		JPanel header = new JPanel(new BorderLayout(8, 0));
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel icon = new JLabel(new ImageIcon(ImageUtil.loadImageResource(CrimeScenePanel.class, "panel_icon.png")));

		JLabel title = new JLabel("Crime Scene");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(ColorScheme.BRAND_ORANGE);

		header.add(icon, BorderLayout.WEST);
		header.add(title, BorderLayout.CENTER);
		return header;
	}

	private JPanel buildBody()
	{
		playersList.setLayout(new BoxLayout(playersList, BoxLayout.Y_AXIS));
		playersList.setBackground(ColorScheme.DARK_GRAY_COLOR);
		npcsList.setLayout(new BoxLayout(npcsList, BoxLayout.Y_AXIS));
		npcsList.setBackground(ColorScheme.DARK_GRAY_COLOR);

		setupTab(playersTab, true);
		setupTab(npcsTab, false);

		JPanel tabBar = new JPanel(new GridLayout(1, 2));
		tabBar.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tabBar.add(playersTab);
		tabBar.add(npcsTab);

		cards.setBackground(ColorScheme.DARK_GRAY_COLOR);
		cards.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
		cards.add(north(playersList), PLAYERS_CARD);
		cards.add(north(npcsList), NPCS_CARD);

		JPanel body = new JPanel(new BorderLayout());
		body.setBackground(ColorScheme.DARK_GRAY_COLOR);
		body.add(tabBar, BorderLayout.NORTH);
		body.add(cards, BorderLayout.CENTER);
		return body;
	}

	private void setupTab(JLabel tab, boolean players)
	{
		tab.setOpaque(true);
		tab.setFont(FontManager.getRunescapeBoldFont());
		tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
		tab.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				selectTab(players);
			}
		});
	}

	private void selectTab(boolean players)
	{
		cardLayout.show(cards, players ? PLAYERS_CARD : NPCS_CARD);
		styleTab(playersTab, players);
		styleTab(npcsTab, !players);
	}

	private static void styleTab(JLabel tab, boolean selected)
	{
		tab.setBackground(selected ? ColorScheme.DARK_GRAY_COLOR : ColorScheme.DARKER_GRAY_COLOR);
		tab.setForeground(selected ? Color.WHITE : ColorScheme.LIGHT_GRAY_COLOR);
		tab.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 2, 0,
				selected ? ColorScheme.BRAND_ORANGE : ColorScheme.MEDIUM_GRAY_COLOR),
			BorderFactory.createEmptyBorder(7, 0, 7, 0)));
	}

	private static JPanel north(JPanel list)
	{
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.add(list, BorderLayout.NORTH);
		return wrapper;
	}

	private JButton buildClearButton()
	{
		JButton button = new JButton("Clear all markers");
		button.setFocusPainted(false);
		button.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		button.addActionListener(e ->
		{
			if (clearCallback != null)
			{
				clearCallback.run();
			}
		});
		return button;
	}

	void setClearCallback(Runnable clearCallback)
	{
		this.clearCallback = clearCallback;
	}

	void update(List<Map.Entry<String, Integer>> players, List<Map.Entry<String, Integer>> npcs)
	{
		SwingUtilities.invokeLater(() ->
		{
			populate(playersList, players);
			populate(npcsList, npcs);
		});
	}

	private static void populate(JPanel listPanel, List<Map.Entry<String, Integer>> leaderboard)
	{
		listPanel.removeAll();

		if (leaderboard.isEmpty())
		{
			JLabel empty = new JLabel("No deaths recorded yet", SwingConstants.CENTER);
			empty.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			empty.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
			empty.setAlignmentX(Component.CENTER_ALIGNMENT);
			listPanel.add(empty);
		}
		else
		{
			int rank = 1;
			for (Map.Entry<String, Integer> entry : leaderboard)
			{
				if (rank > 1)
				{
					listPanel.add(Box.createRigidArea(new Dimension(0, 4)));
				}
				listPanel.add(buildRow(rank++, entry.getKey(), entry.getValue()));
			}
		}

		listPanel.revalidate();
		listPanel.repaint();
	}

	private static JPanel buildRow(int rank, String name, int deaths)
	{
		JPanel row = new JPanel(new BorderLayout(8, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
		row.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel rankLabel = new JLabel(String.valueOf(rank));
		rankLabel.setFont(FontManager.getRunescapeBoldFont());
		rankLabel.setForeground(rankColor(rank));
		rankLabel.setPreferredSize(new Dimension(18, 0));

		JLabel nameLabel = new JLabel(name);
		nameLabel.setForeground(Color.WHITE);

		JLabel countLabel = new JLabel(String.valueOf(deaths));
		countLabel.setFont(FontManager.getRunescapeBoldFont());
		countLabel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);

		JPanel left = new JPanel(new BorderLayout(8, 0));
		left.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		left.add(rankLabel, BorderLayout.WEST);
		left.add(nameLabel, BorderLayout.CENTER);

		row.add(left, BorderLayout.CENTER);
		row.add(countLabel, BorderLayout.EAST);
		return row;
	}

	private static Color rankColor(int rank)
	{
		switch (rank)
		{
			case 1:
				return GOLD;
			case 2:
				return SILVER;
			case 3:
				return BRONZE;
			default:
				return ColorScheme.LIGHT_GRAY_COLOR;
		}
	}
}
