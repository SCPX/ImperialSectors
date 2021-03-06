package ISectors.engine;

import java.awt.Color;
import java.util.ArrayList;

import ISectors.planets.Planet;
import ISectors.ships.Ship;

/**
 * @author SCPX
 *
 */
public class TurnManager {
	public static final int MAX_PLAYERS = 10;
//	private static ArrayList<ArrayList<Ship>> playerFleets;
	private static ArrayList<Location> activeLocations;
	private static ArrayList<Planet> planetarySystem;
	private static ArrayList<Player> players;
	private static Location tempSelectedLoc = null;
	private static float maxSpeed = 0;
//	private static Color[] playerColors = {Color.blue, Color.red, Color.green, Color.yellow, Color.white, Color.cyan, Color.orange, Color.lightGray, Color.magenta, Color.pink};
	
	public static boolean NoFoW = false;

	public static int numPlayers;
	public static int currentPlayer = 1;
//	public static boolean[] playerFactions;
	
	public static void initManager(int nPlayers) {
//		playerFleets = new ArrayList<ArrayList<Ship>>();
		activeLocations = new ArrayList<Location>();
		planetarySystem = new ArrayList<Planet>();
		players = new ArrayList<Player>();
		numPlayers = nPlayers;
		currentPlayer = 1;
//		playerFactions = new boolean[nPlayers];
		for(int i = 0; i < nPlayers; i++) {
			players.add(new Player(i + 1));
		}
	}
	
	public static void setPlayerFaction(int playerNum, boolean isPC) {
		players.get(playerNum - 1).setAI(!isPC);
	}
	
	public static Player getPlayer(int playerNum) {
		return players.get(playerNum - 1);
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
	
	public static void nextTurn() {
		if(GameManager.Instance.isGameOver())
			return;
		currentPlayer++;
		resetTempValues();
		GameManager.selectObj(null);
		if(currentPlayer > numPlayers) {
			endRound();
			currentPlayer = 1;
		}
		if(players.get(currentPlayer - 1).isAI()){
			players.get(currentPlayer - 1).TakeTurn();
		}
	}
	
	public static void endRound() {
//		activeLocations.clear();
		for(int i = 0; i < numPlayers; i++) {
			players.get(i).resolveFleets();
		}
		
		// Resolve all active Locations
		for(int loc = 0; loc < activeLocations.size(); loc++) {
			activeLocations.get(loc).Resolve();
		}
		
		for(int planet = 0; planet < planetarySystem.size(); planet++) {
			planetarySystem.get(planet).enactOrders();
		}
		
		GameManager.CheckEndGame();
	}
	
	public static boolean isLocationVisible(Location l) {
		if(NoFoW) return true;
		
		//Check all ships in player's fleet to see if any are within range.
		ArrayList<Ship> fleet = players.get(currentPlayer - 1).getFleet();
		for(int i = 0; i < fleet.size(); i++) {
			Ship s = fleet.get(i);
			if(Location.distance(s.getLoc(), l) <= s.getSensorRange()) {
				return true;
			}
		}
		
		//Check if any are within range of planet as well.
		ArrayList<Planet> playerSystems = players.get(currentPlayer - 1).getTerritory();
		for(int p = 0; p < playerSystems.size(); p++) {
			Planet planet = playerSystems.get(p);
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
	
	public static void setPlayerData(String[] names, Color[] colors, boolean[] isAi) {
		for(int i = 0; i < names.length && i < colors.length && i < players.size(); i++) {
			Player p = players.get(i);
			p.setColor(colors[i]);
			p.Name = names[i];
			p.setAI(isAi[i]);
		}
	}
	
	public static boolean isReachable(Location l) {
		if(GameManager.getSelectedObj() instanceof Ship) {
			tempSelectedLoc = null;
			if(Location.distance(((Ship)(GameManager.getSelectedObj())).getLoc(), l) <= ((Ship)(GameManager.getSelectedObj())).getSpeed()) {
				return true;
			}
		} else if(GameManager.getSelectedObj() instanceof Location) {
			if(GameManager.getSelectedObj() != tempSelectedLoc) {
				Ship[] ships = ((Location)(GameManager.getSelectedObj())).getOccupants();
				maxSpeed = Float.MAX_VALUE;
				for(int i = 0; i < ships.length; i++) {
					if(ships[i].getLoyalty() == currentPlayer && ships[i].getSpeed() < maxSpeed) {
						maxSpeed = ships[i].getSpeed();
					}
				}
				if(maxSpeed >= Float.MAX_VALUE) {
					maxSpeed = 0.0f;
				}
				tempSelectedLoc = (Location)(GameManager.getSelectedObj());
			}
			if(Location.distance((Location)(GameManager.getSelectedObj()), l) <= maxSpeed) {
				return true;
			}
		}
		return false;
	}

	public static void setPlayerColor(int player, Color newColor) {
		if(newColor != null)
			players.get(player - 1).setColor(newColor);
	}
	
	public static Color getPlayerColor(int player) {
		return players.get(player - 1).getColor();
	}
}
