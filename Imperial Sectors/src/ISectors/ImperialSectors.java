package ISectors;

import javax.swing.SwingUtilities;

import ISectors.view.BattleWindow;

public class ImperialSectors implements Runnable {

	public void run() {
		BattleWindow b = new BattleWindow();
		b.setVisible(true);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new ImperialSectors());
	}

}
