package ISectors.ships;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import ISectors.ships.Ship;

/**
 * @author SCPX
 *
 */
public class Battleship extends Ship {

	public Battleship(int player) {
		super(player);
		this._armor = 4;
		this._maxArmor = 4;
		this._firepower = 3;
		this._sensors = 1;
		this._shipName = "Battleship";
		this._speed = 1;
		this._tier = 0;
		
		try{
			this._icon = ImageIO.read(getClass().getResource("/resources/Battleship.png"));
		} catch (IOException e) {
			e.printStackTrace();
			this._icon = null;
		}
	}
	
	@Override
	public void Upgrade() {
		JOptionPane.showMessageDialog(_location, "This ship can not be upgraded any further!");
		this._upgrading = false;
	}

	@Override
	public boolean canUpgrade() {
		return false;
	}

}
