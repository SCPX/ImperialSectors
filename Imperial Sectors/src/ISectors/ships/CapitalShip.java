package ISectors.ships;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import ISectors.engine.Location;
import ISectors.ships.ScoutShip;

/**
 * 
 * @author SCPX
 *
 */
public class CapitalShip extends Ship {	
	private Ship _upgradeTarget = null;
	private float repairRange = (float)1.5;
	
	public CapitalShip(int player) {
		super(player);
		this._armor = 20;
		this._maxArmor = 20;
		this._firepower = 0;
		this._sensors = (float)1.5;
		this._shipName = "Capitol Ship";
		this._speed = 1;
		this._tier = 0;

		try{
			this._icon = ImageIO.read(getClass().getResource("/resources/CapitalShip.png"));
		} catch (IOException e) {
			e.printStackTrace();
			this._icon = null;
		}
		
		_availableOrders = new Orders[4];
		_availableOrders[0] = Orders.MOVE;
		_availableOrders[1] = Orders.STANDBY;
		_availableOrders[2] = Orders.BUILD;
		_availableOrders[3] = Orders.UPGRADE;
	}
	
	@Override
	public void Destroy() {
		super.Destroy();
		JOptionPane.showMessageDialog(_location, "Player " + this._player + " has lost!");
	}
	
	@Override
	public void Upgrade() {
		JOptionPane.showMessageDialog(_location, "This ship can not be upgraded!");
	}
	
	@Override
	public void enactOrders() {
		if(_order == Orders.BUILD) {
			ScoutShip s = new ScoutShip(_player);
			_location.EnterSector(s);
			_order = Orders.STANDBY;
		} else if(_order == Orders.UPGRADE) {
			_upgradeTarget._upgrading = false;
			_upgradeTarget.Upgrade();
			_upgradeTarget = null;
			_order = Orders.STANDBY;
		} else {
			super.enactOrders();
		}
	}
	
	public void assignOrder(Orders order, Ship target) {
		if(order == Orders.UPGRADE) {
			if(Location.distance(_location, target.getLoc()) < repairRange) {
				_upgradeTarget = target;
				_order = order;
				target._upgrading = true;
			} else {
				JOptionPane.showMessageDialog(_location, "The ship is too far away to be upgraded!\nOnly adjacent ships can be upgraded.");
			}
		} else {
			super.assignOrder(order);
		}
	}
	
	@Override
	public void assignOrder(Orders order, Location dest) {
		if(_upgradeTarget != null) {
			_upgradeTarget._upgrading = false;
			_upgradeTarget = null;
		}
		super.assignOrder(order, dest);
	}
	
	@Override
	public void assignOrder(Orders order) {
		if(_upgradeTarget != null) {
			_upgradeTarget._upgrading = false;
			_upgradeTarget = null;
		}
		super.assignOrder(order);
	}

}
