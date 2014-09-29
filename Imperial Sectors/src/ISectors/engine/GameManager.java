package ISectors.engine;

import java.net.*;
import java.util.Random;

import javax.swing.JOptionPane;

import ISectors.planets.*;
import ISectors.ships.CapitalShip;

public class GameManager extends GameEventHandler {
	public static final int PORT_NUM = 1717;
	public static GameManager Instance;
	public enum GameType { NETWORK, LOCAL };
	public enum GameModes {  DOMINATION, DEATHMATCH, SURVIVAL, CONQUEST };
	
	public static boolean debug = false;

	public static final int DEFAULT_ROWS = 25;
	public static final int DEFAULT_COLS = 25;

	private Selectable selectedObj = null;
	
	private boolean gameOver = false;
	private GameType gameType;
	private GameModes gameMode;
	//private Socket sock;
	//private BufferedReader in; // For network use
	//private PrintWriter out; // For Network use
	private Location[][] _grid;
	private int numRows;
	private int numCols;
	private int numPlanets;
	private int winner = -1;
	
	/**
	 * Initialize the GameManager.
	 */
	public static void Initialize() {
		if(Instance == null) {
			Instance = new GameManager();
		}
	}
	
	/**
	 * Creates a new Game, given the new game's details.
	 * @param type What type of game this will be. (LOCAL or NETWORK)
	 * @param mode What mode is this game. 
	 * @param addr The IP address to transmit the game to.
	 * @param nPlayers The number of players involved in this game.
	 * @param nPlanets The number of planets desired for this game.
	 * @param nRows The width of the game map.
	 * @param nCols The height of the game map.
	 */
	public static void NewGame(GameType type, GameModes mode, InetAddress addr, int nPlayers, int nPlanets, int nRows, int nCols)
	{
		if(type == null || mode == null) {
			System.err.println("Must select a type and mode!");
			return;
		}
		Instance.gameType = type;
		Instance.gameMode = mode;
		if(debug) System.out.println("Mode is " + ModeToString(mode));
		if(type == GameType.NETWORK) {
			Instance.setUPConnection(addr);
			// read number of players from network game
			TurnManager.initManager(nPlayers);
		} else if (type == GameType.LOCAL) {
			TurnManager.initManager(nPlayers);
		}

		Instance.setUpMap(nRows, nCols, nPlanets);
		Instance.gameOver = false;
		Instance.winner = -1;
		Instance.selectedObj = null;
		//ISectors.view.BattleMap.Instance.loadBattleMap(nRows, nCols);//Implemented in BattleWindow.
	}
	
	/**
	 * Retrieves a list of all the game modes, in string format.
	 */
	public static String[] ModesToStrings() {
		GameModes[] modes = GameModes.values();
		String[] modeStrings = new String[modes.length];
		for(int i = 0; i < modes.length; i++) {
			modeStrings[i] = ModeToString(modes[i]);
		}
		return modeStrings;
	}
	
	/**
	 * Takes a given game mode and returns the string version.
	 * @param mode The mode to be converted
	 * @return Returns the string version of the given mode.
	 */
	public static String ModeToString(GameModes mode) {
		switch(mode) {
		case DEATHMATCH : return "Deathmatch";
		case SURVIVAL : return "Survival";
		case DOMINATION : return "Domination";
		case CONQUEST : return "Conquest";
		default : return "Unknown";
		}
	}
	
	/**
	 * Takes a given string and returns the appropriate mode. If there is no mode matching, we return the default mode of Deathmatch
	 * @param mode The string that is to be converted
	 * @return The mode that matches the string.
	 */
	public static GameModes StringToMode(String mode) {
		GameModes[] modes = GameModes.values();
		for(int i = 0; i < modes.length; i++) {
			if(mode.equalsIgnoreCase(ModeToString(modes[i]))) {
				return modes[i];
			}
		}
		return null;
	}
	
	public static void CheckEndGame() {
		// TODO: Implement game checking code here.
		int winningPlayer = -1;
		switch(Instance.gameMode) {
		case DOMINATION : //First player to control >= 75% of all planets in the game.
			float limit = (float) (Instance.numPlanets) * 0.75f;
			for(int i = 0; i < TurnManager.numPlayers; i++) {
				Player p = TurnManager.getPlayer(i + 1);
				if(p.getTerritory().size() >= limit) {
					Instance.gameOver = true;
					winningPlayer = i+1;
				}
			}
			break;
		case SURVIVAL : //Game ends when all players have died. (PvE style)
			break;
		case DEATHMATCH : //Last player to still have his Capital ship wins.
			break;
		case CONQUEST : //Last player with any ship alive wins.
		}
		if(Instance.gameOver) {
			TurnManager.currentPlayer = -1;
			TurnManager.NoFoW = true;
			Instance.winner = winningPlayer;
			Instance.alertListeners(new GameEvent(GameEvent.GAME_OVER));
			if(debug) System.out.println("Player " + winningPlayer + " wins");
		}
	}
	
	public static int GetGameWinner() {
		return Instance.winner;
	}

	public static void selectObj(Selectable obj) {
		Instance.selectedObj = obj;
		Instance.alertListeners(new GameEvent(GameEvent.SELECTION_CHANGED));
	}
	
	public static Selectable getSelectedObj() {
		return Instance.selectedObj;
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

			_grid[xPos][yPos].setPlanet(new IndustrialPlanet(_grid[xPos][yPos]));
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
				
				_grid[xPos][yPos].setPlanet(new IndustrialPlanet(_grid[xPos][yPos]));
				planetLocs[i] = _grid[xPos][yPos];
			}
		}
		
		// Place Players
		minDistance = Math.max(numRows, numCols) / TurnManager.numPlayers;
		maxAttempts = 50 / TurnManager.numPlayers; // We wanna try to enforce the player positions a little more strongly than the planets, if there are only a few players.
		
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
//			sock = new Socket(addr, PORT_NUM);
//			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//			out = new PrintWriter(sock.getOutputStream(), true);
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
	
	public GameModes getGameMode() {
		return gameMode;
	}
	
	public boolean isGameOver() {
		return gameOver;
	}
}
