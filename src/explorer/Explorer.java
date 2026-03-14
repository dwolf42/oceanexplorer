package explorer;

import explorer.view.ShipGui;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

// Please note: code regarding the torpedo-feature is AI generated
public class Explorer {

	public static void main(String[] args) throws InterruptedException {
        try {
            WebApp webApp = new WebApp();
            webApp.startWebApplication();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        Explorer exp = new Explorer();
		try {
			exp.explore();
		} catch (InterruptedException e) {
			// -
		} catch (SQLException | IOException e) {
            e.printStackTrace();
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
				shipApp.launch();
				shipApp.wait();
				shipGui = new ShipGui(shipApp);
			} else {
				System.out.println("Failed to connect to OceanServer");
			}
		}


	}

}
