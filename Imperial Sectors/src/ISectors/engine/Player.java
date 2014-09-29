package ISectors.engine;

import java.awt.Color;
import java.util.ArrayList;

import ISectors.planets.Planet;
import ISectors.ships.Ship;

public class Player {
	public String Name;
	private Color AssociatedColor;
	private boolean _isAI = false;
	private int playerNumber;
	private ArrayList<Ship> fleet;
	private ArrayList<Planet> territory;
	
	public Player(int playerNumber, boolean isAI) {
		this._isAI = isAI;
		this.playerNumber = playerNumber;
		Name = "Player " + playerNumber;
		AssociatedColor = Color.blue;
		fleet = new ArrayList<Ship>();
		territory = new ArrayList<Planet>();
	}
	
	public Player(int playerNumber) {
		_isAI = false;
		this.playerNumber = playerNumber;
		Name = "Player " + playerNumber;
		AssociatedColor = Color.blue;
		fleet = new ArrayList<Ship>();
		territory = new ArrayList<Planet>();
	}
	
	public void addShip(Ship craft) {
		fleet.add(craft);
		TurnManager.addLocation(craft.getLoc());
	}
	
	public void removeShip(Ship craft) {
		fleet.remove(craft);
		if(craft.getLoc() != null) {
			TurnManager.removeLocation(craft.getLoc());
		}
	}
	
	public void addPlanet(Planet newTerritory) {
		territory.add(newTerritory);
	}
	
	public void removePlanet(Planet planet) {
		territory.remove(planet);
	}
	
	public boolean controlsPlanet(Planet planet) {
		return territory.contains(planet);
	}
	
	public Color getColor() {
		return AssociatedColor;
	}
	
	public void setColor(Color newColor) {
		AssociatedColor = newColor;
		for(Ship s : fleet) {
			s.reloadIcon();
		}
	}
	
	public boolean isAI() {
		return _isAI;
	}
	
	public void setAI(boolean ai) { 
		_isAI = ai;
	}
	
	public ArrayList<Ship> getFleet() {
		return fleet;
	}
	
	public ArrayList<Planet> getTerritory() {
		return territory;
	}
	
	public int getPlayerNum() {
		return playerNumber;
	}
	
	public void resolveFleets() {
		for(int i = 0; i < fleet.size(); i++) {
			fleet.get(i).enactOrders();
		}
	}
	
	public void TakeTurn() {
		if(!_isAI) {
			return;
		}
		
		if(GameManager.debug) System.out.println("Player " + playerNumber + " is taking it's turn.");
		
		TurnManager.nextTurn();
	}
}
