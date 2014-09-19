package ISectors.engine;

import java.util.ArrayList;

public class GameEventHandler {
	ArrayList<GameEventListener> listeners = new ArrayList<GameEventListener>();
	
	public void addListener(GameEventListener toAdd) {
		listeners.add(toAdd);
	}
	
	/**
	 * Sends an alert to all listeners with the given event data
	 * @param e The given event data.
	 */
	public void alertListeners(GameEvent e) {
		for(GameEventListener listener : listeners) {
			listener.OnGameEventOccurred(e);
		}
	}
}
