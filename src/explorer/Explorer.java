package explorer;

import java.util.Scanner;

public class Explorer {
	private boolean isExploring = true;
	public static void main(String[] args) throws InterruptedException {
		Explorer exp = new Explorer();
		exp.explore();
	}

	public void explore() throws InterruptedException {
		ShipApp shipApp = new ShipApp("The Ship", "ship");

		// establish connection to OceanServer
		if (shipApp.connectOS("127.0.0.1", 8150)) {
			System.out.println("Connected to OceanServer");
			shipApp.launch();
		} else {
			System.out.println("Failed to connect to OceanServer");
		}

		Scanner scanner = new Scanner(System.in);

		String input;
		while (isExploring) {
			System.out.println("System waiting for input:");
			input = scanner.nextLine().toLowerCase();

			switch (input) {
				case "scan":
					shipApp.scan();
					break;
				case "navigate":
					shipApp.navigate();
					break;
				case "radar":
					shipApp.radar();
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

	public void operate() {
	}
}
