/**
 * This is a test class.
 */
package ISectors.engine;

import ISectors.planets.Planet;

/**
 * @author SCPX
 *
 */
public class PrettyPlanet extends Planet {
	public PrettyPlanet(Location loc) {
		super(loc);
		_name = "Pretty Planet";
		_description = "This is a very pretty planet.";
	}
}
