package ISectors.ships;

import java.io.IOException;

import javax.imageio.ImageIO;

import ISectors.ships.Ship;

/**
 * @author SCPX
 *
 */
public class Frigate extends Ship {

	public Frigate(int player) {
		super(player);
		this._armor = 2;
		this._maxArmor = 2;
		this._firepower = 1;
		this._sensors = 2;
		this._shipName = "Frigate";
		this._speed = 2;
		this._tier = 3;

		try{
			this._icon = ImageIO.read(getClass().getResource("/resources/Frigate.png"));
		} catch (IOException e) {
			e.printStackTrace();
			this._icon = null;
		}
	}
	
	@Override
	public void Upgrade() {
		Cruiser c = new Cruiser(_player);
		_location.EnterSector(c);
		this.Destroy();
	}

	@Override
	public boolean canUpgrade() {
		return true;
	}

}
