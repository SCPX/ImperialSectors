package ISectors.engine;

import java.io.*;
import java.net.*;
import java.util.Random;

import javax.swing.JOptionPane;

import ISectors.planets.*;
import ISectors.ships.CapitalShip;

public class GameManager {
	public static final int port_num = 1717;
	public static GameManager Instance;
	public enum GameType { NETWORK, LOCAL };
	
	public static boolean debug = false;

	public static final int DEFAULT_ROWS = 25;
	public static final int DEFAULT_COLS = 25;

	public static Selectable selectedObj = null;
	
	private GameType gameType;
	private Socket sock;
	private BufferedReader in; // For network use
	private PrintWriter out; // For Network use
	private Location[][] _grid;
	private int numRows;
	private int numCols;
	private int numPlanets;
	
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
		selectedObj = null;
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
		this.numPlanets = nPlanets;
		
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
		Location[] planetLocs = null;
		// Place Planets
		if(nPlanets > 0){
			planetLocs = new Location[nPlanets];
			minDistance = Math.max(nRows, nCols) / (float)nPlanets;
			
			if(debug) System.out.println("minDistance for planets is " + minDistance);
			
			do {
				xPos = (int)((r.nextGaussian() * nRows / 6) + (nRows / 2));
				yPos = (int)((r.nextGaussian() * nCols / 6) + (nCols / 2));
			} while(xPos < 0 || xPos >= nRows || yPos  < 0 || yPos >= nCols);

			if(debug) System.out.println("Placing planet at " + xPos + ", " + yPos);

			_grid[xPos][yPos].setPlanet(new PrettyPlanet(_grid[xPos][yPos]));
			planetLocs[0] = _grid[xPos][yPos];
			for(int i = 1; i < nPlanets; i++) {
				attempts = 0;
				do {
					validLoc = true;
					
					do {
						xPos = (int)((r.nextGaussian() * nRows / 6) + (nRows / 2));
						yPos = (int)((r.nextGaussian() * nCols / 6) + (nCols / 2));
					} while(xPos < 0 || xPos >= nRows || yPos  < 0 || yPos >= nCols);
			
					for(int loc = 0; loc < i - 1; loc++) {
						if(Location.distance(planetLocs[loc], _grid[xPos][yPos]) < minDistance) {
							validLoc = false;
							break;
						}
					}
					attempts++;
				} while(!validLoc && attempts < maxAttempts);

				if(debug && attempts >= maxAttempts) System.out.println("Max attempts reached. Placing planet at " + xPos + ", " + yPos);
				else if(debug) System.out.println("Placing planet at " + xPos + ", " + yPos);
				
				_grid[xPos][yPos].setPlanet(new PrettyPlanet(_grid[xPos][yPos]));
				planetLocs[i] = _grid[xPos][yPos];
			}
		}
		
		// Place Players
		minDistance = Math.max(numRows, numCols) / TurnManager.numPlayers;
		
		if(debug) System.out.println("MinDistance for ships is " + minDistance);
		
		Location[] startPoints = new Location[TurnManager.numPlayers];
		xPos = r.nextInt(numRows);
		yPos = r.nextInt(numCols);

		if(debug) System.out.println("Placing player 1's ship at " + xPos + ", " + yPos);

		// Randomly place the first player.
		_grid[xPos][yPos].EnterSector(new CapitalShip(1));
		startPoints[0] = _grid[xPos][yPos];
		for(int i = 2; i <= TurnManager.numPlayers; i++) {
			attempts = 0;
			do {
				validLoc = true;
				
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
				
				if(nPlanets > 0 && validLoc) {
					for(int p = 0; p < nPlanets; p++) {
						if(Location.distance(planetLocs[p], _grid[xPos][yPos]) < minDistance) {
							validLoc = false;
							break;
						}
					}
				}
				
				attempts++;
			} while(!validLoc && attempts < maxAttempts);

			if(debug && attempts >= maxAttempts) System.out.println("Max Attempts reached. Placing player " + i + "'s ship at " + xPos + ", " + yPos);
			if(debug) System.out.println("Placing player " + i + "'s ship at " + xPos + ", " + yPos);
			
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
	
	public int getRows() {
		return numRows;
	}
	
	public int getCols() {
		return numCols;
	}
	
	public int getNPlanets() {
		return numPlanets;
	}
}
