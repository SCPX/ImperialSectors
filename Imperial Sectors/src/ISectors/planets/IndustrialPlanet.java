package ISectors.planets;

import javax.swing.JOptionPane;

import ISectors.engine.Location;
import ISectors.engine.Orders;
import ISectors.ships.ScoutShip;
import ISectors.ships.Ship;

public class IndustrialPlanet extends Planet {

	public IndustrialPlanet(Location loc) {
		super(loc);
		_name = "Industrial Planet";
		_description = "This busy planet contains ship yards and factories, making it the perfect place to enhance your production.";
		_location = loc;
		
		_orderable = true;
		_availableOrders = new Orders[3];
		_availableOrders[0] = Orders.STANDBY;
		_availableOrders[1] = Orders.BUILD;
		_availableOrders[2] = Orders.UPGRADE;
	}

	@Override
	public void enactOrders() {
		if(!_location.isConflicted()) {
			if(_order == Orders.BUILD) {
				ScoutShip s = new ScoutShip(_owner);
				_location.EnterSector(s);
			} else if(_order == Orders.UPGRADE) {
				_orderTarget.setUpgrading(false);
				_orderTarget.Upgrade();
				_orderTarget = null;
			}
		}
		_order = Orders.STANDBY;
	}

	@Override
	public void assignOrder(Orders order, Ship target) {
		if(_location.isConflicted()) {
			JOptionPane.showMessageDialog(_location, "Planet is in a conflicted zone! Can not perform orders!");
		}
		if(order == Orders.UPGRADE) {
			if(target.getLoc() == _location) {
				if(target.setUpgrading(true)) {
					clearOrder();
					_orderTarget = target;
					_order = order;
				}
			}
		} else {
			if(IsValidOrder(order)) 
				clearOrder();
			_order = order;
		}
	}
	
	private void clearOrder() {
		if(_order == Orders.UPGRADE) {
			_orderTarget.setUpgrading(false);
			_orderTarget = null;
		}
	}
}
