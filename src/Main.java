import explorer.ShipApp;
import ocean.Vec2D;

import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		ShipApp shipApp = new ShipApp("The Ship", "ship");

		// establish connection to OceanServer
		if (shipApp.connectOS("127.0.0.1", 8150)) {
			System.out.println("Connected to OceanServer");
			shipApp.launch(new Vec2D(0,0), new Vec2D(1, 1));

		} else {
			System.out.println("Failed to connect to OceanServer");
		}

/*
shipApp.launch()
launch fragt mich dann, welche Koordinaten und welche Ausrichtung


 */
	}
}