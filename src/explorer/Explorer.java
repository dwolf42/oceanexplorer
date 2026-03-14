package explorer;

import explorer.view.ShipGui;

// Please note: code regarding the torpedo-feature is AI generated
public class Explorer {

	public static void main(String[] args) {
		Explorer exp = new Explorer();
		try {
			exp.explore();
		} catch (InterruptedException e) {
			// -
		}
	}

	// Initialize a new ship and its connection to the OceanServer.
	// Also initialize the ShipGui
	public void explore() throws InterruptedException {
		ShipApp shipApp = new ShipApp("The Ship", "ship");
		ShipGui shipGui;

		synchronized (shipApp) {
			// establish connection to OceanServer
			if (shipApp.connectOS("127.0.0.1", 8150)) {
				System.out.println("Connected to OceanServer");
				shipApp.launch();
				shipApp.wait();
				shipGui = new ShipGui(shipApp);
			} else {
				System.out.println("Failed to connect to OceanServer");
			}
		}


	}

}
