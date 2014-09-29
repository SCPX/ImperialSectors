package ISectors.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import ISectors.engine.GameManager.GameType;
import ISectors.engine.*;
import ISectors.planets.Planet;
import ISectors.ships.*;
import ISectors.engine.Orders;

public class BattleMap extends JPanel implements MouseListener, ActionListener {
	public static BattleMap Instance;
	
	private static final long serialVersionUID = -8876256816008699262L;
	private Color backgroundColor = Color.white;
	private Location[][] _grid = null;
	private int _numRows = 25;
	private int _numCols = 25;
	private PopupMenuHandler popupHandler;
	
	public static boolean displayMap = true;
//	static Orders selectedOrder = Orders.STANDBY;
	
	public BattleMap() {
		addMouseListener(this);
		popupHandler = new PopupMenuHandler(this);
		setLayout(new GridLayout(_numCols, _numRows));
		if(Instance == null)
			Instance = this;
	}
	
	public BattleMap(int nRows, int nCols) {
		_numRows = nRows;
		_numCols = nCols;
		setLayout(new GridLayout(_numCols, _numRows));
		addMouseListener(this);
		popupHandler = new PopupMenuHandler(this);
		
		if(Instance == null) 
			Instance = this;
	}
	
	public void loadBattleMap(int nRows, int nCols) {
		this.removeAll();
		_numRows = nRows;
		_numCols = nCols;
		this.setLayout(new GridLayout(_numCols, _numRows));
		_grid = GameManager.Instance.getGrid();
		
		for(int y = 0; y < _numCols; y++) {
			for(int x = 0; x < _numRows; x++) {
				_grid[x][y].addMouseListener(this);
				this.add(_grid[x][y]);
			}
		}
		this.validate();
	}
	
	public void startGame() {
		displayMap = false;
		repaint();
		JOptionPane.showMessageDialog(this, "Click Okay when ready to begin.");
		displayMap = true;
		repaint();
	}
	
	public void paint(Graphics g) {
		super.paintComponent(g);
		g.setColor(backgroundColor);
		Rectangle bounds = this.getBounds();
		g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
//	    int cellWidth = bounds.width / _numRows;
//	    int cellHeight = bounds.height / _numCols;
		
		if(_grid != null) {
			for(int y = 0; y < _numCols; y++) {
				for(int x = 0; x < _numRows; x++) {
					_grid[x][y].paint(g);
				}
			}//*/
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(_grid == null) return;
		if(e.getComponent() instanceof Location) {
			Location l = (Location) e.getComponent();
			if(e.getButton() == MouseEvent.BUTTON1) {
				if(e.isShiftDown()) {
					if(GameManager.getSelectedObj() instanceof Location) {
						Location loc = (Location)(GameManager.getSelectedObj());
						loc.assignOrder(Orders.MOVE, l);
						GameManager.selectObj(null);
					} else if(GameManager.getSelectedObj() instanceof Ship) {
						((Ship)(GameManager.getSelectedObj())).assignOrder(Orders.MOVE, l);
						GameManager.selectObj(null);
					}
				} else {
					if(GameManager.getSelectedObj() != null) {
						GameManager.selectObj(null);
					}
					// Below should be adjusted so we can select enemies, but not give them orders.
					if(!l.isEmptyOrInvisible()) { //  && l.Allegiance() == TurnManager.currentPlayer
						GameManager.selectObj(l);
					}
				}
			}
			else if(e.getButton() == MouseEvent.BUTTON3) {
				// Send order to ships at selectedLoc to move to l
				if(e.isShiftDown() && GameManager.getSelectedObj() != null) {
					if(GameManager.getSelectedObj() instanceof Location) {
						((Location)(GameManager.getSelectedObj())).assignOrder(Orders.MOVE, l);
						GameManager.selectObj(null);
					} else if(GameManager.getSelectedObj() instanceof Ship) {
						((Ship)(GameManager.getSelectedObj())).assignOrder(Orders.MOVE, l);
						GameManager.selectObj(null);
					}
				} else {
					popupHandler.generatePopUp(l).show(e.getComponent(), e.getX(), e.getY());
				}
			}
		} else {
			System.out.println("Clicked on " + e.getComponent().toString());
		}
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(GameManager.Instance.isGameOver())
			return;
		if(GameManager.Instance.getGameType() == GameType.LOCAL) {
			TurnManager.nextTurn();
			if(!GameManager.Instance.isGameOver()) {
				displayMap = false;
				repaint();
				JOptionPane.showMessageDialog(this, TurnManager.getPlayer(TurnManager.currentPlayer).Name + "'s turn.");
				displayMap = true;
			}
		}
		repaint();
	}
		
	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
}

class PopupMenuHandler implements ActionListener {
	private BattleMap parent;
	private JPopupMenu popup;
	private Hashtable<JMenuItem, Orders> orderItems;
	private Hashtable<JMenuItem, Ship> shipItems;
	private Hashtable<JMenuItem, Ship> upgradeItems;
	private Hashtable<JMenuItem, Object> locSpecificItems;
	private JMenuItem planetItem;
	private JMenuItem locItem;
	private Location associatedLoc;
	
	public PopupMenuHandler(BattleMap par){
		orderItems = new Hashtable<JMenuItem, Orders>();
		shipItems = new Hashtable<JMenuItem, Ship>();
		upgradeItems = new Hashtable<JMenuItem, Ship>();
		locSpecificItems = new Hashtable<JMenuItem, Object>();
		parent = par;
	}
	
	public JPopupMenu generatePopUp(Location l) {
		orderItems.clear();
		shipItems.clear();
		upgradeItems.clear();
		locSpecificItems.clear();
		planetItem = null;
		JMenuItem menuItem;
		popup = new JPopupMenu();
		Ship[] s = l.getOccupants();

		/*** AVAILABLE ORDERS FOR SELECTED SHIPS ***/
		if(GameManager.getSelectedObj() instanceof Location && !((Location)(GameManager.getSelectedObj())).isEmptyOrInvisible()) {
			Ship[] ships = ((Location)(GameManager.getSelectedObj())).getOccupants();
			boolean upgradeMenuCreated = false;
			Orders[] orders;
			for(int i = 0; i < ships.length; i++) {
				if(ships[i].getLoyalty() != TurnManager.currentPlayer)
					continue;
				orders = ships[i].getOrders();
				for(int j = 0; j < orders.length; j++) {
					if(!orderItems.containsValue(orders[j]) && (orders[j] != Orders.UPGRADE || !upgradeMenuCreated)) {
						if(orders[j] == Orders.UPGRADE ) {
							// Add a submenu with a list of ships to upgrade
							JMenu upgradeMenu = new JMenu(Orders.OrderToString(orders[j]));
							if(!l.isEmptyOrInvisible())
							{
								for(int si = 0; si < s.length; si++) {
									if(s[si].canUpgrade() && s[si].getLoyalty() == TurnManager.currentPlayer) {
										menuItem = new JMenuItem(s[si].getName());
										menuItem.addActionListener(this);
										upgradeMenu.add(menuItem);
										upgradeItems.put(menuItem, s[si]);
									}
								}
							}
							popup.add(upgradeMenu);
							upgradeMenuCreated = true;
						} else {
							menuItem = new JMenuItem(Orders.OrderToString(orders[j]));
							menuItem.addActionListener(this);
							popup.add(menuItem);
							orderItems.put(menuItem, orders[j]);
						}
					}
				}
			}
			popup.addSeparator();
		} /*** AVAILABLE ORDERS FOR SELECTED LOCATION ***/
		else if(GameManager.getSelectedObj() instanceof Ship) {
			if(((Ship)GameManager.getSelectedObj()).getLoyalty() == TurnManager.currentPlayer) {
				// Should we only show upgrade order if only the capital ship is selected, and not if an entire location is selected?
				Orders[] orders = ((Ship)(GameManager.getSelectedObj())).getOrders();
				for(int i = 0; i < orders.length; i++) {
					if(orders[i] == Orders.UPGRADE) {
						JMenu upgradeMenu = new JMenu(Orders.OrderToString(orders[i]));
						if(!l.isEmptyOrInvisible()) {
							for(int si = 0; si < s.length; si++) { 
								if(s[si].canUpgrade() && s[si].getLoyalty() == TurnManager.currentPlayer) { // Change this to checking if ship is upgradeable. 
									menuItem = new JMenuItem(s[si].getName());
									menuItem.addActionListener(this);
									upgradeMenu.add(menuItem);
									upgradeItems.put(menuItem, s[si]);
								}
							}
						}
						popup.add(upgradeMenu);
					} else {
						menuItem = new JMenuItem(Orders.OrderToString(orders[i]));
						menuItem.addActionListener(this);
						popup.add(menuItem);
						orderItems.put(menuItem, orders[i]);
					}
				}
				popup.addSeparator();
			}
		}
		else if(GameManager.getSelectedObj() instanceof Planet) {
			if(((Planet)GameManager.getSelectedObj()).canBeOrdered() && (((Planet)GameManager.getSelectedObj()).getAlliance() == TurnManager.currentPlayer)) {
				Orders[] orders = ((Planet)(GameManager.getSelectedObj())).getOrders();
				for (int i = 0; i < orders.length; i++) {
					if(orders[i] == Orders.UPGRADE) {
						JMenu upgradeMenu = new JMenu(Orders.OrderToString(orders[i]));
						if(!l.isEmptyOrInvisible()) {
							for(int si = 0; si < s.length; si++) {
								if(s[si].canUpgrade() && s[si].getLoyalty() == TurnManager.currentPlayer) {
									menuItem = new JMenuItem(s[si].getName());
									menuItem.addActionListener(this);
									upgradeMenu.add(menuItem);
									upgradeItems.put(menuItem, s[si]);
								}
							}
						}
						popup.add(upgradeMenu);
					} else {
						menuItem = new JMenuItem(Orders.OrderToString(orders[i]));
						menuItem.addActionListener(this);
						popup.add(menuItem);
						orderItems.put(menuItem, orders[i]);
					}
				}
			}
			popup.addSeparator();
		}
		
		//Ships at location
		if(s != null && s.length > 0) {
			for(int i = 0; i < s.length; i++) {
				menuItem = new JMenuItem(s[i].getName());
				// Maybe show player colors instead of just red?
				if(s[i].getLoyalty() != TurnManager.currentPlayer) {
					menuItem.setForeground(Color.red);
				}
				menuItem.addActionListener(this);
				popup.add(menuItem);
				shipItems.put(menuItem, s[i]);
			}
			popup.addSeparator();
		}
		
		//Planets at location
		if(l.getPlanet() != null) {
			if(l.getPlanet().getAlliance() == TurnManager.currentPlayer) {
				JMenu planetMenu = new JMenu(l.getPlanet().getName());
				planetMenu.setForeground(TurnManager.getPlayerColor(l.getPlanet().getAlliance()));
				Orders[] orders = l.getPlanet().getOrders();
				for(int i = 0; i < orders.length; i++) {
					if(orders[i] == Orders.UPGRADE) {
						JMenu upgradeMenu = new JMenu(Orders.OrderToString(orders[i]));
						if(!l.isEmptyOrInvisible()) {
							for(int si = 0; si < s.length; si++) {
								if(s[si].canUpgrade() && s[si].getLoyalty() == TurnManager.currentPlayer) {
									menuItem = new JMenuItem(s[si].getName());
									menuItem.addActionListener(this);
									upgradeMenu.add(menuItem);
									locSpecificItems.put(menuItem, s[si]);
								}
							}
						}
						planetMenu.add(upgradeMenu);
					} else {
						menuItem = new JMenuItem(Orders.OrderToString(orders[i]));
						menuItem.addActionListener(this);
						planetMenu.add(menuItem);
						locSpecificItems.put(menuItem, orders[i]);
					}
				}
				popup.add(planetMenu);
			} else if(l.getPlanet().getAlliance() != Planet.UNOWNED) {
				menuItem = new JMenuItem(l.getPlanet().getName());
				menuItem.setForeground(TurnManager.getPlayerColor(l.getPlanet().getAlliance()));
				menuItem.addActionListener(this);
				popup.add(menuItem);
				planetItem = menuItem;
				popup.addSeparator();
			} else {
				menuItem = new JMenuItem(l.getPlanet().getName());
				menuItem.addActionListener(this);
				popup.add(menuItem);
				planetItem = menuItem;
				popup.addSeparator();
			}
		}
		
		//Location Information
		menuItem = new JMenuItem("Location " + l.toString());
		menuItem.addActionListener(this);
		popup.add(menuItem);
		locItem = menuItem;
		associatedLoc = l;
		return popup;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(orderItems.containsKey(e.getSource())) {
			Orders order = orderItems.get(e.getSource());
			if(GameManager.getSelectedObj() instanceof Location) {
				((Location)(GameManager.getSelectedObj())).assignOrder(order, associatedLoc);
			} else if(GameManager.getSelectedObj() instanceof Ship) {
				((Ship)(GameManager.getSelectedObj())).assignOrder(order, associatedLoc);
			} else if(GameManager.getSelectedObj() instanceof Planet) {
				((Planet)(GameManager.getSelectedObj())).assignOrder(order, null);
			}
			GameManager.selectObj(null);
		} else if(upgradeItems.containsKey(e.getSource())) {
			if(GameManager.getSelectedObj() instanceof Location) {
				// retrieve CapitalShip from location
				Ship[] ships = ((Location)(GameManager.getSelectedObj())).getOccupants();
				for(int i = 0; i < ships.length; i++) {
					if(ships[i].getClass() == CapitalShip.class) {
						CapitalShip s = (CapitalShip) ships[i];
						s.assignOrder(Orders.UPGRADE, upgradeItems.get(e.getSource()));
						GameManager.selectObj(null);
						break;
					}
				}
			} else if(GameManager.getSelectedObj() instanceof Ship) {
				if(GameManager.getSelectedObj().getClass() == CapitalShip.class) {
					CapitalShip s = (CapitalShip) GameManager.getSelectedObj();
					s.assignOrder(Orders.UPGRADE, upgradeItems.get(e.getSource()));
					GameManager.selectObj(null);
				}
			} else if(GameManager.getSelectedObj() instanceof Planet) {
				((Planet)GameManager.getSelectedObj()).assignOrder(Orders.UPGRADE, upgradeItems.get(e.getSource()));
				GameManager.selectObj(null);
			}
		} else if(shipItems.containsKey(e.getSource())) {
			if(GameManager.getSelectedObj() != null) {
				GameManager.selectObj(null);
			}
			Ship s = shipItems.get(e.getSource());
//			if(s.getLoyalty() == TurnManager.currentPlayer) {
			GameManager.selectObj(s);
//			}
		} else if(locItem == e.getSource()) {
			if(GameManager.getSelectedObj() != null) {
				GameManager.selectObj(null);
			}
			//Below should be modified to allow us to select the location for the non-friendly forces, but not give orders.
			if(!associatedLoc.isInvisible()) {
				GameManager.selectObj(associatedLoc);
			}
		} else if(planetItem == e.getSource()) {
//			if(associatedLoc.getPlanet().getAlliance() == TurnManager.currentPlayer)
			GameManager.selectObj(associatedLoc.getPlanet());
		} else if(locSpecificItems.containsKey(e.getSource())) {
			if(locSpecificItems.get(e.getSource()) instanceof Orders) {
				// Regular order
				associatedLoc.getPlanet().assignOrder((Orders)(locSpecificItems.get(e.getSource())), null);
			} else if(locSpecificItems.get(e.getSource()) instanceof Ship) {
				// Upgrade Order
				associatedLoc.getPlanet().assignOrder(Orders.UPGRADE, (Ship)(locSpecificItems.get(e.getSource())));
			}
			GameManager.selectObj(null);
		}
		parent.repaint();
	}
}