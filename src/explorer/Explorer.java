package explorer;

import java.util.Scanner;

public class Explorer {
	public static void main(String[] args) {
		Explorer exp = new Explorer();
		exp.explore();
	}

	public void explore() {
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
		while (true) {
			System.out.println("System waiting for input:");
			input = scanner.nextLine();
			if (input.equalsIgnoreCase("exit")) {
				shipApp.exit();
			}



		}
	}

	public void operate() {
	}
}
