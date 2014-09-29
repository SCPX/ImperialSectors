package ISectors.planets;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import ISectors.engine.Location;
import ISectors.engine.Orders;
import ISectors.engine.Selectable;
import ISectors.engine.TurnManager;
import ISectors.ships.Ship;

public abstract class Planet implements Selectable {
	public static final int UNOWNED = -1;
	protected Location _location;
	protected String _name;
	protected String _description;
	protected int _owner = UNOWNED;
	protected BufferedImage _icon = null;
	
	// Order Data
	protected boolean _orderable = false;
	protected Orders _order = Orders.STANDBY;
	protected Orders[] _availableOrders;
	protected Ship _orderTarget = null;
	protected float sensorRange = 1.0f;
	
	public Planet(Location loc) {
		_name = "Planet";
		_description = "A planet";
		_location = loc;
	}
	
	public void Destroy() {
		System.err.println("You're not Darth Vader! You can't just destroy planets!");
		// or can you?
	}
	
	public void enactOrders() {
		// Planets don't do anything normally.
	}
	
	public void assignOrder(Orders order, Ship target) {
		if(!_orderable) {
			JOptionPane.showMessageDialog(_location, "This planet can not receive orders!");
		}
	}
	
	public void Invade(int player) {
		if(player != _owner) {
			_order = Orders.STANDBY;
			if(_owner != UNOWNED) 
				TurnManager.getPlayer(_owner).removePlanet(this);
			TurnManager.getPlayer(player).addPlanet(this);
		}
		_owner = player;
	}
	
	public int getAlliance() {
		return _owner;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getDescription() {
		return _description;
	}
	
	public Location getLocation() {
		return _location;
	}
	
	public Orders[] getOrders() {
		return _availableOrders;
	}
	
	public Orders getCurrentOrder() {
		return _order;
	}
	
	public BufferedImage getIcon() {
		if(_icon == null) {
			try{
				_icon = ImageIO.read(getClass().getResource("/resources/Planet.png"));
			} catch(IOException e) {
				System.err.println("Could not find Planet.png!");
				e.printStackTrace();
				_icon = null;
			}
		}
		// TODO: Rotate ship image
		return _icon;
	}
	
	public Location getSelectedLoc() {
		return this._location;
	}
	
	public Image getSelectedImage() {
		return null;
	}
	
	public boolean canBeOrdered() {
		return _orderable;
	}
	
	public float getSensorRange() {
		return sensorRange;
	}
	
	/**
	 * Checks if the planet can perform the given order.
	 * @param order is the order to be checked.
	 * @return true if the planet can perform the given order, false otherwise.
	 */
	public boolean IsValidOrder(Orders order) {
		if(_orderable) {
			for(int i = 0; i < _availableOrders.length; i++) {
				if(order == _availableOrders[i]) {
					return true;
				}
			}
		}
		return false;
	}
}
