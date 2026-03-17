package explorer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import ocean.Vec2D;

// Please note: code regarding the torpedo-feature is AI generated
// SubmarineServer has been extracted to a separate class to prevent ShipApp from cludder
// Adds a socket to each new submarine and handles communication with it
class SubmarineServer extends Thread {
	private ServerSocket subSocket;

	// Holds all Submaines, so they can be interrupted once the
	private List<Submarine> submarines = new ArrayList<>();
	private int shipDatabaseIdentifier;
	private Vec2D sectorCoordinates;
	private boolean nextIsTorpedo = false;
	private boolean hasSubmarineControlStarted = false;


	public SubmarineServer(int shipDatabaseIdentifier, Vec2D sector) {
		this.shipDatabaseIdentifier = shipDatabaseIdentifier;
		this.sectorCoordinates = sector;
	}

	public void setNextIsTorpedo(boolean nextIsTorpedo) {
		this.nextIsTorpedo = nextIsTorpedo;
	}

	public void run() {
		try {
			subSocket = new ServerSocket(8152);

			while (!isInterrupted()) {
				Socket client = subSocket.accept();
				Submarine submarine = new Submarine(client, shipDatabaseIdentifier, sectorCoordinates, this.nextIsTorpedo);
				this.nextIsTorpedo = false;
				submarine.start();
				submarines.add(submarine);

				// This sleep is here so Submarine has time to spawn before the control will access is.
				// Time may be reduced/increased depending on system performance
				Thread.sleep(5000); // wait for Submarine to spawn
				// prevents submarine control from starting multiple times
				if (!hasSubmarineControlStarted) {
					hasSubmarineControlStarted = true;
					startSubmarineControl();
				}
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			// After thread ends, this iterates through the submarines array list so they are interrupt properly
			submarines.forEach(Submarine::interrupt);
			if (subSocket != null) {
				try {
					subSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void startSubmarineControl() {
		// Reference to SubmarineServer, so the runnable here can stop if the server stops
		SubmarineServer self = this;
		Runnable subControlRunnable = new Runnable() {
			public void run() {
				Scanner scanner = new Scanner(System.in);
				// Contains the submarineIDs of available (spawned) submarines to make it easier to verify whether the user's desired submarine is available
				ArrayList<String> availableSubmarineIDs;
				while (!self.isInterrupted()) {
					if (submarines.isEmpty()) {
						break;
					}
					availableSubmarineIDs = new ArrayList<>();
					System.err.println("Submarine control ready. Input Submarine ID (numerical e.g. 01, 02...) and direction.");
					System.err.println("Available SubmarineIDs:");
					// Temporarily save submarineId so it can be print and put in the ArrayList
					String tempSubID = "";
					for (Submarine sub : submarines) {
						tempSubID = sub.getSubServerID();
						if (null == tempSubID) {
							continue;
						}
						// To only save the numerical pf the submarineID
						availableSubmarineIDs.add(tempSubID.split("#")[1].replace("sub", ""));
						System.err.println(tempSubID);
					}
					System.err.println(" ");
					System.err.println("---------------");
					System.err.println("Directions are:");
					System.err.println("NW, N, NE");
					System.err.println("W,  C, E");
					System.err.println("SW, S, SE");
					System.err.println("UP, DOWN");
					System.err.println("---------------");
					System.err.println(" ");

					System.err.println("Input Submarine ID: ");
					String inputSubID = scanner.nextLine().trim();
					System.err.println("Input direction: ");
					String inputSubRoute = scanner.nextLine().toUpperCase().trim();

					if (!availableSubmarineIDs.contains(inputSubID)) {
						System.err.println("Invalid submarine ID ");
						continue;
					}

					Submarine target = null;
					for (Submarine sub : submarines) {
						String numericID = sub.getSubServerID().split("#")[1].replace("sub", ""); // NEU
						if (numericID.equals(inputSubID)) {
							target = sub;
							break;
						}
					}

					if (target != null) {
						target.setRoute(inputSubRoute);
						System.err.println("Sent route " + inputSubRoute + " to " + inputSubID);
					}
				}
			}
		};
		Thread subControlThread = new Thread(subControlRunnable);
		subControlThread.start();
	}
}
