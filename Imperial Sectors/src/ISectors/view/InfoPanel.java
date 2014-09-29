package ISectors.view;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.*;

import ISectors.engine.GameEvent;
import ISectors.engine.GameEventListener;
import ISectors.engine.GameManager;
import ISectors.engine.Location;
import ISectors.engine.Orders;
import ISectors.engine.Selectable;
import ISectors.engine.TurnManager;
import ISectors.planets.Planet;
import ISectors.ships.Ship;

@SuppressWarnings("serial")
public class InfoPanel extends JDialog implements GameEventListener {
	JPanel pane;
	
	public InfoPanel(Frame owner) {
		super(owner);
		setSize(240, (int)(owner.getHeight() * 0.5));
		GameManager.Instance.addListener(this);;
		pane = new JPanel();
		initComponents();
	}
	
	public void initComponents() {
		pane.removeAll();
		pane.repaint();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		
		Selectable obj = GameManager.getSelectedObj();
		JLabel lblShipImg;
		if(obj != null && obj.getSelectedImage() != null) {
			lblShipImg = new JLabel(new ImageIcon(obj.getSelectedImage()));
			lblShipImg.setSize(new Dimension((int)this.getWidth(), (int)(this.getHeight() * 0.5)));
			pane.add(lblShipImg);
		} else if(!(obj instanceof Location)) {
			lblShipImg = new JLabel("No Image Available");
			lblShipImg.setSize(new Dimension((int)this.getWidth(), (int)(this.getHeight() * 0.5)));
			pane.add(lblShipImg);
		}
		
		if(obj != null) {
			JLabel label;
			if(obj instanceof Location) {
				Location l = (Location)obj;
				JLabel lblName = new JLabel("Location " + l.getX() + ", " + l.getY());
				lblName.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(lblName);
				Ship[] occupants = l.getOccupants();
				if(occupants.length > 0) {
					label = new JLabel("Occupants: ");
					label.setAlignmentX(CENTER_ALIGNMENT);
					pane.add(label);
					for(int i = 0; i < occupants.length; i++) {
						JLabel lblShip = new JLabel("\t" + occupants[i].getName() + " - " + TurnManager.getPlayer(occupants[i].getLoyalty()).Name);
						lblShip.setAlignmentX(CENTER_ALIGNMENT);
						lblShip.setForeground(TurnManager.getPlayerColor(occupants[i].getLoyalty()));
						pane.add(lblShip);
					}
				} else {
					label = new JLabel("Occupants: None");
					label.setAlignmentX(CENTER_ALIGNMENT);
					pane.add(label);
				}
				if(l.getPlanet() != null) {
					label = new JLabel("Planet: " + l.getPlanet().getName());
					label.setAlignmentX(CENTER_ALIGNMENT);
					if(l.getPlanet().getAlliance() != Planet.UNOWNED) 
						label.setForeground(TurnManager.getPlayerColor(l.getPlanet().getAlliance()));
					pane.add(label);
				}
			} else if(obj instanceof Ship) {
				Ship s = (Ship)obj;
				JLabel lblName = new JLabel(s.getName());
				lblName.setForeground(TurnManager.getPlayerColor(s.getLoyalty()));
				lblName.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(lblName);
				label = new JLabel("Current Order: " + Orders.OrderToString(s.getCurrentOrder()));
				label.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(label);
				label = new JLabel("Attack: " + s.getFirepower());
				label.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(label);
				label = new JLabel("Armor: " + s.getArmor());
				label.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(label);
				label = new JLabel("Speed: " + s.getSpeed());
				label.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(label);
				label = new JLabel("Sensor Range: " + s.getSensorRange());
				label.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(label);
				label = new JLabel("Located at " + s.getLoc().getX() + ", " + s.getLoc().getY());
				label.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(label);
			} else if(obj instanceof Planet) {
				Planet p = (Planet)obj;
				label = new JLabel(p.getName());
				label.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(label);
				label = new JLabel("Current Order: " + Orders.OrderToString(p.getCurrentOrder()));
				label.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(label);
				if(p.getAlliance() != Planet.UNOWNED) {
					label = new JLabel("Current Owner: " + TurnManager.getPlayer(p.getAlliance()).Name);
					label.setAlignmentX(CENTER_ALIGNMENT);
					pane.add(label);
				} else {
					label = new JLabel("Current Owner: Unowned");
					label.setAlignmentX(CENTER_ALIGNMENT);
					pane.add(label);
				}
				label = new JLabel(p.getDescription());
				label.setAlignmentX(CENTER_ALIGNMENT);
				pane.add(label);
			}
		} else {
			JLabel lblState = new JLabel("No object selected");
			lblState.setAlignmentX(CENTER_ALIGNMENT);
			pane.add(lblState);
		}
		pane.revalidate();
		this.add(pane);
		this.revalidate();
	}
	
	@Override
	public void OnGameEventOccurred(GameEvent e) {
		if(e.getEventType() == GameEvent.SELECTION_CHANGED) {
			initComponents();
		}
	}
}
