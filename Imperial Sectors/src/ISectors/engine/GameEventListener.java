package ISectors.engine;

public interface GameEventListener {
	/**
	 * Called when a game event happens
	 * @param e The event that has occurred.
	 */
	public void OnGameEventOccurred(GameEvent e);
}
