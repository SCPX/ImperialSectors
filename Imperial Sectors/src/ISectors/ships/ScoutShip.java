package ISectors.ships;

import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author SCPX
 * The smallest ship in the fleet, intended for scouting and exploring rather than combat.
 */
public class ScoutShip extends Ship {

	public ScoutShip(int player) {
		super(player);
		this._armor = 1;
		this._maxArmor = 1;
		this._firepower = 0;
		this._sensors = 4;
		this._shipName = "Scout";
		this._speed = 3;
		this._tier = 1;
		
		try{
			this._icon = ImageIO.read(getClass().getResource("/resources/Scout.png"));
		} catch (IOException e) {
			e.printStackTrace();
			this._icon = null;
		}
	}
	
	@Override
	public void Upgrade() {
		Interceptor i = new Interceptor(_player);
		_location.EnterSector(i);
		this.Destroy();
	}

	@Override
	public boolean canUpgrade() {
		return true;
	}

}
