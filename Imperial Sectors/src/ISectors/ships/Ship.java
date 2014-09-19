package ISectors.ships;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import ISectors.engine.Location;
import ISectors.engine.TurnManager;
import ISectors.engine.Selectable;
import ISectors.engine.Orders;

public abstract class Ship implements Selectable {
		protected int _tier; // Level of power for the ship. Used for upgrading.
		protected float _firepower; // Ship's general attack. Compared to the enemy's armor for deciding battles.
		protected float _armor; // Ship's general defense. Compared to the enemy's firepower for deciding battles.
		protected float _maxArmor; // Ship's maximum defense. What the armor starts at when game starts.
		protected float _speed; // Number of sectors a ship can traverse per turn.
		protected float _sensors; // Range in sectors that a ship can clear the fog of war.
		protected Location _location; // Current sector which the ship resides in.
//		protected Fleet _fleet;
		protected String _shipName; // Stores the name of the type of ship.
		private boolean _created; // Stores whether or not the unit has been created yet.
		protected BufferedImage _icon = null; // The image used to represent this ship.
		private boolean _colored = false; // Used to determine if the icon has been recolored or not.

		// Order Data
		protected Orders _order = Orders.STANDBY;
		private Location _destination = null;
		protected Orders[] _availableOrders;
		
		protected int _player;
		protected boolean _upgrading;

 		public Ship(int playerLoyalty) {
			_created = false;
			_player = playerLoyalty;
			_availableOrders = new Orders[3];
			_availableOrders[0] = Orders.MOVE;
			_availableOrders[1] = Orders.STANDBY;
			_availableOrders[2] = Orders.DECOMMISSION;
		}

		/**
		 * When defeated, a ship must destroy itself.
		 * This removes the ship from the map and deconstructs the ship.
		 */
	 	public void Destroy()
		{
	 		_location.LeaveSector(this);
	 		TurnManager.getPlayer(_player).removeShip(this);
			_tier = -1;
			//_fleet = null;
			_location = null;
			_created = false;
		}
		
	 	/**
	 	 * Whenever a ship is put on the map, it must call create to be added.
	 	 * This creates the ship and sets it's location and player affiliation.
	 	 * A ship does not 'exist' until this method is called.
	 	 * @param The location at which the ship shall start at.
	 	 * @param The affiliated player to which this ship belongs.
	 	 */
		public void Create(Location location)//, Player alignment)
		{
			this._location = location;
			TurnManager.getPlayer(_player).addShip(this);
			_created = true;
		}
		
		/**
		 * Standard move order. Moves the ship from the current location to the new location.
		 * @param The location that this ship is moving to.
		 */
		protected void MoveTo(Location destination) 
		{
			if(destination == null || destination == _location) return;
			
			_location.LeaveSector(this);
			_location = destination;
			destination.EnterSector(this);
		}
		
		/**
		 * When entering an enemy's sector, this ship must attack.
		 * Attempts to destroy the enemy ship. In failing, will move to nearest location to enemy.
		 * @param the target location where the enemy resides.
		 */
		protected void Attack(Location destination)
		{
			if(destination == null || destination.getOccupants() == null) return;
			
//			if(!destination.getResident().Defend(_firepower))
//			{
//				MoveTo(destination, false);
//			} else
//			{
				// If attack failed, find the closest location not occupied by someone.
//				Sector newLoc = destination;
//				do
//				{
//					newLoc = BattleMap.getNeighborInDirectionOf(newLoc,  _location);
//				} while(newLoc.isOccupied() && newLoc != _location);
				
//				if(newLoc != _location)
//				{
//					MoveTo(newLoc, false);
//				}
//			}
		}
		
		/**
		 * When an enemy is attacking this ship, it must defend from the attack.
		 * If the ship's defense is higher than the enemy's offense, then this ship will remain stationary.
		 * If the ship's defense is lower than the enemy's offense, then this ship will be destroyed and
		 * remove itself from the game.
		 * @param Enemy's offensive power
		 * @return true if this ship successfully defends against the enemy's assault. False otherwise.
		 */
		public boolean Defend(float enemyFire) 
		{
			if(enemyFire >= _armor) {
				Destroy();
				return false;
			} else {
				_armor -= enemyFire;
				return true;
			}
		}
		
/*		public void leaveFleet() {
			_fleet = null;
		}
		
		public void joinFleet(Fleet f) {
			_fleet = f;
		}*/
		
		/**
		 * Retrieves the icon representation of this ship for GUI purposes.
		 * @return the <code>BufferedImage</code> representation of this ship.
		 * @throws IOException When file can not be opened.
		 */
		public BufferedImage getShipImage() {
			if(_icon == null) {
				BufferedImage icon;
				try{
					icon = ImageIO.read(getClass().getResource("/resources/Ship.png"));
				} catch(IOException e) {
					System.err.println("Could not find Ship.png!");
					e.printStackTrace();
					icon = null;
				}
				return icon;
			}

			if(!_colored) {
				Color playerColor = TurnManager.getPlayerColor(_player);
				for(int y = 0; y < _icon.getHeight(); y++) {
					for(int x = 0; x < _icon.getWidth(); x++) {
						Color pixel = new Color(_icon.getRGB(x, y), true);
						
						if(pixel.getAlpha() > 0) {
							float[] color1components = pixel.getRGBComponents(null);
							float[] color2components = playerColor.getRGBColorComponents(null);
							float[] newColorComponents = new float[3];
							for(int component = 0; component < 3; component++) {
								newColorComponents[component] = color1components[component] * color2components[component];
							}
							Color newColor;
							newColor = new Color(newColorComponents[0], newColorComponents[1], newColorComponents[2], color1components[3]);

							_icon.setRGB(x, y, newColor.getRGB());
						} 
					}
				}
				_colored = true;
			}
			// TODO: Rotate ship image
			return _icon;
		}
		
		public static BufferedImage getSquadImage(){
			BufferedImage icon;
			try{
				icon = ImageIO.read(Ship.class.getResource("/resources/Squadron.png"));
			} catch(IOException e) {
				System.err.println("Could not find Squadron.png!");
				e.printStackTrace();
				icon = null;
			}
			return icon;
		}
		
		/**
		 * Increases the ship's tier and transforms to a new ship type.
		 */
		public abstract void Upgrade();
		
		/**
		 * Checks if the order is valid, then assigns the order to the ship if it is.
		 * If the order is invalid, this ship will retain it's previous orders.
		 * @param order The order for the ship to perform. Comes from the enumerated data type Orders.
		 */
		public void assignOrder(Orders order) {
			if(isValidOrder(order)){
				if(!_upgrading) {
					_order = order;
				} else {
					JOptionPane.showMessageDialog(_location, "Cannot assign order! The ship is currently being upgraded!");
				}
			} else {
				System.out.println("Invalid order for " + _shipName);
			}
		}
		
		/**
		 * Checks if the order is valid, then assigns the order to the ship if it is.
		 * If the order is invalid, this ship will retain it's previous orders.
		 * Also sets the given location as the ship's destination. (For the MOVE order)
		 * @param order The order for the ship to perform. Comes from the enumerated data type Orders
		 * @param dest The Location that the ship should move to when given a MOVE order.
		 */
		public void assignOrder(Orders order, Location dest) {
			if(isValidOrder(order)) {
				if(!_upgrading) {
					if(order == Orders.MOVE){
						if(Location.distance(_location, dest) > _speed) {
							System.err.println("Location is too far!");
//							JOptionPane.showMessageDialog(_location, "Location is too far! Please select a location within range of this ship's engines.");
							return;
						}
					}
					_destination = dest;
					_order = order;
				} else {
					JOptionPane.showMessageDialog(_location, "Cannot assign order! The ship is currently being upgraded!");
				}
			} else {
				System.out.println("Invalid order for " + _shipName);
			}
		}

		/**
		 * Performs the orders assigned to this ship.
		 */
		public void enactOrders() {
			switch(_order){
			case MOVE:
				MoveTo(_destination);
				break;
			case DECOMMISSION:
				Destroy();
				break;
			case STANDBY:
				//If damaged, perform repairs
				if(_armor < _maxArmor)
				{
					_armor += 1;
				}
				break;
			default:
				System.err.println("Invalid order for " + _shipName + " at " + _location);
			}
			_order = Orders.STANDBY;
			_destination = null;
		}
		
		/**
		 * Checks if the ship can perform the given order.
		 * @param order is the order to be checked.
		 * @return true if the ship can perform the given order, false otherwise.
		 */
		public boolean isValidOrder(Orders order) {
			for(int i = 0; i < _availableOrders.length; i++) {
				if(order == _availableOrders[i]) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public Location getSelectedLoc() {
			return this._location;
		}
		
		/**
		 * Returns an array of all available orders for a given ship.
		 * @return
		 */
		public Orders[] getOrders()  {
			return _availableOrders;
		}
		
		/**
		 * Gets a string list of orders available for the current ship
		 * @return String array of all available orders.
		 */
		public String[] getOrderList() {
			String[] ordersList = new String[_availableOrders.length];
			for(int i = 0; i < _availableOrders.length; i++) {
				ordersList[i] = Orders.OrderToString(_availableOrders[i]);
			}
			return ordersList;
		}
		
		/**
		 * Returns the currently assigned order to this ship.
		 * @return
		 */
		public Orders getCurrentOrder() {
			return _order;
		}
		
		/**
		 * Informs this ship that it is to be upgraded.
		 * @return true if this ship is able to be upgraded.
		 */
		public boolean setUpgrading(boolean val) {
			if(val) {
				if(this.canUpgrade()) {
					if(!this.isUpgrading()){
						_upgrading = true;
						return true;
					} else {
						JOptionPane.showMessageDialog(_location, getName() + " is already set for being upgraded!");
						return false;
					}
				} else {
					JOptionPane.showMessageDialog(_location, getName() + " can not be upgraded anymore!");
					return false;
				}
			} else {
				_upgrading = false;
				return true;
			}
		}
		
		/**
		 * Generic getter for the firepower variable.
		 * @return firepower rating of this ship.
		 */
		public float getFirepower() {
			return _firepower;
		}
		
		/**
		 * Generic getter for the armor variable.
		 * @return armor of this ship.
		 */
		public float getArmor() {
			return _armor;
		}
		
		/**
		 * Generic getter for the speed variable.
		 * @return speed in number of sectors per turn of this ship.
		 */
		public float getSpeed()
		{
			return _speed;
		}
		
		/**
		 * Generic getter for location variable. To set location, you must call MoveTo.
		 * @return the current location of this ship.
		 */
		public Location getLoc()
		{
			return _location;
		}
		
		/**
		 * Checks if ship can upgrade, or if ship is not upgradable.
		 * @return true if ship can be upgraded.
		 */
		public abstract boolean canUpgrade() ;
		
		/**
		 * Generic getter for the sensor variable. Sensor range is determined in the number of 
		 * sectors out that this ship can see out.
		 * @return the number of sectors away from this ship that can be seen.
		 */
		public float getSensorRange()
		{
			return _sensors;
		}
		
		/**
		 * Generic getter for this ship's alliance.
		 * @return the Player number affiliated with this Ship.
		 */
		public int getLoyalty()
		{
			return _player;
		}//*/
		
		/**
		 * Generic getter for this ship's name.
		 * @return a string containing the ship's display name.
		 */
		public String getName()
		{
			return _shipName;
		}

		/**
		 * Checks whether the ship is currently upgrading or not.
		 * @return true if ship is currently upgrading.
		 */
		public boolean isUpgrading() {
			return _upgrading;
		}
		
		/**
		 * Generic getter for this ship's battle orders, if any.
		 * @return the current <code>CommandOrders</code> affiliated to this ship, or <code>null</code> if there is none.
		 */
/*		public CommandOrders getCommand()
//		{
//			return _currentOrders;
//		}//*/
		
		/**
		 * Assigns new orders to this ship, removing the old ones if there are any.
		 * @param the new orders being assigned to this ship.
		 */
/*		public void assignOrders(CommandOrders incomingOrders) 
//		{
//			if(incomingOrders.isValid())
//			{
//				if(_currentOrders != null)
//				{
//					this._playerAlignment.removeOrder(_currentOrders);
//					_currentOrders = null;
//				}
//				_currentOrders = incomingOrders;
//				this._playerAlignment.addOrder(incomingOrders);			
//			}
//		}//*/
		
		/**
		 * Checks whether a ship has been "created" or not. To Create a ship, call <code>Create</code>.
		 * @return true if ship "exists", false otherwise.
		 */
		public boolean isCreated()
		{
			return _created;
		}

		/**
		 * Checks whether a given ship is an enemy of this ship or not.
		 * @param The possible enemy ship.
		 * @return true if the <code>target</code> is a different affiliated player. False otherwise.
		 */
/*		public boolean isEnemy(Ship target) 
		{
			if(target == null) return false;
			if(target.getAlignment() != this._playerAlignment)
				return true;
			return false;
		}//*/
}
