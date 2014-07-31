package ISectors.engine;

import java.io.*;
import java.net.*;
import java.util.Random;

import javax.swing.JOptionPane;

import ISectors.planets.*;
import ISectors.ships.CapitalShip;
import ISectors.ships.Ship;

public class GameManager {
	public static final int port_num = 1717;
	public static GameManager Instance;
	public enum GameType { NETWORK, LOCAL };

	public static final int DEFAULT_ROWS = 25;
	public static final int DEFAULT_COLS = 25;

	public static Location selectedLoc = null;
	public static Ship selectedShip = null;
	
	private GameType gameType;
	private Socket sock;
	private BufferedReader in; // For network use
	private PrintWriter out; // For Network use
	private Location[][] _grid;
	private int numRows;
	private int numCols;

	
	public static void Initialize() {
		if(Instance == null) {
			Instance = new GameManager();
		}
	}
	
	public static void NewGame(GameType type, int nPlayers, InetAddress addr) {
		NewGame(DEFAULT_ROWS, DEFAULT_COLS, type, nPlayers, 0, addr);
	}
	
	public static void NewGame(int nRows, int nCols, GameType type, int nPlayers, int nPlanets, InetAddress addr)
	{
		Instance.gameType = type;
		if(type == GameType.NETWORK) {
			Instance.setUPConnection(addr);
			// read number of players from network game
			TurnManager.initManager(nPlayers);
		} else if (type == GameType.LOCAL) {
			TurnManager.initManager(nPlayers);
		}

		Instance.setUpMap(nRows, nCols, nPlanets);
		selectedLoc = null;
		selectedShip = null;
		//ISectors.view.BattleMap.Instance.loadBattleMap(nRows, nCols);//Implemented in BattleWindow.
	}
	
	public static void NewGame(int nRows, int nCols, GameType type, int nPlayers, int nPlanets) {
		NewGame(nRows, nCols, type, nPlayers, nPlanets, null);
	}
	
	private GameManager() 
	{
	}
	
	private void setUpMap(int nRows, int nCols, int nPlanets) {
		this.numCols = nCols;
		this.numRows = nRows;
		
		_grid = new Location[numRows][numCols];
		
		for(int x = 0; x < numRows; x++) {
			for(int y = 0; y < numCols; y++) {
				_grid[x][y] = new Location(x, y);
			}
		}
		
		float minDistance;
		boolean validLoc;
		Random r = new Random();
		int xPos = 0, yPos = 0;
		int maxAttempts = 5, attempts = 0; 
		// Place Planets
		if(nPlanets > 0){
			Location[] planetLocs = new Location[nPlanets];
			minDistance = Math.max(nRows, nCols) / nPlanets;
			double tmp1,tmp2;
			tmp1 = r.nextGaussian();
			xPos = (int)(tmp1 * nRows);
			tmp2 = r.nextGaussian();
			yPos = (int)(tmp2 * nCols);
			System.out.println("Placing planet at " + xPos + "(" + tmp1 + "), " + yPos + "(" + tmp2 + ")");
			_grid[xPos][yPos].setPlanet(new PrettyPlanet(_grid[xPos][yPos]));
			planetLocs[0] = _grid[xPos][yPos];
			for(int i = 1; i < nPlanets; i++) {
				validLoc = true;
				attempts = 0;
				do {
					tmp1 = r.nextGaussian();
					xPos = (int)(tmp1 * nRows);
					tmp2 = r.nextGaussian();
					yPos = (int)(tmp2 * nCols);
					System.out.println("Placing planet at " + xPos + "(" + tmp1 + "), " + yPos + "(" + tmp2 + ")");
					
					for(int loc = 0; loc < i - 1; loc++) {
						if(Location.distance(planetLocs[i], _grid[xPos][yPos]) < minDistance) {
							validLoc = false;
							break;
						}
					}
					attempts++;
				} while(!validLoc && attempts < maxAttempts);
				_grid[xPos][yPos].setPlanet(new PrettyPlanet(_grid[xPos][yPos]));
				planetLocs[i] = _grid[xPos][yPos];
			}
		}
		
		// Place Players
		minDistance = Math.max(numRows, numCols) / TurnManager.numPlayers;
		Location[] startPoints = new Location[TurnManager.numPlayers];
		xPos = r.nextInt(numRows);
		yPos = r.nextInt(numCols);
		// Randomly place the first player.
		_grid[xPos][yPos].EnterSector(new CapitalShip(1));
		startPoints[0] = _grid[xPos][yPos];
		for(int i = 2; i <= TurnManager.numPlayers; i++) {
			validLoc = true;
			attempts = 0;
			do {
				// Randomly determine a direction and a distance from first player.
				xPos = r.nextInt(numRows);
				yPos = r.nextInt(numCols);
				// Analyze location as a valid location. i.e. not within minDistance of other players and still a valid location.
				for(int loc = 0; loc < i - 1; loc++) {
					if(Location.distance(startPoints[loc], _grid[xPos][yPos]) < minDistance) {
						validLoc = false;
						break;
					}
				}
				attempts++;
			} while(!validLoc && attempts < maxAttempts);
			// If valid, assign location as starting point and move on.
			_grid[xPos][yPos].EnterSector(new CapitalShip(i));
			startPoints[i - 1] = _grid[xPos][yPos];
		}
	}
	
	private void setUPConnection(InetAddress addr) {
		try{
			sock = new Socket(addr, port_num);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Could not connect");
			System.out.println("Could not connect.");
			e.printStackTrace();
		}
	}
	
	public Location[][] getGrid() {
		return _grid;
	}
	
	public GameType getGameType() {
		return gameType;
	}
}
