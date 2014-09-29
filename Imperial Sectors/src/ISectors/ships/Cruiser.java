package ISectors.ships;

import java.io.IOException;

import javax.imageio.ImageIO;

import ISectors.ships.Ship;

public class Cruiser extends Ship {
	
	public Cruiser(int player) {
		super(player);
		this._armor = 2;
		this._maxArmor = 2;
		this._firepower = 2;
		this._sensors = 1;
		this._shipName = "Cruiser";
		this._speed = 2;
		this._tier = 4;
	}
	
	@Override
	public void Upgrade() {
		Battleship b = new Battleship(_player);
		_location.EnterSector(b);
		this.Destroy();
	}

	@Override
	public boolean canUpgrade() {
		return true;
	}

	@Override
	protected void loadIcon() {
		try{
			this._icon = ImageIO.read(getClass().getResource("/resources/Cruiser.png"));
		} catch (IOException e) {
			e.printStackTrace();
			this._icon = null;
		}
		_selectedImage = _icon;
	}
}
