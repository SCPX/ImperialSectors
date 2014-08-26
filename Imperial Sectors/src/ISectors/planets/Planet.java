package ISectors.planets;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import ISectors.engine.Location;
import ISectors.engine.Selectable;

public abstract class Planet implements Selectable {
	protected Location _location;
	protected String _name;
	protected String _description;
	protected int _owner = -1;
	protected BufferedImage _icon = null;
	
	public Planet(Location loc) {
		_name = "Planet";
		_description = "A planet";
		_location = loc;
	}
	
	public void Destroy() {
		System.err.println("You're not Darth Vader! You can't just destroy planets!");
		// or can you?
	}
	
	public void Invade(int player) {
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
}
