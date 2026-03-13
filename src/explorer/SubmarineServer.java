package explorer;

import ocean.AppLauncher;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SubmarineServer extends Thread {
	private ServerSocket subSocket;
	private List<Submarine> submarines = new ArrayList<>();
    private int shipDatabaseIdentifier;

    public SubmarineServer(int shipDatabaseIdentifier) {
        this.shipDatabaseIdentifier = shipDatabaseIdentifier;
    }

    public void run() {
		try {
			subSocket = new ServerSocket(8152);

			while (!isInterrupted()) {
					Socket client = subSocket.accept();
					Submarine submarine = new Submarine(client, shipDatabaseIdentifier);
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
