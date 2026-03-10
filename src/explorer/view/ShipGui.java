package explorer.view;

import explorer.ShipApp;

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
		win.setLayout(new GridLayout(3, 3));
		// Set window size
		win.setSize(300, 200);

		Button button1 = new Button("Press me1");
		button1.setSize(20,20);
		MyActionListener myAL1 = new MyActionListener();
		// Add action listener to button
		button1.addActionListener(myAL1);
		// Put button in window
		win.add(button1);


		Button button2 = new Button("Press me2");
		button2.setSize(20,20);
		MyActionListener myAL2 = new MyActionListener();
		// Add action listener to button
		button2.addActionListener(myAL2);
		// Put button in window
		win.add(button2);

		Button button3 = new Button("Press me3");
		button3.setSize(20,20);
		MyActionListener myAL3 = new MyActionListener();
		// Add action listener to button
		button3.addActionListener(myAL3);
		// Put button in window
		win.add(button3);
		// Make window and buttons visible
		win.setVisible(true);


		Button button4 = new Button("Press me4");
		button4.setSize(20,20);
		MyActionListener myAL4 = new MyActionListener();
		// Add action listener to button
		button4.addActionListener(myAL4);
		// Put button in window
		win.add(button4);


		Button button5 = new Button("Press me4");
		button5.setSize(20,20);
		MyActionListener myAL5 = new MyActionListener();
		// Add action listener to button
		button5.addActionListener(myAL5);
		// Put button in window
		win.add(button5);
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
