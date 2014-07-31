package ISectors.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.text.NumberFormatter;

import ISectors.engine.GameManager;

public class BattleWindow extends JFrame implements ActionListener {	
	private static final long serialVersionUID = 5149958171543488559L;

	/*
	 * This class will eventually contain other information, such as info panels, and data panels.
	 * This will also control switching between different menus...Maybe.
	 */
	
	public BattleWindow() {
		setSize(480, 480);
		GameManager.Initialize();
		initComponents();
	}
	
	public void initComponents() {
		BattleMap m = new BattleMap();
		m.setPreferredSize(new Dimension(750, 750));
		add(m, BorderLayout.CENTER);
		
		JButton b = new JButton("End Turn");
		b.addActionListener(m);
		add(b, BorderLayout.SOUTH);
		
		JMenuBar bar = new JMenuBar();
		setJMenuBar(bar);
		JMenu fileMenu = new JMenu("File");
		JMenu OptionMenu = new JMenu("Options");
		bar.add(fileMenu);
		bar.add(OptionMenu);
		
		JMenuItem itmNewGame = new JMenuItem("New Game");
		JMenuItem itmExit = new JMenuItem("Exit");
		JMenuItem itmDebug = new JMenuItem("Debug");
		fileMenu.add(itmNewGame);
		fileMenu.add(itmExit);
		OptionMenu.add(itmDebug);
		
		itmNewGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				generateNewGameMenu();
			}
		});
		
		itmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(DISPOSE_ON_CLOSE);
			}
		});
		
		itmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Setup debug dialog.
			}
		});
		
		pack();
	}
	
	
	private void generateDebugDialog() {
		JDialog dbgDialog = new JDialog(this, "Debug");
		dbgDialog.setSize(200, 80);
		dbgDialog.setModal(true);
		dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.PAGE_AXIS));
		
		JLabel lblMapSize = new JLabel("Map Size");
		dbgDialog.add(lblMapSize);
		JPanel pane = new JPanel();
		JLabel lblWidth = new JLabel("Width:");
		JTextField txtWidth = new JTextField(GameManager.Instance.getGrid().length + "", 6);
		pane.add(lblWidth);
		pane.add(txtWidth);
		dbgDialog.add(pane);
		JLabel lblHeight = new JLabel("Height:");
		JTextField txtHeight = new JTextField(GameManager.Instance.getGrid()[0].length + "", 6);
		pane = new JPanel();
		pane.add(lblHeight);
		pane.add(txtHeight);
		dbgDialog.add(pane);
		
		Integer[] choices = {2, 3, 4, 5, 6, 7, 8, 9, 10};
		pane = new JPanel();
		JComboBox<Integer> cboNPlayers = new JComboBox<Integer>(choices);
		cboNPlayers.setSelectedIndex(0);
		JLabel lblNPlayers = new JLabel("Num Players: ");
		pane.add(lblNPlayers);
		pane.add(cboNPlayers);
		dbgDialog.add(pane);
		
		JPanel playerPane = createPlayerPanel((int)cboNPlayers.getSelectedItem());
		dbgDialog.add(playerPane);
		
		JLabel lblDebug = new JLabel("Show Debug:");
		JCheckBox chkDebug = new JCheckBox("Show Debug:");
		pane = new JPanel();
		pane.add(lblDebug);
		pane.add(chkDebug);
		dbgDialog.add(pane);
	}
	
	private JPanel createPlayerPanel(int nPlayers) {
		// TO BE DONE
		return new JPanel();
	}
	
	private JDialog dialog;
	private JPanel displayPane;
	private JComboBox<String> modeSelect;
	private JComboBox<String> gameSelect;
	private JLabel[] labels;
	private JComboBox<Integer> playerSelect;
	private JFormattedTextField txtPlanets;
	private JFormattedTextField[] sizes;
	private JButton[] buttons;
	private JTable gameList;
	
	private void generateNewGameMenu() {
		dialog = new JDialog(this, "New Game");
		dialog.setSize(200, 80);
		dialog.setModal(true);
		dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.PAGE_AXIS));

		//Pre-generated, since these don't change.
		buttons = new JButton[4];
		buttons[0] = new JButton("Done");
		buttons[1] = new JButton("Cancel");
		buttons[0].addActionListener(this);
		buttons[1].addActionListener(this);
		
		JLabel modeLabel = new JLabel("Game Type:");
		String[] options = {"LOCAL", "ONLINE"};
		modeSelect = new JComboBox<String>(options);
		modeSelect.setSelectedIndex(0);
		modeSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Swap out Local and Online display.
				displayPane.removeAll();
				if(modeSelect.getSelectedItem() == "LOCAL") {
					createLocalData(displayPane);
				} else if(modeSelect.getSelectedItem() == "ONLINE") {
					createOnlineData(displayPane);
				}
				displayPane.validate();
				dialog.validate();
				dialog.pack();
			}
		});

		JPanel modePanel = new JPanel();
		modePanel.add(modeLabel);
		modePanel.add(modeSelect);
		dialog.add(modePanel);
				
		displayPane = new JPanel();
		displayPane.setLayout(new BoxLayout(displayPane, BoxLayout.PAGE_AXIS));
		dialog.add(displayPane);
		
		createLocalData(displayPane);
		
		dialog.pack();
		dialog.setVisible(true);
	}
	
	private void createLocalData(JPanel pane) {
		labels = new JLabel[5];
		labels[0] = new JLabel("Number of Players:");
		labels[1] = new JLabel("Number of Planets:");
		labels[2] = new JLabel("Size of Map:");
		labels[3] = new JLabel("Width:");
		labels[4] = new JLabel("Height:");
		
		Integer[] choices = {2, 3, 4, 5, 6, 7, 8, 9, 10};
		playerSelect = new JComboBox<Integer>(choices);
		playerSelect.setSelectedIndex(0);
		
		NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(0);
		formatter.setMaximum(Integer.MAX_VALUE);
		formatter.setCommitsOnValidEdit(true);
		sizes = new JFormattedTextField[2];
		sizes[0] = new JFormattedTextField(formatter);//GameManager.DEFAULT_ROWS + "", 6);
		sizes[0].setText(GameManager.DEFAULT_ROWS + "");
		sizes[0].setColumns(6);
		sizes[1] = new JFormattedTextField(formatter);//GameManager.DEFAULT_COLS + "", 6);
		sizes[1].setText(GameManager.DEFAULT_COLS + "");
		sizes[1].setColumns(6);
		
		txtPlanets = new JFormattedTextField(formatter);//"0", 3);
		txtPlanets.setText("0");
		txtPlanets.setColumns(3);
	
		JLabel modeLabel = new JLabel("Game Mode:");
		String [] options = {"Deathmatch", "Survival", "Domination"};
		gameSelect = new JComboBox<String>(options);
		JPanel modePanel = new JPanel();
		modePanel.add(modeLabel);
		modePanel.add(gameSelect);
		pane.add(modePanel);
		
		JPanel p = new JPanel();
		p.add(labels[0]);
		p.add(playerSelect);
		pane.add(p);
		p = new JPanel();
		p.add(labels[1]);
		p.add(txtPlanets);
		pane.add(p);
		labels[2].setAlignmentX(Component.RIGHT_ALIGNMENT);
		pane.add(labels[2]);
		JPanel wPanel = new JPanel();
		wPanel.add(labels[3]);
		wPanel.add(sizes[0]);
		pane.add(wPanel);
		JPanel hPanel = new JPanel();
		hPanel.add(labels[4]);
		hPanel.add(sizes[1]);
		pane.add(hPanel);
		JPanel btnPanel = new JPanel();
		btnPanel.add(buttons[0]);
		btnPanel.add(buttons[1]);
		pane.add(btnPanel);
	}

	private void createOnlineData(JPanel pane) {
		String[] columnNames = {"Lobby Name", "Current #", "# of Players", "Password?"};
		Object[][] data = {{"Game 1", new Integer(1), new Integer(2), "Yes"}, 
				{"Game 2", new Integer(3), new Integer(3), "No"}
		};
		// Need to create a table model to store data
		gameList = new JTable(data, columnNames);
		JScrollPane scrollPane = new JScrollPane(gameList);
		gameList.setFillsViewportHeight(true);
		
		buttons[2] = new JButton("Refresh");
		buttons[2].setAlignmentX(LEFT_ALIGNMENT);
		buttons[3] = new JButton("Create");
		buttons[3].setAlignmentX(RIGHT_ALIGNMENT);
		buttons[2].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//Request data refresh from server.
				//Update table data
				
			}
		});
		buttons[3].addActionListener(this);
		
		JPanel tablePane = new JPanel();
		tablePane.add(buttons[2]);
		tablePane.add(buttons[3]);
		pane.add(tablePane);
		
		pane.add(scrollPane);

		JPanel btnPanel = new JPanel();
		btnPanel.add(buttons[0]);
		btnPanel.add(buttons[1]);
		pane.add(btnPanel);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == buttons[0]) {
			if(modeSelect.getSelectedItem() == "LOCAL") {
				int nRows = Integer.parseInt(sizes[0].getText());
				int nCols = Integer.parseInt(sizes[1].getText());
				int nPlayers = (Integer)(playerSelect.getSelectedItem());
				int nPlanets = Integer.parseInt(txtPlanets.getText());
				GameManager.NewGame(nRows, nCols, GameManager.GameType.LOCAL, nPlayers, nPlanets);
				BattleMap.Instance.loadBattleMap(nRows, nCols);
			} else if(modeSelect.getSelectedItem() == "ONLINE") {
				System.out.println("Connect to online game?");
			}
			dialog.setVisible(false);
			dialog.dispose();
		} else if(e.getSource() == buttons[1]) {
			dialog.setVisible(false);
			dialog.dispose();
		} else if(e.getSource() == buttons[3]) {
			System.out.println("Not implemented yet");
		} else {
			System.out.println("Clicked on " + e.getSource());
		}
	}
}