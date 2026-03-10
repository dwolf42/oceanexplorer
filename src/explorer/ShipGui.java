package explorer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

public class ShipGui {
	private ShipApp shipApp;
	private boolean isExploring = true;

	public ShipGui(ShipApp shipApp) {
		this.shipApp = shipApp;
		// Create window
		Frame win = new Frame(shipApp.getShipId());
		win.setLayout(new GridLayout(3, 2));
		// Set window size
		win.setSize(300, 200);

		Button button1 = new Button("Press me");
		MyActionListener myAL = new MyActionListener();
		// Add action listener to button
		button1.addActionListener(myAL);
		// Put button in window
		win.add(button1);
		// Make window and buttons visible
		win.setVisible(true);
	}

	class MyActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("Button presses");
			System.exit(0);
		}
	}
	public void blah() throws InterruptedException {
		Scanner scanner = new Scanner(System.in);

		String input;
		while (isExploring && !Thread.currentThread().isInterrupted()) {
			synchronized (shipApp) {

				System.out.println("System waiting for input:");
				input = scanner.nextLine().toLowerCase();

				switch (input) {
					case "scan":
						shipApp.scan();
						shipApp.wait();
						break;
					case "navigate":
						shipApp.navigate();
						shipApp.wait();
						break;
					case "radar":
						shipApp.radar();
						shipApp.wait();
						break;
					case "sub":
						shipApp.deploySubmarine();
						break;
					case "exit":
						isExploring = false;
						shipApp.exit();
						break;
					default:
						System.out.println("Invalid input");
						break;
				}
			}
		}
	}
}
