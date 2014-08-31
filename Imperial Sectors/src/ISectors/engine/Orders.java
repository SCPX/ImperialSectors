package ISectors.engine;

public enum Orders {
	MOVE, STANDBY, DECOMMISSION, BUILD, UPGRADE;

	/**
	 * Converts an order into string format
	 * @param order The order to be printed
	 * @return Name of order in a String
	 */
	public static String OrderToString(Orders order) {
		String retValue;
		switch(order) {
		case MOVE:
			retValue = "Move To";
			break;
		case STANDBY:
			retValue = "Standby";
			break;
		case DECOMMISSION:
			retValue = "Decommision";
			break;
		case UPGRADE: 
			retValue = "Upgrade";
			break;
		case BUILD:
			retValue = "Build Ship";
			break;
		default:
			retValue = "Unknown Order";
		}
		return retValue;
	}

}
