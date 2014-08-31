package ISectors.ships;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import ISectors.engine.Location;

/**
 * A wrapper class for storing a list of ships
 * All ships belong to a fleet, and combat is organized per fleet.
 * @author SCPX
 */
public class Fleet {
	private ArrayList<Ship> _ships;
	private Location _location;
	
	public Fleet(Location loc) {
		_ships = new ArrayList<Ship>();
		_location = loc;
	}
	
	public void addShip(Ship newShip) {
		if(_ships.add(newShip)) {
			newShip._location = _location;
		}
	}
	
	public void removeShip(Ship s) {
		_ships.remove(s);
	}
	
	public float totalAttackPower() {
		float totalPower = 0;
		for(int i = 0; i < _ships.size(); i++) {
			totalPower += _ships.get(i)._firepower;
		}
		return totalPower;
	}
	
	public float getAverageAttack() {
		float average = totalAttackPower();
		return average / _ships.size();
	}
	
	public Ship[] getShips() {
		return (Ship[])_ships.toArray();
	}
	
	public String ToString() {
		String output = "Fleet : {";
		if(_ships.size() > 0) {
			Ship s = _ships.get(0);
			output += s.getName();
		}
		for(int i = 1; i < _ships.size(); i++) {
			Ship s = _ships.get(i);
			output = output + ", " + s.getName();
		}
		output += "}";
		return output;
	}
	
	public void Destroy() {
		for(int i = 0; i < _ships.size(); i++) {
			Ship s = _ships.get(i);
			s.Destroy();
		}
		_ships.clear();
	}
	
	public void Dismantle() {
		for(int i = 0; i < _ships.size(); i++) {
			Ship s = _ships.get(i);
			//s.leaveFleet();
		}
	}
	
	public void mergeFleet(Fleet f) {
		Ship[] list = getShips();
		Dismantle();
		for(int i = 0; i < list.length; i++) {
			//list[i].joinFleet(f);
		}
	}
	
	public BufferedImage getImage() throws IOException {
		int size = _ships.size();
		if(size < 3) {
			//Display fleet image for three or more ships
		} /*else if(size == 2) {
			//Display pair image for two ships
		}//*/ 
		else if (size == 1) {
			return _ships.get(0).getShipImage();
		} 
		return null;
	}
	//AssignOrder(Order o); // Give all ships in fleet the same order
	//AssignOrder(Order o, Ship s); // Give ship s an order
}
