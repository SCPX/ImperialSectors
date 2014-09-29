package ISectors.ships;

import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author SCPX
 *
 */
public class Interceptor extends Ship {

	public Interceptor(int player) {
		super(player);
		this._armor = 1;
		this._maxArmor = 1;
		this._firepower = 1;
		this._sensors = 1;
		this._shipName = "Interceptor";
		this._speed = 4;
		this._tier = 2;
	}
	
	@Override
	public void Upgrade() {
		Frigate f = new Frigate(_player);
		_location.EnterSector(f);
		this.Destroy();
	}

	@Override
	public boolean canUpgrade() {
		return true;
	}

	@Override
	protected void loadIcon() {
		try{
			this._icon = ImageIO.read(getClass().getResource("/resources/Interceptor.png"));
		} catch (IOException e) {
			e.printStackTrace();
			this._icon = null;
		}
		_selectedImage = _icon;
	}

}
