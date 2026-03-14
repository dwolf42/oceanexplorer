package explorer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class SubmarineServer extends Thread {
	private ServerSocket subSocket;
	private List<Submarine> submarines = new ArrayList<>();
    private int shipDatabaseIdentifier;
    private int sectorID;

    public SubmarineServer(int shipDatabaseIdentifier, int sectorID) {
        this.shipDatabaseIdentifier = shipDatabaseIdentifier;
        this.sectorID = sectorID;
    }

    public void run() {
		try {
			subSocket = new ServerSocket(8152);

			while (!isInterrupted()) {
					Socket client = subSocket.accept();
					Submarine submarine = new Submarine(client, shipDatabaseIdentifier, sectorID);
					submarine.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
            e.printStackTrace();
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

}
