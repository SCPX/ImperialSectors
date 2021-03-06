package ISectors.engine;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import ISectors.ships.*;
import ISectors.planets.*;
import ISectors.engine.Orders;

public class Location extends Component implements Selectable {
	private boolean debug = false;
	
	private static final long serialVersionUID = -7388200672741015050L;
	public static final int CONTESTED = -1;
	private int _row; // Y coordinate position on grid of this Sector.
	private int _col; // X coordinate position on grid of this Sector.
//	private Fleet _occupants = null;
	private ArrayList<Ship> _occupants;
	private Planet _planet;
	private int border = 1;
	private int borderThickness = 3;
	//public boolean selected = false;
	private boolean conflicted = false;

	public Location(int x, int y) {
		_col = x;
		_row = y;
		_occupants = new ArrayList<Ship>();
	}
	
	public Location(int x, int y, Ship occupant) {
		_col = x;
		_row = y;
		_occupants = new ArrayList<Ship>();
		_occupants.add(occupant);
		if(!occupant.isCreated())
		{
			occupant.Create(this);
		}
//		_occupants = new Fleet(this);
//		_occupants.addShip(occupant);
	}
	
	public Location(int x, int y, Ship[] occupants) {
		_col= x;
		_row = y;
		_occupants = new ArrayList<Ship>();
		for(int i = 0; i < occupants.length; i++) {
			_occupants.add(occupants[i]);
			if(!occupants[i].isCreated()) {
				occupants[i].Create(this);
			}
		}
	}
	
	public void paint(Graphics g) {
		Rectangle bounds = this.getBounds();
		if(TurnManager.isLocationVisible(this) && ISectors.view.BattleMap.displayMap) { // Location is not in fog of war.
			if(GameManager.getSelectedObj() != null && GameManager.getSelectedObj().getSelectedLoc() != this && TurnManager.isReachable(this)) { // This location is within range of the selected ship without being the selected location itself.
				g.setColor(Color.blue);
				g.fillRect(bounds.x + border, bounds.y + border, bounds.width - (2 * border), bounds.height - (2 * border));
			} else { // Location is not within range of any selected ship, if there are any selected ships.
				g.setColor(Color.black);
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			}
			//Draw Conflict border
			if(conflicted) {
				g.setColor(Color.orange);
				for(int i = 0; i < 2; i++) {
					g.drawRect(bounds.x + i, bounds.y + i, bounds.width - (2 * i), bounds.height - (2 * i));
				}
			}
			//draw fleet/planet
			if(_planet != null) {
				BufferedImage _icon;
				_icon = _planet.getIcon();
				if(_icon != null) {
					g.drawImage(_icon, bounds.x + border, bounds.y + border, bounds.width - (2 * border), bounds.height - (2 * border), null);
				}
				if(_planet.getAlliance() != Planet.UNOWNED) {
					// Draw red border
					g.setColor(TurnManager.getPlayerColor(_planet.getAlliance()));
					for(int i = 0; i < borderThickness; i++) {
						g.drawRect(bounds.x + i, bounds.y + i, bounds.width - (2 * i), bounds.height - (2 * i));
					}
				}
			} else if(!_occupants.isEmpty()) {
				BufferedImage _icon;
				if(_occupants.size() > 1) {
					_icon = Ship.getSquadImage();
				} else {
					_icon = _occupants.get(0).getShipImage();
				}
				if(_icon != null) {
					g.drawImage(_icon, bounds.x + border, bounds.y + border, bounds.width - (2 * border), bounds.height - (2 * border), null);
				}
			}
			
			if(GameManager.getSelectedObj() != null && GameManager.getSelectedObj().getSelectedLoc() == this) { // This is the selected location. Draw selection border
				g.setColor(Color.yellow);
				for(int i = 0; i < borderThickness; i++) {
					g.drawRect(bounds.x + i, bounds.y + i, bounds.width - (2 * i), bounds.height - (2 * i));
				}
			}
		} else { // Location is a part of the fog of war.
			if(GameManager.getSelectedObj() != null && GameManager.getSelectedObj().getSelectedLoc() != this && TurnManager.isReachable(this)) { // Location is within travel range of selected ship.
				if((GameManager.getSelectedObj() instanceof Ship && ((Ship)(GameManager.getSelectedObj())).getLoyalty() != TurnManager.currentPlayer) || 
						(GameManager.getSelectedObj() instanceof Planet && ((Planet)(GameManager.getSelectedObj())).getAlliance() != TurnManager.currentPlayer)) {
					g.setColor(Color.DARK_GRAY);
					g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
				} else {
					g.setColor(Color.blue);
					g.fillRect(bounds.x + border, bounds.y + border, bounds.width - (2 * border), bounds.height - (2 * border));
				}
			} else { 
				g.setColor(Color.DARK_GRAY);
				g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
//				g.fillRect(bounds.x + border, bounds.y + border, bounds.width - (2 * border), bounds.height - (2 * border));
			}
		}
	}
	
	public String toString() {
		return "{" + _col + ", " + _row + "}";
	}
	
	public void EmptySector() {
		_occupants.clear();
	}
	
	/** Allegiance
	 * Return the player number of the ships in this location. If there are more than a single player's ship, 
	 * then it returns the current player's number iff the current player's ships are involved.
	 */
	public int Allegiance() {
		if(_occupants.isEmpty()) return 0;
		
		boolean contested = false;
		int loyalty = _occupants.get(0).getLoyalty();
		for(int i = 1; i < _occupants.size(); i++) {
			if(!contested && _occupants.get(i).getLoyalty() != loyalty) {
				contested = true;
				if(loyalty != TurnManager.currentPlayer && _occupants.get(i).getLoyalty() != TurnManager.currentPlayer) {
					loyalty = -1;
				} else if(_occupants.get(i).getLoyalty() == TurnManager.currentPlayer) {
					loyalty = _occupants.get(i).getLoyalty();
				}
			} else if(contested && _occupants.get(i).getLoyalty() == TurnManager.currentPlayer) {
				loyalty = _occupants.get(i).getLoyalty();
			}
		}
		
		return loyalty;
	}
	
	public boolean isEmptyOrInvisible() {
		return (_occupants.isEmpty() && _planet == null) || !TurnManager.isLocationVisible(this);
	}
	
	public boolean isInvisible() {
		return !TurnManager.isLocationVisible(this);
	}
	
	public boolean IsEmpty() {
		return _occupants.isEmpty();
	}
	
	public void EnterSector(Ship s) {
		_occupants.add(s);
		if(!s.isCreated()) {
			s.Create(this);
		}
		TurnManager.addLocation(this);
		
		// Check if an enemy of s is in this sector.
		// if so, mark as a conflicted Location
	}
	
/*	public boolean EnterSector(Fleet f) {
		if(_occupants == null) {
			_occupants = f;
		} else {
			// merge fleets
			_occupants.mergeFleet(f);
		}
		return true;
	}//*/
	
	public boolean LeaveSector(Ship s) {
		if(_occupants == null) {
			return false;
		}
//		_occupants.removeShip(s);
		_occupants.remove(s);
		if(_occupants.isEmpty()) {
			TurnManager.removeLocation(this);
			conflicted = false;
		}

		return true;
	}
	
	public Ship[] getOccupants() {
		Ship[] s = new Ship[_occupants.size()];
		for(int i = 0; i < _occupants.size(); i++) {
			s[i] = _occupants.get(i);
		}
		return s;
	}
	
	public void assignOrder(Orders order, Location target) {
		if(_occupants.isEmpty()) return;
				
		for(int i = 0; i < _occupants.size(); i++) {
			Ship s = _occupants.get(i);
			if(s.getLoyalty() == TurnManager.currentPlayer) {
				if(target == null) 
					s.assignOrder(order);
				else
					s.assignOrder(order, target);
			}
		}
	}
	
	public void Resolve() {
		//if(!conflicted) return;
		if(_occupants.isEmpty()) {
			if(debug)
				System.out.println("Nothing to resolve");
			return;
		}
		
		// Resolve combat
		int nFleets = _occupants.size();
		Ship[][] fleets = new Ship[TurnManager.numPlayers][nFleets]; // Stores the different fleets
		int[] nShips = new int[TurnManager.numPlayers]; // stores how many ships in each fleet.
		int[] fleetCombat = new int[TurnManager.numPlayers];
		int[] fleetAlignments = new int[TurnManager.numPlayers];
		
//		for(int i = 0; i < nShips.length; i++) 
//			nShips[i] = 0;
	
		nFleets = 0;

		for(int i = 0; i < _occupants.size(); i++) {
			// For each ship:
			// Determine if loyalty is already accounted for.
			boolean accounted = false;
			Ship s = _occupants.get(i);
			for(int j = 0; j < nFleets; j++) {
				if(nShips[j] > 0 && fleets[j][0].getLoyalty() == s.getLoyalty()) {
					fleets[j][nShips[j]++] = s;
					accounted = true;
					break;
				}
			}
			
			if(!accounted) {
				if(debug) System.out.println("New ship added to fleet " + nFleets);
				fleetCombat[nFleets] = 0;
				fleets[nFleets][0] = s;
				fleetAlignments[nFleets] = s.getLoyalty();
				nShips[nFleets++]++;
			}
		}
		
		if(nFleets <= 1) {
			if(debug) System.out.println("Only one fleet");
			if(!_occupants.isEmpty() && _planet != null) {
				_planet.Invade(_occupants.get(0).getLoyalty());
			}
			conflicted = false;
			return;
		}
		
		if(debug) System.out.println("Resolving Combat");
		// Fleets have been determined.
		// Calculate combat for all fleets.
		if(debug) System.out.print("Combat powers are : ");
		for(int i = 0; i < nFleets; i++) {
			for(int j = 0; j < nShips[i]; j++) {
				fleetCombat[i] += fleets[i][j].getFirepower();
			}
			if(debug) System.out.print("(" + i + ") " + fleetCombat[i] + "   ");
		}
		if(debug) System.out.println("");
		
		// compare firepower against all fleets.
		int numCombats = nFleets;
		for(int i = 0; i < numCombats; i++) {
			// Determine combat distribution.
			if(debug) System.out.println("Resolving combat for fleet " + i + " with firepower " + fleetCombat[i]);
			while(fleetCombat[i] > 0) {
				int targetFleet; int nTries = 0;
				do {
					targetFleet = (int)(Math.random() * nFleets);
				} while ((nShips[targetFleet] <= 0 || fleets[targetFleet][0].getLoyalty() == fleetAlignments[i]) && nTries++ < 20);
				if(nTries >= 20) {
					// assume no more targets
					if(debug) System.out.println("-No more targets. Moving on...");
					break;
				}
				int targetShip = (int)(Math.random() * nShips[targetFleet]);
				float shipDef = fleets[targetFleet][targetShip].getArmor();
				if(debug) System.out.println("-Target fleet " + targetFleet + ", ship " + targetShip + " with defense of " + shipDef);
				if(!fleets[targetFleet][targetShip].Defend(fleetCombat[i])) {
					if(debug) System.out.println("-Ship destroyed. Removing from list.");
					// remove ship from array
					for(int s = targetShip; s < nShips[targetFleet] - 1; s++)
					{
						fleets[targetFleet][s] = fleets[targetFleet][s + 1];
					}
					nShips[targetFleet]--;
					if(nShips[targetFleet] <= 0) {
						if(debug) System.out.println("-Fleet destroyed. Removing from list.");
						// if fleet is empty, remove from array
						for(int f = targetFleet; f < nFleets - 1; f++)
						{
							fleets[f] = fleets[f + 1];
							nShips[f] = nShips[f + 1];
						}
						nFleets--;
					}
				}
				fleetCombat[i] -= shipDef;
				if(debug) System.out.println("Fleet Combat decreased to " + fleetCombat[i]);
			}
			
			if(debug) System.out.println("Remaining number of fleets is " + nFleets);
			if(nFleets > 1) {
				conflicted = true;
			} else 
			{
				conflicted = false;
			}
			/*if(nFleets > 2) {
				double[] distribution = new double[nFleets - 1];
				for(int j = 0; j < nFleets - 1; j++) {
					
				}
			} else {
				while(fleetCombat[i] > 0) {
					
				}
			}//*/
		}
		
		// If planet, determine planet's allegiance.
		if(_planet != null && !_occupants.isEmpty() && !conflicted) {
			if(_occupants.get(0).getLoyalty() != _planet.getAlliance())
				_planet.Invade(_occupants.get(0).getLoyalty());
		}
	}
	
	public void setPlanet(Planet p) {
		if(_planet != null) {
			TurnManager.unregisterPlanet(_planet);
		}
		_planet = p;
		TurnManager.registerPlanet(_planet);
	}
	
	public Planet getPlanet() {
		return _planet;
	}
	
	public void Activate() {
		if(_occupants.isEmpty()) return;
		
		Ship[] ships = getOccupants();
		
		for(int i = 0; i < ships.length; i++) {
			Ship s = ships[i];
			s.enactOrders();
		}
	}
	
	public static int distance(Location firstLoc, Location secondLoc) {
		return (int) Math.floor(Math.sqrt(Math.pow(firstLoc._col - secondLoc._col, 2) + Math.pow(firstLoc._row - secondLoc._row, 2)));
	}

	@Override
	public Location getSelectedLoc() {
		return this;
	}
	
	public boolean isConflicted() {
		return conflicted;
	}

	@Override
	public Image getSelectedImage() {
		return null;
	}
}
