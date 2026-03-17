package explorer;

import ocean.Vec;
import ocean.Vec2D;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;


// Please note: code regarding the torpedo-feature is AI generated
// The Submarine class holds the socket (connection) to communicate with the SubmarineServer
public class Submarine extends Thread {
	private Socket connection;
	private BufferedReader in;
	private PrintWriter out;
	private Database database = new Database();
	private int shipDatabaseIdentifier;         // Primary key of the ship in the database
	private String serverSubID;                 // Submarine ID received from the server
	private int subIdentifier = 0;                  // Primary key of the submarine in the database
	private Vec2D sectorCoordinates;
    private int sectorID = 0;

	private boolean torpedoMode;
	private Thread torpedoThread;

	public Submarine(Socket connection, int shipDatabaseIdentifier, Vec2D sectorCoordinates, boolean torpedoMode) throws SQLException {
		this.connection = connection;
		this.shipDatabaseIdentifier = shipDatabaseIdentifier;
		this.sectorCoordinates = sectorCoordinates;
		this.torpedoMode = torpedoMode;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			out = new PrintWriter(connection.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Send and receive messages from SubmarineServer
	@Override
	public void run() {
		try {
			String line;

			while (!isInterrupted() && (line = in.readLine()) != null) {
				handleMessage(new JSONObject(new JSONTokener(line)));
			}

			System.out.println("Submarine thread exiting");
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// -
		} finally {
			exit();
		}
	}


	// Process torpedo communication separated from the other submarines to avoid interferences
	public void torpedoComm(JSONObject json) {
		out.println(json);
	}

	// Periodically sends the command to propell the torpedo
	private void startTorpedoMode() {
		torpedoThread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				torpedoComm(new JSONObject().put("cmd", "pilot").put("route", "C"));
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		torpedoThread.start();
	}


	public void handleMessage(JSONObject jsonObject) throws InterruptedException, SQLException {
		String cmd = jsonObject.get("cmd").toString();
		switch (cmd) {
            case "ready":
                System.out.println("Ready Message: " + jsonObject);
                this.serverSubID = jsonObject.get("id").toString();

                // checks if submarine does not exist in the database (if subIdentifier is still 0 it has not been inserted yet)
                if (subIdentifier == 0) {
                    // inserts submarine into the database and assigns the generated primary key to the subIdentifier
                    subIdentifier = database.insertSubmarineData(shipDatabaseIdentifier, serverSubID);
                }

				if (torpedoMode) startTorpedoMode();
				break;
            case "message":
                System.out.println("Message Message: " + jsonObject);
                break;
            case "measure":
                System.out.println("Measure Message: " + jsonObject);
                JSONArray vecs = jsonObject.getJSONArray("vecs");

                int x = 0;
                int y = 0;
                int z = 0;

                for (int i = 0; i < vecs.length(); i++) {
                    JSONArray vec = vecs.getJSONArray(i);
                    x = vec.getInt(0);
                    y = vec.getInt(1);
                    z = vec.getInt(2);
                }

                // If the sector does not exist in the sectors table (sectorID == 0),
                // insert it first and then retrieve its ID from the database
                if(sectorID == 0) {
                    database.insertSector(sectorCoordinates.getX(), sectorCoordinates.getY());
                    sectorID = database.getSectorID(sectorCoordinates);
                }

                database.insertSubMeasurements(subIdentifier, sectorID, x, y, z);
                break;
            case "crash":
                System.out.println("Crashed");
                System.out.println("Crash Message: " + jsonObject);

                JSONObject sunkPos = (JSONObject) jsonObject.query("/sunkPos");
                JSONArray vecCrash = sunkPos.getJSONArray("vec");

                database.insertSubSunkPosition(
                        vecCrash.getInt(0),
                        vecCrash.getInt(1),
                        vecCrash.getInt(2),
                        subIdentifier
                );
                exit();
                break;
            case "arise":
                System.out.println("Arise Message: " + jsonObject);

                JSONObject arisePos = (JSONObject) jsonObject.query("/arisePos");
                JSONArray vecArise = arisePos.getJSONArray("vec");

                // We pick only the x and y coordinates because z is almost always 1
                database.insertSubArisePosition(
                        vecArise.getInt(0),
                        vecArise.getInt(1),
                        subIdentifier
                );

                exit();
                break;
            default:
                System.out.println("Unknown Command: " + cmd);
        }
	}

	// The arise command triggers the submarine's thread interruption
	public void exit() {
		if (torpedoThread != null) torpedoThread.interrupt();
		try {
			out.close();
			in.close();
			connection.close();
			database.close();
			this.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getSubServerID() {
		return this.serverSubID;
	}

	public void setRoute(String route) {
		out.println(new JSONObject().put("cmd", "pilot").put("route", route));
	}

}
