package explorer;

import explorer.view.ShipGui;
import java.io.IOException;
import java.sql.SQLException;

// Please note: code regarding the torpedo-feature is AI generated
public class Explorer {

	public static void main(String[] args) {
		try {
			WebApp webApp = new WebApp();
			webApp.startWebApplication();
			Explorer exp = new Explorer();
			exp.explore();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

	}

	// Initialize a new ship and its connection to the OceanServer.
	// Also initialize the ShipGui
	public void explore() throws InterruptedException, SQLException, IOException {
		ShipApp shipApp = new ShipApp("Leopard1", "ship");
		ShipGui shipGui;
		synchronized (shipApp) {
			// establish connection to OceanServer
			if (shipApp.connectOS("127.0.0.1", 8150)) {
				System.out.println("Connected to OceanServer");
				shipGui = new ShipGui(shipApp);
				shipApp.setShipGui(shipGui);
				shipApp.launch();
				shipGui.updateWinTitle(shipApp.getShipId());
				shipApp.wait();
			} else {
				System.out.println("Failed to connect to OceanServer");
			}
		}


	}

}
