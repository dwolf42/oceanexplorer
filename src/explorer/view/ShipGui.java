package explorer.view;

import explorer.ShipApp;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Scanner;

// Please note: code regarding the torpedo-feature is AI generated
public class ShipGui {
	private ShipApp shipApp;

	public ShipGui(ShipApp shipApp) {
		this.shipApp = shipApp;
		// Create window
		Frame win = new Frame(shipApp.getShipId());
		win.setLayout(new GridLayout(0, 3));
		// Set window size
		win.setSize(350, 350);

		ButtonFactory buttonFactory = new ButtonFactory();
		ArrayList<Button> buttons = buttonFactory.doMakeMeButtons(this.shipApp);
		for (Button button : buttons) {
			win.add(button);
		}

		// Make window and buttons visible
		win.setVisible(true);
	}

}
