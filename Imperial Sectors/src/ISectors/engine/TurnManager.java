package ISectors.engine;

import java.util.ArrayList;

import ISectors.planets.Planet;
import ISectors.ships.Ship;

/**
 * @author SCPX
 *
 */
public class TurnManager {
	private static ArrayList<ArrayList<Ship>> playerFleets;
	private static ArrayList<Location> activeLocations;
	private static ArrayList<Planet> planetarySystem;
	private static Location tempSelectedLoc = null;
	private static float maxSpeed = 0;
	
	public static boolean NoFoW = false;

	public static int numPlayers;
	public static int currentPlayer = 1;
	public static boolean[] playerFactions;
	
	public static void initManager(int nPlayers) {
		playerFleets = new ArrayList<ArrayList<Ship>>();
		activeLocations = new ArrayList<Location>();
		planetarySystem = new ArrayList<Planet>();
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
		GameManager.selectedObj = null;
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
			}
		}
		
		// Resolve all active Locations
		for(int loc = 0; loc < activeLocations.size(); loc++) {
			activeLocations.get(loc).Resolve();
		}
		
		for(int planet = 0; planet < planetarySystem.size(); planet++) {
			planetarySystem.get(planet).enactOrders();
		}
	}
	
	public static boolean isLocationVisible(Location l) {
		if(NoFoW) return true;
		
		//Check all ships in player's fleet to see if any are within range.
		ArrayList<Ship> fleet = playerFleets.get(currentPlayer - 1);
		for(int i = 0; i < fleet.size(); i++) {
			Ship s = fleet.get(i);
			if(Location.distance(s.getLoc(), l) <= s.getSensorRange()) {
				return true;
			}
		}
		
		//Check if any are within range of planet as well.
		for(int p = 0; p < planetarySystem.size(); p++) {
			Planet planet = planetarySystem.get(p);
			if(planet.getAlliance() == currentPlayer && Location.distance(planet.getLocation(), l) <= planet.getSensorRange()) {
				return true;
			}
		}
		return false;
	}
	
	private static void resetTempValues() {
		tempSelectedLoc = null;
	}
	
	public static void registerPlanet(Planet p) {
		if(!planetarySystem.contains(p))
			planetarySystem.add(p);
	}
	
	public static void unregisterPlanet(Planet p) {
		if(planetarySystem.contains(p))
			planetarySystem.remove(p);
	}
	
	public static boolean isReachable(Location l) {
		if(GameManager.selectedObj instanceof Ship) {
			tempSelectedLoc = null;
			if(Location.distance(((Ship)(GameManager.selectedObj)).getLoc(), l) <= ((Ship)(GameManager.selectedObj)).getSpeed()) {
				return true;
			}
		} else if(GameManager.selectedObj instanceof Location) {
			if(GameManager.selectedObj != tempSelectedLoc) {
				Ship[] ships = ((Location)(GameManager.selectedObj)).getOccupants();
				maxSpeed = Float.MAX_VALUE;
				for(int i = 0; i < ships.length; i++) {
					if(ships[i].getLoyalty() == currentPlayer && ships[i].getSpeed() < maxSpeed) {
						maxSpeed = ships[i].getSpeed();
					}
				}
				if(maxSpeed >= Float.MAX_VALUE) {
					maxSpeed = 0.0f;
				}
				tempSelectedLoc = (Location)(GameManager.selectedObj);
			}
			if(Location.distance((Location)(GameManager.selectedObj), l) <= maxSpeed) {
				return true;
			}
		}
		return false;
	}
}
