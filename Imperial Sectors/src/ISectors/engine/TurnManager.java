package ISectors.engine;

import java.util.ArrayList;

import ISectors.ships.Ship;

/**
 * @author SCPX
 *
 */
public class TurnManager {
	private static ArrayList<ArrayList<Ship>> playerFleets;
	private static ArrayList<Location> activeLocations;
	private static Location tempSelectedLoc = null;
	private static float maxSpeed = 0;

	public static int numPlayers;
	public static int currentPlayer = 1;
	public static boolean[] playerFactions;
	
	public static void initManager(int nPlayers) {
		playerFleets = new ArrayList<ArrayList<Ship>>();
		activeLocations = new ArrayList<Location>();
		numPlayers = nPlayers;
		playerFactions = new boolean[nPlayers];
		for(int i = 0; i < nPlayers; i++) {
			ArrayList<Ship> fleet = new ArrayList<Ship>();
			playerFleets.add(fleet);
			playerFactions[i] = false;
		}
		playerFactions[0] = true;
	}
	
	public static void setPlayerFaction(int playerNum, boolean isPC) {
		playerFactions[playerNum - 1] = isPC;
	}
	
	public static boolean isPlayerFaction(int playerNum) {
		return playerFactions[playerNum - 1];
	}
	
	public static void addShip(Ship s, int player) {
		ArrayList<Ship> fleet = playerFleets.get(player - 1);
		fleet.add(s);
		playerFleets.set(player - 1, fleet);
		addLocation(s.getLoc());
	}
	
	public static void removeShip(Ship s, int player) {
		ArrayList<Ship> fleet = playerFleets.get(player - 1);
		fleet.remove(s);
		playerFleets.set(player - 1, fleet);
		if(s.getLoc()!= null) {
			removeLocation(s.getLoc());
		}
	}
	
	public static void removeLocation(Location loc) {
		if(loc.IsEmpty()) {
			activeLocations.remove(loc);
		} else {
			addLocation(loc);
		}
	}
	
	public static void addLocation(Location loc) {
		if(!activeLocations.contains(loc)) {
			activeLocations.add(loc);
		}
	}
	
	public static int playerLoyalty(Ship s) {
		for(int i = 0; i < numPlayers; i++) {
			ArrayList<Ship> fleet = playerFleets.get(i);
			if(fleet.contains(s)) {
				return i+1;
			}
		}
		return -1;
	}
	
	public static void nextTurn() {
		currentPlayer++;
		resetTempValues();
		GameManager.selectedLoc = null;
		GameManager.selectedShip = null;
		if(currentPlayer > numPlayers) {
			endRound();
			currentPlayer = 1;
		}
	}
	
	public static void endRound() {
//		activeLocations.clear();
		for(int i = 0; i < numPlayers; i++) {
			ArrayList<Ship> fleet = playerFleets.get(i);
			for(int j = 0; j < fleet.size(); j++) {
				Ship s = fleet.get(j);
				s.enactOrders();
//				if(s.isCreated()) addLocation(s.getLoc());
			}
		}
		
		// Resolve all active Locations
		System.out.println("Resolving...");
		for(int loc = 0; loc < activeLocations.size(); loc++) {
			activeLocations.get(loc).Resolve();
		}
	}
	
	public static boolean isLocationVisible(Location l) {
		ArrayList<Ship> fleet = playerFleets.get(currentPlayer - 1);
		for(int i = 0; i < fleet.size(); i++) {
			Ship s = fleet.get(i);
			if(Location.distance(s.getLoc(), l) <= s.getSensorRange()) {
				return true;
			}
		}
		return false;
	}
	
	private static void resetTempValues() {
		tempSelectedLoc = null;
	}
	
	public static boolean isReachable(Location l) {
		if(GameManager.selectedShip != null) {
			tempSelectedLoc = null;
			if(Location.distance(GameManager.selectedShip.getLoc(), l) <= GameManager.selectedShip.getSpeed()) {
				return true;
			}
		} else if(GameManager.selectedLoc != null) {
			if(GameManager.selectedLoc != tempSelectedLoc) {
				maxSpeed = 0;
				Ship[] ships = GameManager.selectedLoc.getOccupants();
				for(int i = 0; i < ships.length; i++) {
					if(ships[i].getSpeed() > maxSpeed) {
						maxSpeed = ships[i].getSpeed();
					}
				}
				tempSelectedLoc = GameManager.selectedLoc;
			}
			if(Location.distance(GameManager.selectedLoc, l) <= maxSpeed) {
				return true;
			}
		}
		return false;
	}
}
