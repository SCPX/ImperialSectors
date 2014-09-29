package ISectors.engine;

/**
 * Object class for storing Game Event data. Used for triggering event signals.
 * @author Owner
 *
 */
public class GameEvent {
	public static final int GAME_OVER = 0;
	public static final int SELECTION_CHANGED = 1;
	private int eventType;
	
	public GameEvent(int eventType) {
		this.eventType = eventType;
	}
	
	public int getEventType() {
		return eventType;
	}
}
