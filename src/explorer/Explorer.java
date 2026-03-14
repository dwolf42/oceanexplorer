package explorer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Explorer {
	private boolean isExploring = true;

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

	public void explore() throws InterruptedException, SQLException, IOException {
		ShipApp shipApp = new ShipApp("Leopard1", "ship");

		synchronized (shipApp) {
			// establish connection to OceanServer
			if (shipApp.connectOS("127.0.0.1", 8150)) {
				System.out.println("Connected to OceanServer");
				shipApp.launch();
				shipApp.wait();
			} else {
				System.out.println("Failed to connect to OceanServer");
			}
		}

		Scanner scanner = new Scanner(System.in);

		String input;
		while (isExploring && !Thread.currentThread().isInterrupted()) {
			synchronized (shipApp) {

				System.out.println("System waiting for input (scan, navigate, radar, sub or exit):");
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
